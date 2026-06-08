package com.jiyuu.banking.dto;

import com.jiyuu.banking.entity.Card;

import java.time.LocalDate;

public record CardResponse(
        long id,
        String cardNumber,
        LocalDate expireAt,
        String state,
        String network,
        String cvv
) {
    public static CardResponse of(Card card, String cvv) {
        return new CardResponse(
                card.getIdCard(),
                card.getCardNumber(),
                card.getExpireAt(),
                card.getState().name(),
                card.getNetwork().name(),
                cvv
        );
    }
}
