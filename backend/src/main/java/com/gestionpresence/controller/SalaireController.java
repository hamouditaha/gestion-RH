package com.gestionpresence.controller;

import com.gestionpresence.dto.BulletinSalaireDTO;
import com.gestionpresence.service.CalculSalaireService;
import com.gestionpresence.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/salaires")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class SalaireController {

    @Autowired
    private CalculSalaireService calculSalaireService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/calculer/{employeeId}")
    public ResponseEntity<BulletinSalaireDTO> calculerSalaire(
            @PathVariable @NotNull Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mois) {
        log.info("Calcul du salaire pour l'employé {} pour le mois {}", employeeId, mois);
        BulletinSalaireDTO bulletin = calculSalaireService.calculerSalaireMensuel(employeeId, mois);
        return ResponseEntity.ok(bulletin);
    }

    @PostMapping("/envoyer/{bulletinId}")
    public ResponseEntity<String> envoyerBulletin(@PathVariable @NotNull Long bulletinId) {
        log.info("Envoi du bulletin de salaire {}", bulletinId);
        emailService.envoyerBulletinSalaire(bulletinId);
        return ResponseEntity.ok("Bulletin envoyé avec succès");
    }

    @PostMapping("/calculer-tous")
    public ResponseEntity<String> calculerSalairesMensuels(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mois) {
        log.info("Calcul des salaires pour le mois {}", mois);
        calculSalaireService.calculerTousLesSalaires(mois);
        return ResponseEntity.ok("Calcul des salaires terminé");
    }
}
