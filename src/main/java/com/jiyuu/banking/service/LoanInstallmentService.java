package com.jiyuu.banking.service;

import com.jiyuu.banking.dto.InstallmentResponse;
import com.jiyuu.banking.dto.PagedResponse;
import com.jiyuu.banking.entity.Loan;
import com.jiyuu.banking.entity.LoanInstallment;
import com.jiyuu.banking.enums.InstallmentStatus;
import com.jiyuu.banking.repository.LoanInstallmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class LoanInstallmentService {
    private final LoanInstallmentRepository installmentRepository;

    public void generateInstallment(Loan loan) {
        List<LoanInstallment> installments = new ArrayList<>();

        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        int n = loan.getDuration();

        // M = C × i / (1 - (1 + i)^-n)
        BigDecimal onePlusI = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusIPowN = onePlusI.pow(n);
        BigDecimal denominator = BigDecimal.ONE
                .subtract(BigDecimal.ONE.divide(onePlusIPowN, 10, RoundingMode.HALF_UP));

        BigDecimal monthlyPayment = loan.getAmount()
                .multiply(monthlyRate)
                .divide(denominator, 2, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = loan.getAmount();

        for (int i = 1; i <= n; i++) {
            BigDecimal interest = remainingBalance
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principal = monthlyPayment.subtract(interest);

            LoanInstallment installment = LoanInstallment.builder()
                    .installmentNumber(i)
                    .principalAmount(principal)
                    .interestAmount(interest)
                    .totalAmount(monthlyPayment)
                    .dueDate(loan.getDisbursementDate().plusMonths(i))
                    .installmentStatus(InstallmentStatus.PENDING)
                    .loan(loan)
                    .build();

            installments.add(installment);
            remainingBalance = remainingBalance.subtract(principal);
        }

        this.installmentRepository.saveAll(installments);
    }

    public PagedResponse<InstallmentResponse> scheduleInstallment(long idLoan, int page, int size, String sort, String direction) {
        Sort sortBy = direction.equalsIgnoreCase("desc")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();

        Pageable pageable = PageRequest.of(page, size, sortBy);

        Page<InstallmentResponse> installments = this.installmentRepository
                .findAllByLoan_IdLoan(idLoan,pageable)
                .map(InstallmentResponse::of);

        return PagedResponse.of(installments);
    }
}
