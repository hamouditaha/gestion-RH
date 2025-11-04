package com.gestionpresence.controller;

import com.gestionpresence.dto.PresenceDTO;
import com.gestionpresence.service.PresenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/presences")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class PresenceController {

    @Autowired
    private PresenceService presenceService;

    @PostMapping("/pointage")
    public ResponseEntity<PresenceDTO> enregistrerPointage(
            @RequestParam @NotBlank String matricule,
            @RequestParam @NotBlank String typePointage) {
        log.info("Enregistrement du pointage pour le matricule: {}", matricule);
        PresenceDTO presence = presenceService.enregistrerPointage(matricule, typePointage, LocalDateTime.now());
        return ResponseEntity.ok(presence);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PresenceDTO>> getPresencesByEmployee(@PathVariable @NotNull Long employeeId) {
        log.info("Récupération des présences pour l'employé: {}", employeeId);
        List<PresenceDTO> presences = presenceService.findByEmployeeId(employeeId);
        return ResponseEntity.ok(presences);
    }

    @GetMapping("/periode")
    public ResponseEntity<List<PresenceDTO>> getPresencesByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        log.info("Récupération des présences pour la période: {} à {}", debut, fin);
        List<PresenceDTO> presences = presenceService.findByPeriode(debut, fin);
        return ResponseEntity.ok(presences);
    }
}
