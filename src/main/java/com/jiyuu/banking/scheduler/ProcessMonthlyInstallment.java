package com.jiyuu.banking.scheduler;

import com.jiyuu.banking.entity.LoanInstallment;
import com.jiyuu.banking.enums.InstallmentStatus;
import com.jiyuu.banking.repository.LoanInstallmentRepository;
import com.jiyuu.banking.service.LoanService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ProcessMonthlyInstallment {
    private final LoanService loanService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void collectMonthlyDue() {
        log.info("Début du prélèvement de mensualité");
        this.loanService.processMonthlyInstallment();
        log.info("Fin du prélèvement de mensualité");
    }
}
