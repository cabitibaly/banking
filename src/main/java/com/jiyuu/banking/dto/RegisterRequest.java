package com.jiyuu.banking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email est invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String password,

        @NotBlank(message = "Le rôle est obligatoire")
        String role
) {
}
