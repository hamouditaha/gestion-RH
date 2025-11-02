package com.gestionpresence.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
public class ScheduledTasks {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private EmployeeService employeeService;

    // Heure limite pour considérer un employé comme absent (17h00)
    private static final LocalTime ABSENCE_TIME_LIMIT = LocalTime.of(17, 0);

    /**
     * Calcul automatique des salaires à la fin du mois
     */
    @Scheduled(cron = "0 0 20 L * ?") // Dernier jour du mois à 20h00
    public void calculAutoSalairesMensuels() {
        log.info(" Début du calcul automatique des salaires mensuels...");
        // Implémentation à ajouter
    }

    /**
     * Nettoyage des anciennes données
     */
    @Scheduled(cron = "0 0 2 * * SUN") // Tous les dimanches à 2h00
    public void nettoyageDonnees() {
        log.info(" Nettoyage des données anciennes...");
        // Implémentation à ajouter
    }

    /**
     * Vérification de la santé de l'application
     */
    @Scheduled(fixedRate = 300000) // Toutes les 5 minutes
    public void healthCheck() {
        log.debug(" Health check - Application en cours d'exécution");
    }

    /**
     * Marquage automatique des absences quotidiennes
     * S'exécute tous les jours à 17h30
     */
    @Scheduled(cron = "0 30 17 * * ?")
    public void marquerAbsencesQuotidiennes() {
        log.info("Début du marquage automatique des absences quotidiennes...");

        LocalDate today = LocalDate.now();
        List<com.gestionpresence.model.Employee> employees = employeeService.findAll();

        for (com.gestionpresence.model.Employee employee : employees) {
            try {
                // Vérifier si l'employé a déjà un pointage aujourd'hui
                boolean isPresent = presenceService.isPresentToday(employee.getId());

                if (!isPresent) {
                    // Créer un pointage d'absence
                    presenceService.enregistrerPointage(
                        employee.getMatricule(),
                        "ABSENT",
                        today.atTime(ABSENCE_TIME_LIMIT)
                    );
                    log.info("Absence marquée pour l'employé: {}", employee.getMatricule());
                }
            } catch (Exception e) {
                log.error("Erreur lors du marquage d'absence pour l'employé {}: {}", employee.getMatricule(), e.getMessage());
            }
        }

        log.info("Marquage des absences quotidiennes terminé.");
    }
}
