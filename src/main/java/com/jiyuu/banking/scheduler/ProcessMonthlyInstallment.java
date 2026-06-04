package com.jiyuu.banking.scheduler;

import com.jiyuu.banking.dto.TransactionRequest;
import com.jiyuu.banking.entity.LoanInstallment;
import com.jiyuu.banking.enums.InstallmentStatus;
import com.jiyuu.banking.repository.LoanInstallmentRepository;
import com.jiyuu.banking.service.TransactionsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ProcessMonthlyInstallment {
    private final TransactionsService transactionsService;
    private final LoanInstallmentRepository installmentRepository;

    @Scheduled(cron = "0 * * * * *")
    public void collectMonthlyDue() {
        log.info("Début du prélèvement de mensualité");
        List<LoanInstallment> installments = this.installmentRepository
                .findByDueDateAndInstallmentStatus(LocalDate.now(), InstallmentStatus.PENDING);

        for (LoanInstallment installment : installments) {
            TransactionRequest request = TransactionRequest.builder()
                    .amount(installment.getTotalAmount())
                    .currency("XOF")
                    .type("INTEREST")
                    .source(installment.getLoan().getAccount().getNumeroAccount())
                    .build();

            this.transactionsService.createTransaction(request, installment.getIdInstallment());
        }

        log.info("Fin du prélèvement de mensualité");
    }
}
