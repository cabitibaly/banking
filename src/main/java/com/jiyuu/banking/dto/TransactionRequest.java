package com.jiyuu.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "Le type de transaction est obligatoire")
        String type,

        @NotNull(message = "Le montant est obligatoire")
        @PositiveOrZero(message = "Le montant doit être positif")
        BigDecimal amount,

        @NotBlank(message = "La monnaie est obligatoire")
        String currency,

        Long source,
        Long target
) {
}
