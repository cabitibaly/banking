package com.jiyuu.banking.service;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationSender {
    private final JavaMailSender javaMailSender;

    public void sendMail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@jiyuu.com");
        message.setTo(to);
        message.setSubject("Code de verification");

        String text = String.format(
                "Bonjour, veuillez cliquer sur le lien suivant pour activer votre compte : \nhttp://localhost:8080/api/v1/auth/activate?code=%s",
                code
        );

        message.setText(text);
        javaMailSender.send(message);
    }

    public void sendForgotPasswordMail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@jiyuu.com");
        message.setTo(to);
        message.setSubject("Réinitialisation du mot de passe");

        String text = String.format(
                "Bonjour, veuillez cliquer sur le lien suivant pour réinitialiser votre mot de passe : \nhttp://localhost:8080/api/v1/auth/reset-password?token=%s",
                token
        );
        message.setText(text);
        javaMailSender.send(message);
    }
}
