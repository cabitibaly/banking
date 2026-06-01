package com.jiyuu.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record LoanRequest(
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        String telephone,

        @NotBlank(message = "Le numéro de compte est obligatoire")
        String accountNumber,

        @NotNull(message = "Le montant est obligatoire")
        @PositiveOrZero(message = "Le montant est obligatoire")
        BigDecimal amount,

        @NotBlank(message = "La raison est obligatoire")
        String reason,

        @NotNull(message = "La durée est obligatoire")
        @PositiveOrZero(message = "La durée est obligatoire")
        Integer duration,

        @NotBlank(message = "Le type de crédit est obligatoire")
        String type
) {
}
