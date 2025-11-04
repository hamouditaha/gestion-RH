package com.gestionpresence.service;

import com.gestionpresence.dto.BulletinSalaireDTO;
import com.gestionpresence.exception.BusinessException;
import com.gestionpresence.exception.ResourceNotFoundException;
import com.gestionpresence.model.Employee;
import com.gestionpresence.model.Presence;
import com.gestionpresence.model.BulletinSalaire;
import com.gestionpresence.repository.PresenceRepository;
import com.gestionpresence.repository.EmployeeRepository;
import com.gestionpresence.repository.BulletinSalaireRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
@Transactional
public class CalculSalaireService {
    
    @Autowired
    private PresenceRepository presenceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BulletinSalaireRepository bulletinSalaireRepository;
    
    // Configuration externalisée
    @Value("${app.salaire.deduction.jour-absence:200.0}")
    private double deductionParJourAbsence;
    
    @Value("${app.salaire.deduction.minute-retard:2.0}")
    private double deductionParMinuteRetard;
    
    @Value("${app.salaire.heure.debut:09:00}")
    private LocalTime heureDebutTravail;
    
    @Value("${app.salaire.heure.fin:17:00}")
    private LocalTime heureFinTravail;
    
    @Value("${app.salaire.jours.ouvrables:5}")
    private int joursOuvrablesParSemaine;
    
    // Cache pour optimiser les calculs répétitifs
    private final Map<String, Double> cacheCalculSalaire = new ConcurrentHashMap<>();
    
    /**
     * Calcule le salaire mensuel avec toutes les déductions
     */
    public BulletinSalaire calculerSalaireMensuel(Employee employee, LocalDate mois) {
        log.info("Calcul du salaire pour l'employé {} - Mois: {}", 
                 employee.getMatricule(), mois);
        
        try {
            LocalDate debutMois = mois.withDayOfMonth(1);
            LocalDate finMois = mois.withDayOfMonth(mois.lengthOfMonth());
            
            // Vérification de la période valide
            validerPeriode(debutMois, finMois);
            
            // Récupération et analyse des présences
            List<Presence> presences = recupererPresencesMensuelles(employee, debutMois, finMois);
            AnalysePresence analyse = analyserPresences(presences, debutMois, finMois);
            
            // Calcul des déductions
            CalculDeductions deductions = calculerDeductions(analyse, employee.getSalaireBase());
            
            // Création du bulletin
            return creerBulletinSalaire(employee, debutMois, finMois, analyse, deductions);
            
        } catch (Exception e) {
            log.error("Erreur lors du calcul du salaire pour l'employé {}", 
                     employee.getMatricule(), e);
            throw new CalculSalaireException(
                "Erreur lors du calcul du salaire: " + e.getMessage(), e);
        }
    }
    


    /**
     * Calcule le salaire mensuel avec signature compatible avec l'ancien appel
     */
    public BulletinSalaireDTO calculerSalaireMensuel(Long employeeId, LocalDate mois) throws ResourceNotFoundException {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'id: " + employeeId));
        BulletinSalaire bulletin = calculerSalaireMensuel(employee, mois);
        return convertToDTO(bulletin);
    }

    /**
     * Calcule les salaires pour tous les employés
     */
    public void calculerTousLesSalaires(LocalDate mois) throws BusinessException {
        log.info("Calcul des salaires pour tous les employés - Mois: {}", mois);

        List<Employee> employees = employeeRepository.findAll();
        List<BulletinSalaire> bulletins = new ArrayList<>();

        for (Employee employee : employees) {
            try {
                BulletinSalaire bulletin = calculerSalaireMensuel(employee, mois);
                bulletins.add(bulletin);
                bulletinSalaireRepository.save(bulletin);
                log.info("Bulletin calculé et sauvegardé pour {}", employee.getMatricule());
            } catch (Exception e) {
                log.error("Erreur lors du calcul du salaire pour {}", employee.getMatricule(), e);
                throw new BusinessException("Erreur lors du calcul des salaires: " + e.getMessage(), e);
            }
        }

        log.info("Calcul terminé pour {} employés", bulletins.size());
    }
    
    /**
     * Récupère et valide les présences du mois
     */
    private List<Presence> recupererPresencesMensuelles(Employee employee, LocalDate debut, LocalDate fin) {
        List<Presence> presences = presenceRepository.findByEmployeeIdAndDateHeurePointageBetween(
            employee.getId(),
            debut.atStartOfDay(),
            fin.atTime(23, 59, 59)
        );

        if (presences.isEmpty()) {
            log.warn("Aucune présence trouvée pour l'employé {} sur la période {}-{}",
                    employee.getMatricule(), debut, fin);
        }

        return presences;
    }
    
    /**
     * Analyse complète des présences
     */
    private AnalysePresence analyserPresences(List<Presence> presences, LocalDate debut, LocalDate fin) {
        Map<LocalDate, List<Presence>> presencesParJour = grouperPresencesParJour(presences);
        
        int joursTravailles = calculerJoursTravailles(presencesParJour);
        int joursOuvrables = calculerJoursOuvrables(debut, fin);
        int joursAbsence = Math.max(0, joursOuvrables - joursTravailles);
        int totalRetardsMinutes = calculerTotalRetards(presencesParJour);
        double heuresSupplementaires = calculerHeuresSupplementaires(presencesParJour);
        
        return AnalysePresence.builder()
                .joursTravailles(joursTravailles)
                .joursOuvrables(joursOuvrables)
                .joursAbsence(joursAbsence)
                .totalRetardsMinutes(totalRetardsMinutes)
                .heuresSupplementaires(heuresSupplementaires)
                .presencesParJour(presencesParJour)
                .build();
    }
    
    /**
     * Groupe les présences par date
     */
    private Map<LocalDate, List<Presence>> grouperPresencesParJour(List<Presence> presences) {
        return presences.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getDateHeurePointage().toLocalDate()
                ));
    }
    
    /**-----------------------------------------------------------------------------------------------------------------
     * Calcule les jours effectivement travaillés
     */
    private int calculerJoursTravailles(Map<LocalDate, List<Presence>> presencesParJour) {
        return (int) presencesParJour.entrySet().stream()
                .filter(entry -> estJourTravaillé(entry.getKey(), entry.getValue()))
                .count();
    }
    
    /**
     * Détermine si un jour est considéré comme travaillé
     */
    private boolean estJourTravaillé(LocalDate date, List<Presence> presencesDuJour) {
        if (estWeekend(date) || estJourFerie(date)) {
            return false;
        }
        
        // Un jour est travaillé s'il y a au moins un pointage "ENTREE"
        return presencesDuJour.stream()
                .anyMatch(p -> "ENTREE".equals(p.getTypePointage()));
    }
    
    /**
     * Calcule les jours ouvrables (hors weekends et jours fériés)
     */
    private int calculerJoursOuvrables(LocalDate debut, LocalDate fin) {
        return (int) debut.datesUntil(fin.plusDays(1))
                .filter(date -> !estWeekend(date) && !estJourFerie(date))
                .count();
    }
    
    /**
     * Vérifie si c'est un weekend
     */
    private boolean estWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * Vérifie si c'est un jour férié (à implémenter selon le pays)
     */
    private boolean estJourFerie(LocalDate date) {
        // TODO: Implémenter la logique des jours fériés
        // Pour l'instant, retourne false
        return false;
    }
    
    /**
     * Calcule le total des retards en minutes
     */
    private int calculerTotalRetards(Map<LocalDate, List<Presence>> presencesParJour) {
        return presencesParJour.values().stream()
                .mapToInt(this::calculerRetardJour)
                .sum();
    }
    
    /**
     * Calcule le retard pour une journée
     */
    private int calculerRetardJour(List<Presence> presencesDuJour) {
        return presencesDuJour.stream()
                .filter(p -> "ENTREE".equals(p.getTypePointage()))
                .mapToInt(this::calculerRetardPresence)
                .sum();
    }
    
    /**
     * Calcule le retard en minutes pour une présence
     */
    private int calculerRetardPresence(Presence presence) {
        LocalDateTime heurePointage = presence.getDateHeurePointage();
        LocalDateTime heureNormale = heurePointage.toLocalDate().atTime(heureDebutTravail);
        
        if (heurePointage.isAfter(heureNormale)) {
            return (int) ChronoUnit.MINUTES.between(heureNormale, heurePointage);
        }
        return 0;
    }
    
    /**
     * Calcule les heures supplémentaires
     */
    private double calculerHeuresSupplementaires(Map<LocalDate, List<Presence>> presencesParJour) {
        return presencesParJour.entrySet().stream()
                .filter(entry -> !estWeekend(entry.getKey()))
                .mapToDouble(entry -> calculerHeuresSupplementairesJour(entry.getKey(), entry.getValue()))
                .sum();
    }
    
    /**
     * Calcule les heures supplémentaires pour une journée
     */
    private double calculerHeuresSupplementairesJour(LocalDate date, List<Presence> presencesDuJour) {
        // Simplifié: heures après l'heure de fin normale
        return presencesDuJour.stream()
                .filter(p -> "SORTIE".equals(p.getTypePointage()))
                .mapToDouble(p -> {
                    LocalDateTime heureFinNormale = date.atTime(heureFinTravail);
                    if (p.getDateHeurePointage().isAfter(heureFinNormale)) {
                        return ChronoUnit.MINUTES.between(heureFinNormale, p.getDateHeurePointage()) / 60.0;
                    }
                    return 0;
                })
                .sum();
    }
    
    /**
     * Calcule toutes les déductions
     */
    private CalculDeductions calculerDeductions(AnalysePresence analyse, double salaireBase) {
        double deductionAbsences = analyse.getJoursAbsence() * deductionParJourAbsence;
        double deductionRetards = analyse.getTotalRetardsMinutes() * deductionParMinuteRetard;
        
        // Éviter les déductions supérieures au salaire de base
        double totalDeductions = deductionAbsences + deductionRetards;
        if (totalDeductions > salaireBase) {
            log.warn("Les déductions ({}) dépassent le salaire base ({})", 
                    totalDeductions, salaireBase);
            deductionAbsences = salaireBase * (deductionAbsences / totalDeductions);
            deductionRetards = salaireBase * (deductionRetards / totalDeductions);
        }
        
        double salaireNet = Math.max(0, salaireBase - deductionAbsences - deductionRetards);
        
        return CalculDeductions.builder()
                .deductionAbsences(deductionAbsences)
                .deductionRetards(deductionRetards)
                .salaireNet(salaireNet)
                .build();
    }
    
    /**
     * Crée le bulletin de salaire final
     */
    private BulletinSalaire creerBulletinSalaire(Employee employee, LocalDate debut, LocalDate fin,
                                                AnalysePresence analyse, CalculDeductions deductions) {
        BulletinSalaire bulletin = new BulletinSalaire();
        bulletin.setEmployee(employee);
        bulletin.setPeriodeDebut(debut);
        bulletin.setPeriodeFin(fin);
        bulletin.setSalaireBase(employee.getSalaireBase());
        bulletin.setJoursTravailles(analyse.getJoursTravailles());
        bulletin.setJoursAbsence(analyse.getJoursAbsence());
        bulletin.setTotalRetardsMinutes(analyse.getTotalRetardsMinutes());
        bulletin.setHeuresSupplementaires(analyse.getHeuresSupplementaires());
        bulletin.setDeductionAbsences(deductions.getDeductionAbsences());
        bulletin.setDeductionRetards(deductions.getDeductionRetards());
        bulletin.setSalaireNet(deductions.getSalaireNet());
        bulletin.setEnvoye(false);
        
        log.info("Bulletin créé pour {} - Salaire net: {}", 
                employee.getMatricule(), deductions.getSalaireNet());
        
        return bulletin;
    }
    
    /**
     * Validation de la période
     */
    private void validerPeriode(LocalDate debut, LocalDate fin) {
        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin");
        }
        
        if (debut.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La période ne peut pas être dans le futur");
        }
    }
    
    // Classes internes pour une meilleure organisation
    
    @lombok.Builder
    @lombok.Data
    private static class AnalysePresence {
        private int joursTravailles;
        private int joursOuvrables;
        private int joursAbsence;
        private int totalRetardsMinutes;
        private double heuresSupplementaires;
        private Map<LocalDate, List<Presence>> presencesParJour;
    }
    
    @lombok.Builder
    @lombok.Data
    private static class CalculDeductions {
        private double deductionAbsences;
        private double deductionRetards;
        private double salaireNet;
    }
    
    /**
     * Exception métier pour les calculs de salaire
     */
    public static class CalculSalaireException extends RuntimeException {
        public CalculSalaireException(String message) {
            super(message);
        }

        public CalculSalaireException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private BulletinSalaireDTO convertToDTO(BulletinSalaire bulletin) {
        return BulletinSalaireDTO.builder()
                .id(bulletin.getId())
                .employeeId(bulletin.getEmployee().getId())
                .employeeMatricule(bulletin.getEmployee().getMatricule())
                .employeeNom(bulletin.getEmployee().getNom())
                .employeePrenom(bulletin.getEmployee().getPrenom())
                .periodeDebut(bulletin.getPeriodeDebut())
                .periodeFin(bulletin.getPeriodeFin())
                .salaireBase(bulletin.getSalaireBase())
                .joursTravailles(bulletin.getJoursTravailles())
                .joursAbsence(bulletin.getJoursAbsence())
                .totalRetardsMinutes(bulletin.getTotalRetardsMinutes())
                .heuresSupplementaires(bulletin.getHeuresSupplementaires())
                .deductionAbsences(bulletin.getDeductionAbsences())
                .deductionRetards(bulletin.getDeductionRetards())
                .salaireNet(bulletin.getSalaireNet())
                .envoye(bulletin.isEnvoye())
                .build();
    }
}
