package com.jiyuu.banking.scheduler;

import com.jiyuu.banking.service.CardService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class ProcessExpiredCards {
    private final CardService cardService;

    @Scheduled(cron = "0 0 0 * * *")
    public void expireOutdatedCards() {
        log.info("Début du marquage des cartes expirées");
        this.cardService.markCardsAsExpired();
        log.info("Fin du marquage des cartes expirée");
    }
}
