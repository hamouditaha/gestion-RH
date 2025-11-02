package com.gestionpresence.controller;

import com.gestionpresence.model.BulletinSalaire;
import com.gestionpresence.service.CalculSalaireService;
import com.gestionpresence.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/salaires")
@CrossOrigin(origins = "http://localhost:4200")
public class SalaireController {

    @Autowired
    private CalculSalaireService calculSalaireService;

    @Autowired
    private EmailService emailService;
    
    @PostMapping("/calculer/{employeeId}")
    public ResponseEntity<BulletinSalaire> calculerSalaire(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mois) {

        try {
            BulletinSalaire bulletin = calculSalaireService.calculerSalaireMensuel(employeeId, mois);
            return ResponseEntity.ok(bulletin);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/envoyer/{bulletinId}")
    public ResponseEntity<String> envoyerBulletin(@PathVariable Long bulletinId) {
        try {
            emailService.envoyerBulletinSalaire(bulletinId);
            return ResponseEntity.ok("Bulletin envoyé avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'envoi: " + e.getMessage());
        }
    }
    
    @PostMapping("/calculer-tous")
    public ResponseEntity<String> calculerSalairesMensuels(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mois) {
        
        try {
            calculSalaireService.calculerTousLesSalaires(mois);
            return ResponseEntity.ok("Calcul des salaires terminé");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du calcul: " + e.getMessage());
        }
    }
}