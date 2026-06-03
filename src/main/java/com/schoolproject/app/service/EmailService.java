package com.schoolproject.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    @Value("${app.public-url:http://localhost:8081}")
    private String publicUrl;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.email.from:no-reply@academicai.local}")
    private String fromAddress;

    public void sendVerificationEmail(String email, String token) {
        String link = publicUrl + "/verify-email?token=" + token;
        sendEmail(
                email,
                "Verify your AcademicAI account",
                "Welcome to AcademicAI.\n\nVerify your account using this link:\n" + link
        );
    }

    public void sendPasswordResetEmail(String email, String token) {
        String link = publicUrl + "/reset-password?token=" + token;
        sendEmail(
                email,
                "Reset your AcademicAI password",
                "Reset your password using this link:\n" + link
        );
    }

    private void sendEmail(String to, String subject, String body) {
        if (!emailEnabled || mailSender == null) {
            log.info("Email disabled. Suppression sending to: {}, Subject: {}", to, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.warn("Failed to send email. To: {}, Subject: {}", to, subject, exception);
        }
    }
}
