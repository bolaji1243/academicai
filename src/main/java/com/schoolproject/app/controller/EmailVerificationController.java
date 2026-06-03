package com.schoolproject.app.controller;

import com.schoolproject.app.dto.MessageResponse;
import com.schoolproject.app.dto.VerifyEmailRequest;
import com.schoolproject.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailVerificationController {

    private final AuthService authService;

    @GetMapping("/verify-email")
    public MessageResponse verifyEmailFromLink(@RequestParam String token) {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken(token);

        return authService.verifyEmail(request);
    }
}
