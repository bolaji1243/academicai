package com.schoolproject.app.service;

import com.schoolproject.app.dto.AuthResponse;
import com.schoolproject.app.dto.ForgotPasswordRequest;
import com.schoolproject.app.dto.GoogleLoginRequest;
import com.schoolproject.app.dto.GoogleUserInfo;
import com.schoolproject.app.dto.LecturerRegisterRequest;
import com.schoolproject.app.dto.LoginRequest;
import com.schoolproject.app.dto.MessageResponse;
import com.schoolproject.app.dto.RegisterAspiringStudentRequest;
import com.schoolproject.app.dto.RegisterRequest;
import com.schoolproject.app.dto.ResetPasswordRequest;
import com.schoolproject.app.dto.TokenRefreshRequest;
import com.schoolproject.app.dto.UserResponse;
import com.schoolproject.app.dto.VerifyEmailRequest;
import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.entity.RefreshToken;
import com.schoolproject.app.entity.UniversityStudentProfile;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.enums.AuditEventType;
import com.schoolproject.app.enums.AuthProvider;
import com.schoolproject.app.enums.Level;
import com.schoolproject.app.enums.Role;
import com.schoolproject.app.enums.Semester;
import com.schoolproject.app.repository.LecturerProfileRepository;
import com.schoolproject.app.repository.RefreshTokenRepository;
import com.schoolproject.app.repository.UniversityStudentProfileRepository;
import com.schoolproject.app.repository.UserRepository;
import com.schoolproject.app.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final UniversityStudentProfileRepository universityStudentProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureTokenService secureTokenService;
    private final EmailService emailService;
    private final LoginRateLimiter loginRateLimiter;
    private final GoogleTokenService googleTokenService;
    private final AuditService auditService;
    private final RequestMetadataService requestMetadataService;

    @Value("${app.refresh-token.expiration-days:30}")
    private long refreshTokenExpirationDays;

    @Value("${app.email-verification.expiration-minutes:30}")
    private long emailVerificationExpirationMinutes;

    @Value("${app.password-reset.expiration-minutes:15}")
    private long passwordResetExpirationMinutes;

    @Value("${app.lecturer-registration.code:}")
    private String lecturerRegistrationCode;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already taken");
        }

        Role role = getStudentRole(request);
        validateStudentProfileRequest(request, role);

        try {
            User user = createUnverifiedUser(
                    request.getFullName(),
                    email,
                    request.getPassword(),
                    role
            );

            if (role == Role.UNIVERSITY_STUDENT) {
                createUniversityStudentProfile(user, request);
            }

            log.info("Registered user {} with role {}", user.getPublicId(), user.getRole());
            auditService.record(AuditEventType.REGISTER, user, user.getEmail(), "Local user registered");

            return new MessageResponse("User registered successfully. Check your email to verify your account");
        } catch (DataIntegrityViolationException e) {
            log.warn("Conflict detected during student registration for email: {}", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email or unique identifier already taken");
        }
    }

    @Transactional
    public MessageResponse registerAspiringStudent(RegisterAspiringStudentRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already taken");
        }

        try {
            createUnverifiedUser(
                    request.getFullName(),
                    email,
                    request.getPassword(),
                    Role.ASPIRING_STUDENT
            );

            log.info("Registered aspiring student with email {}", email);
            auditService.record(AuditEventType.REGISTER, null, email, "Aspiring student registered");

            return new MessageResponse("User registered successfully. Check your email to verify your account");
        } catch (DataIntegrityViolationException e) {
            log.warn("Conflict detected during aspiring student registration for email: {}", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already taken");
        }
    }

    @Transactional
    public AuthResponse registerLecturer(LecturerRegisterRequest request) {
        if (lecturerRegistrationCode == null || lecturerRegistrationCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lecturer registration is not configured");
        }

        if (!lecturerRegistrationCode.trim().equals(request.getLecturerRegistrationCode().trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid lecturer registration code");
        }

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already taken");
        }

        String staffId = requireText(request.getStaffId(), "Staff ID is required");
        if (lecturerProfileRepository.existsByStaffId(staffId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Staff ID already taken");
        }

        try {
            User user = User.builder()
                    .publicId(generateUniquePublicId())
                    .fullName(request.getFullName().trim())
                    .email(email)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.LECTURER)
                    .authProvider(AuthProvider.LOCAL)
                    .enabled(true)
                    .locked(false)
                    .build();
            userRepository.save(user);
            createLecturerProfile(user, request, staffId);
            String accessToken = jwtTokenProvider.generateToken(user.getEmail());
            String refreshToken = createRefreshToken(user);
            log.info("Registered lecturer {} (email verification skipped)", user.getPublicId());
            auditService.record(AuditEventType.REGISTER, user, user.getEmail(), "Lecturer registered");
            return new AuthResponse(
                    accessToken,
                    refreshToken,
                    user.getPublicId(),
                    user.getRole(),
                    "Lecturer registered successfully"
            );
        } catch (DataIntegrityViolationException e) {
            log.warn("Conflict detected during lecturer registration for email: {}", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email or Staff ID already taken");
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        loginRateLimiter.checkAllowed(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    loginRateLimiter.recordFailure(email);
                    auditService.record(AuditEventType.LOGIN_FAILURE, null, email, "User not found");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        if (!user.isEnabled() || user.isLocked()) {
            auditService.record(AuditEventType.LOGIN_FAILURE, user, email, "Account is not available");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not available");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginRateLimiter.recordFailure(email);
            log.warn("Failed login attempt for {}", email);
            auditService.record(AuditEventType.LOGIN_FAILURE, user, email, "Wrong password");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        loginRateLimiter.recordSuccess(email);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = createRefreshToken(user);
        log.info("User {} logged in", user.getPublicId());
        auditService.record(AuditEventType.LOGIN_SUCCESS, user, user.getEmail(), "Password login successful");

        return new AuthResponse(accessToken, refreshToken, user.getPublicId(), user.getRole(), "Login successful");
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        String tokenAttemptKey = "google-token:" + secureTokenService.hash(request.getIdToken());
        loginRateLimiter.checkAllowed(tokenAttemptKey);

        GoogleUserInfo googleUser;
        try {
            googleUser = googleTokenService.verify(request.getIdToken());
        } catch (ResponseStatusException ex) {
            loginRateLimiter.recordFailure(tokenAttemptKey);
            auditService.record(AuditEventType.GOOGLE_LOGIN_FAILURE, null, null, ex.getReason());
            throw ex;
        }

        String emailAttemptKey = "google:" + googleUser.email();
        loginRateLimiter.checkAllowed(emailAttemptKey);

        User user;
        try {
            user = resolveGoogleUser(googleUser);
        } catch (ResponseStatusException ex) {
            loginRateLimiter.recordFailure(emailAttemptKey);
            auditService.record(AuditEventType.GOOGLE_LOGIN_FAILURE, null, googleUser.email(), ex.getReason());
            throw ex;
        }

        if (user.isLocked()) {
            loginRateLimiter.recordFailure(emailAttemptKey);
            auditService.record(AuditEventType.GOOGLE_LOGIN_FAILURE, user, user.getEmail(), "Account is locked");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not available");
        }

        loginRateLimiter.recordSuccess(tokenAttemptKey);
        loginRateLimiter.recordSuccess(emailAttemptKey);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = createRefreshToken(user);
        log.info("User {} logged in with Google", user.getPublicId());
        auditService.record(AuditEventType.GOOGLE_LOGIN_SUCCESS, user, user.getEmail(), "Google login successful");

        return new AuthResponse(accessToken, refreshToken, user.getPublicId(), user.getRole(), "Google login successful");
    }

    @Transactional
    public AuthResponse refresh(TokenRefreshRequest request) {
        String tokenHash = secureTokenService.hash(request.getRefreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        User user = refreshToken.getUser();
        LocalDateTime now = LocalDateTime.now();
        int revokedCount = refreshTokenRepository.revokeIfActive(refreshToken.getId(), now, now);
        if (revokedCount != 1) {
            log.warn("Rejected inactive refresh token for user {}", user.getPublicId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String rotatedRefreshToken = createRefreshToken(user);
        auditService.record(AuditEventType.TOKEN_REFRESHED, user, user.getEmail(), "Refresh token rotated");

        return new AuthResponse(accessToken, rotatedRefreshToken, user.getPublicId(), user.getRole(), "Token refreshed");
    }

    @Transactional
    public MessageResponse logout(TokenRefreshRequest request) {
        refreshTokenRepository.findByTokenHash(secureTokenService.hash(request.getRefreshToken()))
                .ifPresent(refreshToken -> {
                    LocalDateTime now = LocalDateTime.now();
                    refreshTokenRepository.revokeIfActive(refreshToken.getId(), now, now);
                    auditService.record(AuditEventType.LOGOUT, refreshToken.getUser(), refreshToken.getUser().getEmail(), "Refresh token revoked");
                });

        return new MessageResponse("Logged out successfully");
    }

    @Transactional
    public MessageResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmailVerificationTokenHash(secureTokenService.hash(request.getToken()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (user.getEmailVerificationExpiresAt() == null
                || user.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token expired");
        }

        user.setEnabled(true);
        user.setEmailVerificationTokenHash(null);
        user.setEmailVerificationExpiresAt(null);
        userRepository.save(user);

        log.info("Verified email for user {}", user.getPublicId());
        auditService.record(AuditEventType.EMAIL_VERIFIED, user, user.getEmail(), "Email verified");

        return new MessageResponse("Email verified successfully");
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = secureTokenService.generateToken();
            user.setPasswordResetTokenHash(secureTokenService.hash(resetToken));
            user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            auditService.record(AuditEventType.PASSWORD_RESET_REQUESTED, user, user.getEmail(), "Password reset requested");
        });

        return new MessageResponse("If the email exists, a password reset link has been sent");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetTokenHash(secureTokenService.hash(request.getToken()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset token"));

        if (user.getPasswordResetExpiresAt() == null
                || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(null);
        refreshTokenRepository.deleteByUser(user);
        userRepository.save(user);

        log.info("Password reset for user {}", user.getPublicId());
        auditService.record(AuditEventType.PASSWORD_RESET_COMPLETED, user, user.getEmail(), "Password reset completed");

        return new MessageResponse("Password reset successfully");
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return UserResponse.from(user);
    }

    private String createRefreshToken(User user) {
        String token = secureTokenService.generateToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(secureTokenService.hash(token))
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .ipAddress(requestMetadataService.getIpAddress())
                .userAgent(requestMetadataService.getUserAgent())
                .authProvider(user.getAuthProvider().name())
                .build();

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    private User createUnverifiedUser(
            String fullName,
            String email,
            String password,
            Role role
    ) {
        String verificationToken = secureTokenService.generateToken();

        User user = User.builder()
                .publicId(generateUniquePublicId())
                .fullName(fullName.trim())
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .authProvider(AuthProvider.LOCAL)
                .enabled(false)
                .locked(false)
                .emailVerificationTokenHash(secureTokenService.hash(verificationToken))
                .emailVerificationExpiresAt(LocalDateTime.now().plusMinutes(emailVerificationExpirationMinutes))
                .build();

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return user;
    }

    private User resolveGoogleUser(GoogleUserInfo googleUser) {
        return userRepository.findByGoogleSubject(googleUser.subject())
                .map(existingUser -> {
                    if (!existingUser.getEmail().equalsIgnoreCase(googleUser.email())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Google account is already linked to another email");
                    }

                    return updateGoogleUser(existingUser, googleUser);
                })
                .orElseGet(() -> userRepository.findByEmail(googleUser.email())
                        .map(existingUser -> updateGoogleUser(existingUser, googleUser))
                        .orElseGet(() -> createGoogleUser(googleUser)));
    }

    private User createGoogleUser(GoogleUserInfo googleUser) {
        User user = User.builder()
                .publicId(generateUniquePublicId())
                .fullName(googleUser.fullName())
                .email(googleUser.email())
                .googleSubject(googleUser.subject())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.ASPIRING_STUDENT)
                .authProvider(AuthProvider.GOOGLE)
                .enabled(true)
                .locked(false)
                .build();

        return userRepository.save(user);
    }

    private User updateGoogleUser(User user, GoogleUserInfo googleUser) {
        if (user.getGoogleSubject() != null && !user.getGoogleSubject().equals(googleUser.subject())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already linked to a different Google account");
        }

        user.setGoogleSubject(googleUser.subject());

        if (user.getAuthProvider() == AuthProvider.LOCAL) {
            user.setAuthProvider(AuthProvider.LOCAL_AND_GOOGLE);
        } else if (user.getAuthProvider() == null) {
            user.setAuthProvider(AuthProvider.GOOGLE);
        }

        if (!user.isEnabled()) {
            user.setEnabled(true);
            user.setEmailVerificationTokenHash(null);
            user.setEmailVerificationExpiresAt(null);
        }

        if (user.getFullName() == null || user.getFullName().isBlank()) {
            user.setFullName(googleUser.fullName());
        }

        return user;
    }

    private void createLecturerProfile(User user, LecturerRegisterRequest request, String staffId) {
        LecturerProfile profile = LecturerProfile.builder()
                .user(user)
                .department(requireText(request.getDepartment(), "Department is required"))
                .faculty(requireText(request.getFaculty(), "Faculty is required"))
                .staffId(staffId)
                .build();

        lecturerProfileRepository.save(profile);
        user.setLecturerProfile(profile);
    }

    private void createUniversityStudentProfile(User user, RegisterRequest request) {
        String matricNumber = requireText(request.getMatricNumber(), "Matric number is required");

        if (universityStudentProfileRepository.existsByMatricNumber(matricNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matric number already taken");
        }

        UniversityStudentProfile profile = UniversityStudentProfile.builder()
                .user(user)
                .fullName(user.getFullName())
                .matricNumber(matricNumber)
                .department(requireText(request.getDepartment(), "Department is required"))
                .level(Level.valueOf(requireText(request.getLevel(), "Level is required")))
                .faculty(requireText(request.getFaculty(), "Faculty is required"))
                .semester(request.getSemester())
                .session(requireText(request.getSession(), "Session is required"))
                .build();

        universityStudentProfileRepository.save(profile);
        user.setUniversityStudentProfile(profile);
    }

    private Role getStudentRole(RegisterRequest request) {
        if (request.getRole() == null) {
            return Role.ASPIRING_STUDENT;
        }

        if (request.getRole() == Role.ASPIRING_STUDENT || request.getRole() == Role.UNIVERSITY_STUDENT) {
            return request.getRole();
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Student role must be ASPIRING_STUDENT or UNIVERSITY_STUDENT"
        );
    }

    private void validateStudentProfileRequest(RegisterRequest request, Role role) {
        if (role == Role.ASPIRING_STUDENT) {
            return;
        }

        requireText(request.getMatricNumber(), "Matric number is required");
        requireText(request.getDepartment(), "Department is required");
        requireText(request.getLevel(), "Level is required");
        requireText(request.getFaculty(), "Faculty is required");

        if (request.getSemester() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semester is required");
        }
        requireText(request.getSession(), "Session is required");
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return value.trim();
    }

    private String generateUniquePublicId() {
        return UUID.randomUUID().toString();
    }
}
