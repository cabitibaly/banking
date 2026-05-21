package com.jiyuu.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CustomerRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le téléphone est obligatoire")
        String telephone,

        @NotNull(message = "La date de naissance est obligatoire")
        LocalDateTime dateNaissance
) {
}
