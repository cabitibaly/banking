package com.jiyuu.banking.service;

import com.jiyuu.banking.entity.EmailVerification;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.EmailVerificationRepository;
import com.jiyuu.banking.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final NotificationSender notificationSender;

    public String generateCode() {
        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        return String.format("%06d", randomInteger);
    }

    public void createNewCode(User user) {
        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setUser(user);
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
        emailVerification.setExpiresAt(expiredAt);

        String code = this.generateCode();
        emailVerification.setCode(code);

        this.emailVerificationRepository.save(emailVerification);
        this.notificationSender.sendMail(user.getEmail(), code);
    }

    public void resendCode(String email) {
        User user = this.userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        this.createNewCode(user);
    }

    public EmailVerification readByCode(String code) {
        return this.emailVerificationRepository
                .findByCode(code)
                .orElseThrow(() -> new ValidationException("Le code fourni n'est pas valide"));
    }
}
