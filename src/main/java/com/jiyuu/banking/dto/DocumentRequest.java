package com.jiyuu.banking.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentRequest(
        @NotBlank(message = "Le fichier KYC est obligatoire")
        String fileUrl,

        @NotBlank(message = "Le type du document est obligatoire")
        String kycType
) {
}
