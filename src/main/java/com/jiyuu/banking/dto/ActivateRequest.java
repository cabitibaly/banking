package com.jiyuu.banking.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivateRequest(
        @NotBlank(message = "Le code PIN est obligatoire")
        String pin
) {
}
