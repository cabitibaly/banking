package com.jiyuu.banking.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
        long id,
        String numero,
        String nom,
        String prenom,
        String email,
        String telephone,
        LocalDateTime dateNaissance,
        String statusCustomer,
        LocalDateTime dateCreation,
        LocalDateTime dateModification
) {
}
