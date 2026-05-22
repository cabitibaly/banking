package com.jiyuu.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank(message = "Le type de compte est obligatoire")
        String type,

        @NotNull(message = "Le solde decouvert est obligatoire")
        BigDecimal decouvert,

        @NotBlank(message = "La monnaie est obligatoire")
        String currency,

        @NotNull(message = "Veuillez selectionner un client")
        long idCustomer
) {
}
