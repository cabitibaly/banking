package com.jiyuu.banking.scheduler;

import com.jiyuu.banking.dto.TransactionRequest;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.enums.AccountStatus;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.service.AccountService;
import com.jiyuu.banking.service.TransactionsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class FeeApplication {
    private final AccountService accountService;

    @Scheduled(cron = "0 0 0 * * *")
    public void feeApplication() {
        log.info("Application des frais");
        this.accountService.collectFee();
        log.info("Terminé");
    }
}
