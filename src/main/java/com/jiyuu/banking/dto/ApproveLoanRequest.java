package com.jiyuu.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ApproveLoanRequest(

        @NotNull(message = "Le montant est obligatoire")
        @PositiveOrZero(message = "Le montant doit être supérieur à 0")
        BigDecimal interest,

        @NotBlank(message = "Veuillez renseigner un motif")
        String comments
) {
}
