package com.schoolproject.app.controller;

import com.schoolproject.app.dto.AuthResponse;
import com.schoolproject.app.dto.ForgotPasswordRequest;
import com.schoolproject.app.dto.GoogleLoginRequest;
import com.schoolproject.app.dto.LoginRequest;
import com.schoolproject.app.dto.MessageResponse;
import com.schoolproject.app.dto.LecturerRegisterRequest;
import com.schoolproject.app.dto.RegisterRequest;
import com.schoolproject.app.dto.ResetPasswordRequest;
import com.schoolproject.app.dto.TokenRefreshRequest;
import com.schoolproject.app.dto.UserResponse;
import com.schoolproject.app.dto.VerifyEmailRequest;
import com.schoolproject.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/register-lecturer")
    public MessageResponse registerLecturer(@Valid @RequestBody LecturerRegisterRequest request) {
        return authService.registerLecturer(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.googleLogin(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public MessageResponse logout(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.logout(request);
    }

    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }
}
