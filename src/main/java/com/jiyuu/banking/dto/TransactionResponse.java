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
        String source,
        String target
) {
    public static TransactionResponse of(Transactions transactions, String source, String target) {
        return new TransactionResponse(
                transactions.getIdTransaction(),
                transactions.getTransactionRef(),
                transactions.getCurrencyTransaction().name(),
                transactions.getTransactionType().name(),
                transactions.getAmountTransaction(),
                transactions.getTransactionStatus().name(),
                source,
                target
        );
    }
}
