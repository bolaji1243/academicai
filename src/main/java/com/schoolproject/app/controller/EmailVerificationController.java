package com.schoolproject.app.controller;

import com.schoolproject.app.dto.VerifyEmailRequest;
import com.schoolproject.app.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class EmailVerificationController {

    private final AuthService authService;

    @GetMapping("/verify-email")
    public void verifyEmailFromLink(@RequestParam String token, HttpServletResponse response) throws IOException {
        try {
            VerifyEmailRequest request = new VerifyEmailRequest();
            request.setToken(token);
            authService.verifyEmail(request);
            response.sendRedirect("https://academicaifrontend-academic-ai-qn1b.vercel.app/verify-email/success");
        } catch (ResponseStatusException e) {
            response.sendRedirect("https://academicaifrontend-academic-ai-qn1b.vercel.app/verify-email/error");
        }
    }
}
