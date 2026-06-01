package com.jiyuu.banking.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    public void sendTransactionReport(String to, byte[] pdf) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("no-reply@jiyuu.com");
        helper.setTo(to);
        helper.setSubject("Relevé de transactions");
        helper.setText("Bonjour, voici le relevé de vos transactions");

        ByteArrayResource resource = new ByteArrayResource(pdf);
        helper.addAttachment("transactions-report.pdf", resource);

        javaMailSender.send(message);
    }
}
