package com.jiyuu.banking.dto;

public record AccountSearchCriteria(
        String numero,
        String status,
        String type
) {
}
