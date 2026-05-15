package com.jiyuu.banking.scheduler;

import com.jiyuu.banking.repository.EmailVerificationRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@AllArgsConstructor
public class DeleteExipreCode {
    private final EmailVerificationRepository emailVerificationRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteAllExpiredCode() {
        log.info("Suppression des codes expirés");
        this.emailVerificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
