package com.gestionpresence.controller;

import com.gestionpresence.model.Presence;
import com.gestionpresence.service.PresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/presences")
@CrossOrigin(origins = "http://localhost:4200")
public class PresenceController {
    
    @Autowired
    private PresenceService presenceService;
    
    @PostMapping("/pointage")
    public ResponseEntity<Presence> enregistrerPointage(@RequestParam String matricule,
                                                       @RequestParam String typePointage) {
        try {
            Presence presence = presenceService.enregistrerPointage(matricule, typePointage, LocalDateTime.now());
            return ResponseEntity.ok(presence);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/employee/{employeeId}")
    public List<Presence> getPresencesByEmployee(@PathVariable Long employeeId) {
        return presenceService.findByEmployeeId(employeeId);
    }
    
    @GetMapping("/periode")
    public List<Presence> getPresencesByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return presenceService.findByPeriode(debut, fin);
    }
}