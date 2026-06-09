package com.jiyuu.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebitRequest(

        @NotBlank(message = "Le numéro de carte est obligatoire")
        String cardNumber,

        @NotBlank(message = "Le code PIN est obligatoire")
        String pin,

        @NotBlank(message = "Le code CVV est obligatoire")
        String cvv,

        @NotNull(message = "La date d'expiration est obligatoire")
        LocalDate expirationDate,

        @NotNull(message = "Le montant est obligatoire")
        @PositiveOrZero(message = "Le montant doit être positif")
        BigDecimal amount,

        @NotBlank(message = "La monnaie est obligatoire")
        String currency
) {
}
