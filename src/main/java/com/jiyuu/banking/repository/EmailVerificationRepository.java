package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.EmailVerification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByCode(String code);

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime now);
}
