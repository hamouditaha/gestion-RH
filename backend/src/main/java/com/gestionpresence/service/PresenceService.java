package com.gestionpresence.service;

import com.gestionpresence.model.Employee;
import com.gestionpresence.model.Presence;
import com.gestionpresence.repository.EmployeeRepository;
import com.gestionpresence.repository.PresenceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class PresenceService {

    @Autowired
    private PresenceRepository presenceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Configuration des horaires de travail
    private static final LocalTime HEURE_DEBUT_TRAVAIL = LocalTime.of(9, 0);
    private static final LocalTime HEURE_FIN_TRAVAIL = LocalTime.of(17, 0);

    public List<Presence> findByEmployeeId(Long employeeId) {
        return presenceRepository.findByEmployeeId(employeeId);
    }

    public List<Presence> findByPeriode(LocalDate debut, LocalDate fin) {
        return presenceRepository.findByPeriode(debut, fin);
    }

    public Presence enregistrerPointage(String matricule, String typePointage, LocalDateTime dateHeurePointage) {
        log.info("Enregistrement du pointage pour {} - Type: {} - Date/Heure: {}", matricule, typePointage, dateHeurePointage);

        // Récupérer l'employé avec toutes ses informations
        Employee employee = employeeRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec le matricule: " + matricule));

        log.info("Informations employé: {} {} - Poste: {} - Email: {}",
                employee.getPrenom(), employee.getNom(), employee.getPoste(), employee.getEmail());

        // Créer la présence
        Presence presence = new Presence(employee, dateHeurePointage, typePointage);

        // Calculer le retard si c'est une entrée
        if ("ENTREE".equals(typePointage)) {
            calculerRetard(presence);
        }

        Presence savedPresence = presenceRepository.save(presence);
        log.info("Pointage enregistré avec succès - ID: {} - Employé: {} {} - Type: {}",
                savedPresence.getId(), employee.getPrenom(), employee.getNom(), typePointage);

        return savedPresence;
    }

    private void calculerRetard(Presence presence) {
        LocalTime heurePointage = presence.getDateHeurePointage().toLocalTime();

        if (heurePointage.isAfter(HEURE_DEBUT_TRAVAIL)) {
            long minutesRetard = java.time.Duration.between(HEURE_DEBUT_TRAVAIL, heurePointage).toMinutes();
            presence.setRetard(true);
            presence.setMinutesRetard((int) minutesRetard);
            log.info("Retard détecté: {} minutes pour {}", minutesRetard, presence.getEmployee().getMatricule());
        } else {
            presence.setRetard(false);
            presence.setMinutesRetard(0);
        }
    }

    public List<Presence> getPresencesByEmployeeAndPeriode(Long employeeId, LocalDate debut, LocalDate fin) {
        return presenceRepository.findByEmployeeIdAndPeriode(employeeId, debut, fin);
    }

    public boolean isPresentToday(Long employeeId) {
        LocalDate today = LocalDate.now();
        long entrees = presenceRepository.countEntreesByEmployeeAndDate(employeeId, today);
        long sorties = presenceRepository.countSortiesByEmployeeAndDate(employeeId, today);

        // Considérer présent si au moins une entrée et pas de sortie ou plus d'entrées que de sorties
        return entrees > sorties;
    }

    public Optional<Presence> findById(Long id) {
        return presenceRepository.findById(id);
    }

    public void deletePresence(Long id) {
        Presence presence = presenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Présence non trouvée avec l'id: " + id));
        presenceRepository.delete(presence);
    }

    public List<Presence> getAllPresences() {
        return presenceRepository.findAll();
    }
}
