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
public class EmployeeDTO {

    private Long id;

    @NotBlank(message = "Le matricule est obligatoire")
    @Size(min = 3, max = 20, message = "Le matricule doit contenir entre 3 et 20 caractères")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Le matricule ne peut contenir que des lettres majuscules et des chiffres")
    private String matricule;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le poste est obligatoire")
    @Size(min = 2, max = 100, message = "Le poste doit contenir entre 2 et 100 caractères")
    private String poste;

    @NotNull(message = "Le salaire de base est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le salaire doit être positif")
    @DecimalMax(value = "100000.0", message = "Le salaire ne peut pas dépasser 100 000")
    private Double salaireBase;

    private LocalDate dateEmbauche;

    private String qrCodeBase64;
}
