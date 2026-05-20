package com.jiyuu.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ResetPassword(
        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        String newPassword,

        @NotBlank(message = "L'ancien mot de passe est obligatoire")
        String oldPassword
) {
}
