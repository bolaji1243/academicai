package com.schoolproject.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate;

    @Value("${app.public-url:http://localhost:8081}")
    private String publicUrl;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    public void sendVerificationEmail(String email, String token) {
        String link = publicUrl + "/verify-email?token=" + token;
        sendEmail(
                email,
                "Verify your AcademicAI account",
                "<p>Welcome to AcademicAI.</p><p>Verify your account using this link:</p>" +
                "<p><a href=\"" + link + "\">" + link + "</a></p>"
        );
    }

    public void sendPasswordResetEmail(String email, String token) {
        String link = publicUrl + "/reset-password?token=" + token;
        sendEmail(
                email,
                "Reset your AcademicAI password",
                "<p>Reset your password using this link:</p>" +
                "<p><a href=\"" + link + "\">" + link + "</a></p>"
        );
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        if (!emailEnabled) {
            log.info("Email disabled. Skipping sending to: {}, Subject: {}", to, subject);
            return;
        }

        if (brevoApiKey.isBlank()) {
            log.warn("Brevo API key not configured. Skipping email to: {}", to);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", "AcademicAI", "email", "no-reply@academicai.com"),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "htmlContent", htmlContent
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email",
                    new HttpEntity<>(body, headers),
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent successfully to: {}, Subject: {}", to, subject);
            } else {
                log.warn("Brevo API returned non-2xx for to: {}, status: {}", to, response.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("Failed to send email via Brevo. To: {}, Subject: {}", to, subject, e);
        }
    }
}
