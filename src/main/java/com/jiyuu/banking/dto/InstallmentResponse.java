package com.jiyuu.banking.dto;

import com.jiyuu.banking.entity.LoanInstallment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentResponse(
        long id,
        int number,
        String status,
        LocalDate dueDate,
        BigDecimal principal,
        BigDecimal interestAmount,
        BigDecimal totalAmount
) {
    public static InstallmentResponse of(LoanInstallment installment) {
        return new InstallmentResponse(
                installment.getIdInstallment(),
                installment.getInstallmentNumber(),
                installment.getInstallmentStatus().toString(),
                installment.getDueDate(),
                installment.getPrincipalAmount(),
                installment.getInterestAmount(),
                installment.getTotalAmount()
        );
    }
}
