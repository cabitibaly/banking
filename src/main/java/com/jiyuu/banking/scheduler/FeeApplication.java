package com.jiyuu.banking.scheduler;

import com.jiyuu.banking.dto.TransactionRequest;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.enums.AccountStatus;
import com.jiyuu.banking.repository.AccountRepository;
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
    private final TransactionsService transactionsService;
    private final AccountRepository accountRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void feeApplication() {
        log.info("Application des frais");
        List<Account> accounts = this.accountRepository.findAll();

        for (Account account : accounts) {

            if (account.getAccountStatus() == AccountStatus.CLOSED) {
                continue;
            }

            TransactionRequest request = TransactionRequest.builder()
                    .amount(BigDecimal.valueOf(500))
                    .currency("XOF")
                    .type("FEE")
                    .source(account.getNumeroAccount())
                    .build();

            this.transactionsService.createTransaction(request, null);
        }

        log.info("Terminé");
    }
}
