package com.jiyuu.banking.dto;

import com.jiyuu.banking.entity.Transactions;
import java.math.BigDecimal;

public record TransactionResponse(
        long id,
        String transactionRef,
        String currency,
        String type,
        BigDecimal amount,
        String status,
        String originalTransactionRef,
        String source,
        String target,
        String createdAt
) {
    public static TransactionResponse of(Transactions transactions, String source, String target) {
        return new TransactionResponse(
                transactions.getIdTransaction(),
                transactions.getTransactionRef(),
                transactions.getCurrencyTransaction().name(),
                transactions.getTransactionType().name(),
                transactions.getAmountTransaction(),
                transactions.getTransactionStatus().name(),
                transactions.getOriginalTransactionRef(),
                source,
                target,
                transactions.getCreatedAt().toString()
        );
    }
}
