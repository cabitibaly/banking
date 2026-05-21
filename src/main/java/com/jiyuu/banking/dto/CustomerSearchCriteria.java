package com.jiyuu.banking.dto;

public record CustomerSearchCriteria(
        String nom,
        String telephone,
        String status,
        String numero
) {
}
