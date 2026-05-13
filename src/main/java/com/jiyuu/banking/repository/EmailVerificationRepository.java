package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByCode(String code);

    @Query("DELETE FROM EmailVerification t WHERE t.expiresAt < :now")
    void deleteAllByExpiredCode(LocalDateTime now);
}
