package com.jiyuu.banking.dto;

import com.jiyuu.banking.entity.Account;

import java.math.BigDecimal;

public record AccountResponse(
        long id,
        String numeroCompte,
        String type,
        String status,
        String currency,
        BigDecimal solde,
        BigDecimal decouvert,
        boolean isDecouvert
) {
    public static AccountResponse of(Account account) {
        return new AccountResponse(
                account.getIdAccount(),
                account.getNumeroAccount(),
                account.getAccountType().toString(),
                account.getAccountStatus().toString(),
                account.getCurrencyAccount().toString(),
                account.getSoldeAccount(),
                account.getDecouvert(),
                account.isEstDecouvert()
        );
    }
}
