package com.jiyuu.banking.dto;

import com.jiyuu.banking.enums.Network;
import jakarta.validation.constraints.NotBlank;

public record CardRequest(
        @NotBlank(message = "Le numéro de compte est obligatoire")
        String accountNumber,

        Network network
) {
}
