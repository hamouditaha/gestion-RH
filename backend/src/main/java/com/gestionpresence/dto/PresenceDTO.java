package com.gestionpresence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceDTO {

    private Long id;

    @NotNull(message = "L'ID de l'employé est obligatoire")
    private Long employeeId;

    private String employeeMatricule;
    private String employeeNom;
    private String employeePrenom;

    @NotNull(message = "La date et heure du pointage sont obligatoires")
    @PastOrPresent(message = "La date du pointage ne peut pas être dans le futur")
    private LocalDateTime dateHeurePointage;

    @NotBlank(message = "Le type de pointage est obligatoire")
    @Pattern(regexp = "^(ENTREE|SORTIE)$", message = "Le type doit être ENTREE ou SORTIE")
    private String typePointage;

    private boolean retard;
    private int minutesRetard;
}
