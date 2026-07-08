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

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${app.email.from:no-reply@academicai.local}")
    private String emailFrom;

    @Value("${app.frontend-url:https://academicaifrontend-academic-ai-qn1b.vercel.app}")
    private String frontendUrl;

    @Value("${app.public-url:http://localhost:8080}")
    private String publicUrl;

    public void sendVerificationEmail(String email, String token) {
        String link = publicUrl + "/verify-email?token=" + token;
        String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 0;">
                <tr><td align="center">
                <table width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                <tr><td style="padding:40px 32px 0 32px;text-align:center;">
                <h1 style="margin:0 0 8px 0;font-size:22px;color:#1a1a2e;font-weight:700;">AcademicAI</h1>
                <p style="margin:0 0 24px 0;font-size:15px;color:#6b7280;">Verify your email address</p>
                </td></tr>
                <tr><td style="padding:0 32px 24px 32px;">
                <p style="margin:0 0 16px 0;font-size:15px;color:#374151;line-height:1.6;">Hi there,</p>
                <p style="margin:0 0 24px 0;font-size:15px;color:#374151;line-height:1.6;">Thanks for joining AcademicAI! Click the button below to verify your account and get started.</p>
                <table cellpadding="0" cellspacing="0" style="margin:0 auto 24px auto;">
                <tr><td style="background-color:#4f46e5;border-radius:8px;text-align:center;">
                <a href="%s" style="display:inline-block;padding:14px 40px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;">Verify your email</a>
                </td></tr>
                </table>
                <p style="margin:0 0 8px 0;font-size:13px;color:#9ca3af;line-height:1.5;">If the button doesn't work, copy and paste this link in your browser:</p>
                <p style="margin:0 0 0 0;font-size:13px;color:#6b7280;word-break:break-all;line-height:1.5;">%s</p>
                </td></tr>
                <tr><td style="padding:24px 32px 32px 32px;border-top:1px solid #e5e7eb;text-align:center;">
                <p style="margin:0;font-size:13px;color:#9ca3af;">If you didn't create an account, you can safely ignore this email.</p>
                </td></tr>
                </table>
                </td></tr>
                </table>
                </body>
                </html>
                """.formatted(link, link);
        sendEmail(email, "Verify your AcademicAI account", html);
    }

    public void sendPasswordResetEmail(String email, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 0;">
                <tr><td align="center">
                <table width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                <tr><td style="padding:40px 32px 0 32px;text-align:center;">
                <h1 style="margin:0 0 8px 0;font-size:22px;color:#1a1a2e;font-weight:700;">AcademicAI</h1>
                <p style="margin:0 0 24px 0;font-size:15px;color:#6b7280;">Reset your password</p>
                </td></tr>
                <tr><td style="padding:0 32px 24px 32px;">
                <p style="margin:0 0 16px 0;font-size:15px;color:#374151;line-height:1.6;">Hi there,</p>
                <p style="margin:0 0 24px 0;font-size:15px;color:#374151;line-height:1.6;">We received a request to reset your AcademicAI password. Click the button below to choose a new one.</p>
                <table cellpadding="0" cellspacing="0" style="margin:0 auto 24px auto;">
                <tr><td style="background-color:#4f46e5;border-radius:8px;text-align:center;">
                <a href="%s" style="display:inline-block;padding:14px 40px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;">Reset password</a>
                </td></tr>
                </table>
                <p style="margin:0 0 8px 0;font-size:13px;color:#9ca3af;line-height:1.5;">If the button doesn't work, copy and paste this link in your browser:</p>
                <p style="margin:0 0 0 0;font-size:13px;color:#6b7280;word-break:break-all;line-height:1.5;">%s</p>
                </td></tr>
                <tr><td style="padding:24px 32px 32px 32px;border-top:1px solid #e5e7eb;text-align:center;">
                <p style="margin:0;font-size:13px;color:#9ca3af;">If you didn't request a password reset, you can safely ignore this email.</p>
                </td></tr>
                </table>
                </td></tr>
                </table>
                </body>
                </html>
                """.formatted(link, link);
        sendEmail(email, "Reset your AcademicAI password", html);
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
                    "sender", Map.of("name", "AcademicAI", "email", emailFrom),
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
