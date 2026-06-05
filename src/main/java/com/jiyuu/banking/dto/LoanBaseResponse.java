package com.jiyuu.banking.dto;

import com.jiyuu.banking.entity.Loan;

import java.math.BigDecimal;

public record LoanBaseResponse(
        long id,
        String customer,
        String telephone,
        String accountNumber,
        String reason,
        int duration,
        BigDecimal amount,
        BigDecimal interestRate,
        String type,
        String status
) {
    public static LoanBaseResponse of(Loan loan) {
        return new LoanBaseResponse(
                loan.getIdLoan(),
                loan.getCustomer().getNomCustomer(),
                loan.getCustomer().getTelephoneCustomer(),
                loan.getAccount().getNumeroAccount(),
                loan.getReason(),
                loan.getDuration(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getLoanType().toString(),
                loan.getLoanStatus().toString()
        );
    }
}
