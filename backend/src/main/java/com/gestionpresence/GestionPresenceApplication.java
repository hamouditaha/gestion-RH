package com.gestionpresence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableCaching        // Activation du cache
@EnableAsync          // Activation des méthodes asynchrones
@EnableScheduling     // Activation des tâches planifiées
public class GestionPresenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionPresenceApplication.class, args);
    }

    /**
     * Méthode exécutée après le démarrage de l'application
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("🚀 =========================================");
        log.info("🚀 Application Gestion-Presence démarrée !");
        log.info("🚀 =========================================");
        log.info("📊 Module: Gestion des présences par QR Code");
        log.info("💼 Fonctionnalités:");
        log.info("   • Scan QR Code des employés");
        log.info("   • Suivi des présences/absences");
        log.info("   • Calcul automatique des salaires");
        log.info("   • Envoi des bulletins par email");
        log.info("🔗 API REST disponible sur: http://localhost:8080/api");
        log.info("📚 Documentation: http://localhost:8080/swagger-ui.html");
        log.info("===========================================");
    }

    /**
     * Gestion des exceptions non capturées
     */
    @EventListener
    public void handleUncaughtException(Throwable exception) {
        log.error("❌ Exception non gérée détectée: {}", exception.getMessage(), exception);
    }
}