package com.gestionpresence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulletinSalaireDTO {

    private Long id;

    @NotNull(message = "L'ID de l'employé est obligatoire")
    private Long employeeId;

    private String employeeMatricule;
    private String employeeNom;
    private String employeePrenom;

    @NotNull(message = "La date de début de période est obligatoire")
    private LocalDate periodeDebut;

    @NotNull(message = "La date de fin de période est obligatoire")
    private LocalDate periodeFin;

    private double salaireBase;
    private int joursTravailles;
    private int joursAbsence;
    private int totalRetardsMinutes;
    private double heuresSupplementaires;
    private double deductionAbsences;
    private double deductionRetards;
    private double salaireNet;
    private boolean envoye;
}
