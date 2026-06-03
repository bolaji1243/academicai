package com.schoolproject.app.service;

import com.schoolproject.app.dto.AuthResponse;
import com.schoolproject.app.dto.GoogleLoginRequest;
import com.schoolproject.app.dto.GoogleUserInfo;
import com.schoolproject.app.dto.LecturerRegisterRequest;
import com.schoolproject.app.dto.LoginRequest;
import com.schoolproject.app.dto.RegisterRequest;
import com.schoolproject.app.dto.TokenRefreshRequest;
import com.schoolproject.app.entity.LecturerProfile;
import com.schoolproject.app.entity.RefreshToken;
import com.schoolproject.app.entity.UniversityStudentProfile;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.enums.Role;
import com.schoolproject.app.repository.LecturerProfileRepository;
import com.schoolproject.app.repository.RefreshTokenRepository;
import com.schoolproject.app.repository.UniversityStudentProfileRepository;
import com.schoolproject.app.repository.UserRepository;
import com.schoolproject.app.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LecturerProfileRepository lecturerProfileRepository;

    @Mock
    private UniversityStudentProfileRepository universityStudentProfileRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SecureTokenService secureTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    @Mock
    private GoogleTokenService googleTokenService;

    @Mock
    private AuditService auditService;

    @Mock
    private RequestMetadataService requestMetadataService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                lecturerProfileRepository,
                universityStudentProfileRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtTokenProvider,
                secureTokenService,
                emailService,
                loginRateLimiter,
                googleTokenService,
                auditService,
                requestMetadataService
        );
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationDays", 30L);
        ReflectionTestUtils.setField(authService, "emailVerificationExpirationMinutes", 30L);
        ReflectionTestUtils.setField(authService, "passwordResetExpirationMinutes", 15L);
        ReflectionTestUtils.setField(authService, "lecturerRegistrationCode", "test-lecturer-code");
    }

    @Test
    void registerCreatesDisabledUserAndSendsVerificationEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ada Lovelace");
        request.setEmail("ADA@example.com");
        request.setPassword("password123");
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(secureTokenService.generateToken()).thenReturn("verify-token");
        when(secureTokenService.hash("verify-token")).thenReturn("verify-hash");
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("ada@example.com", savedUser.getEmail());
        assertEquals(36, savedUser.getPublicId().length());
        assertEquals(Role.ASPIRING_STUDENT, savedUser.getRole());
        assertFalse(savedUser.isEnabled());
        assertEquals("verify-hash", savedUser.getEmailVerificationTokenHash());
        verify(emailService).sendVerificationEmail("ada@example.com", "verify-token");
    }

    @Test
    void registerCanCreateUniversityStudent() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Alan Turing");
        request.setEmail("ALAN@example.com");
        request.setPassword("password123");
        request.setRole(Role.UNIVERSITY_STUDENT);
        request.setMatricNumber("MAT-001");
        request.setDepartment("Computer Science");
        request.setLevel("300");
        request.setFaculty("Science");

        when(userRepository.existsByEmail("alan@example.com")).thenReturn(false);
        when(universityStudentProfileRepository.existsByMatricNumber("MAT-001")).thenReturn(false);
        when(secureTokenService.generateToken()).thenReturn("verify-token");
        when(secureTokenService.hash("verify-token")).thenReturn("verify-hash");
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(universityStudentProfileRepository.save(any(UniversityStudentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals(Role.UNIVERSITY_STUDENT, userCaptor.getValue().getRole());
        verify(universityStudentProfileRepository).save(any(UniversityStudentProfile.class));
    }

    @Test
    void registerRejectsLecturerRole() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Grace Hopper");
        request.setEmail("grace@example.com");
        request.setPassword("password123");
        request.setRole(Role.LECTURER);

        assertThrows(ResponseStatusException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerLecturerCreatesLecturerWhenCodeIsValid() {
        LecturerRegisterRequest request = new LecturerRegisterRequest();
        request.setFullName("Grace Hopper");
        request.setEmail("GRACE@example.com");
        request.setPassword("password123");
        request.setDepartment("Computer Science");
        request.setFaculty("Science");
        request.setStaffId("STAFF-001");
        request.setLecturerRegistrationCode("test-lecturer-code");

        when(userRepository.existsByEmail("grace@example.com")).thenReturn(false);
        when(lecturerProfileRepository.existsByStaffId("STAFF-001")).thenReturn(false);
        when(secureTokenService.generateToken()).thenReturn("verify-token");
        when(secureTokenService.hash("verify-token")).thenReturn("verify-hash");
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(lecturerProfileRepository.save(any(LecturerProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerLecturer(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("grace@example.com", savedUser.getEmail());
        assertEquals(Role.LECTURER, savedUser.getRole());
        assertFalse(savedUser.isEnabled());
        verify(lecturerProfileRepository).save(any(LecturerProfile.class));
        verify(emailService).sendVerificationEmail("grace@example.com", "verify-token");
    }

    @Test
    void registerLecturerRejectsInvalidCode() {
        LecturerRegisterRequest request = new LecturerRegisterRequest();
        request.setFullName("Grace Hopper");
        request.setEmail("grace@example.com");
        request.setPassword("password123");
        request.setDepartment("Computer Science");
        request.setFaculty("Science");
        request.setStaffId("STAFF-001");
        request.setLecturerRegistrationCode("wrong-code");

        assertThrows(ResponseStatusException.class, () -> authService.registerLecturer(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginReturnsAccessAndRefreshTokens() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ada@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .publicId("ABC123XYZ987LMNO")
                .email("ada@example.com")
                .password("hashed-password")
                .role(Role.ASPIRING_STUDENT)
                .enabled(true)
                .locked(false)
                .build();

        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtTokenProvider.generateToken("ada@example.com")).thenReturn("access-token");
        when(secureTokenService.generateToken()).thenReturn("refresh-token");
        when(secureTokenService.hash("refresh-token")).thenReturn("refresh-hash");

        AuthResponse response = authService.login(request);

        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("ABC123XYZ987LMNO", response.getUserId());
        assertEquals(Role.ASPIRING_STUDENT, response.getRole());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(loginRateLimiter).recordSuccess("ada@example.com");
    }

    @Test
    void loginRejectsWrongPasswordAndRecordsFailure() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ada@example.com");
        request.setPassword("wrong-password");

        User user = User.builder()
                .email("ada@example.com")
                .password("hashed-password")
                .role(Role.ASPIRING_STUDENT)
                .enabled(true)
                .locked(false)
                .build();

        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> authService.login(request));
        verify(loginRateLimiter).recordFailure("ada@example.com");
    }

    @Test
    void googleLoginCreatesEnabledUserAndReturnsTokens() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("google-id-token");

        when(googleTokenService.verify("google-id-token"))
                .thenReturn(new GoogleUserInfo("google-subject-1", "ada@example.com", "Ada Lovelace"));
        when(secureTokenService.hash("google-id-token")).thenReturn("google-token-hash");
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByGoogleSubject("google-subject-1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("generated-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateToken("ada@example.com")).thenReturn("access-token");
        when(secureTokenService.generateToken()).thenReturn("refresh-token");
        when(secureTokenService.hash("refresh-token")).thenReturn("refresh-hash");

        AuthResponse response = authService.googleLogin(request);

        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(Role.ASPIRING_STUDENT, response.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());
        User savedUser = userCaptor.getAllValues().get(0);
        assertEquals("ada@example.com", savedUser.getEmail());
        assertEquals("Ada Lovelace", savedUser.getFullName());
        assertTrue(savedUser.isEnabled());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshRotatesTokenWhenAtomicRevokeSucceeds() {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("old-refresh-token");

        User user = User.builder()
                .publicId("ABC123XYZ987LMNO")
                .email("ada@example.com")
                .role(Role.ASPIRING_STUDENT)
                .enabled(true)
                .locked(false)
                .build();
        RefreshToken existingRefreshToken = RefreshToken.builder()
                .id(42L)
                .user(user)
                .tokenHash("old-refresh-hash")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(secureTokenService.hash("old-refresh-token")).thenReturn("old-refresh-hash");
        when(refreshTokenRepository.findByTokenHash("old-refresh-hash"))
                .thenReturn(Optional.of(existingRefreshToken));
        when(refreshTokenRepository.revokeIfActive(eq(42L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1);
        when(jwtTokenProvider.generateToken("ada@example.com")).thenReturn("new-access-token");
        when(secureTokenService.generateToken()).thenReturn("new-refresh-token");
        when(secureTokenService.hash("new-refresh-token")).thenReturn("new-refresh-hash");

        AuthResponse response = authService.refresh(request);

        assertEquals("new-access-token", response.getToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(refreshTokenRepository).revokeIfActive(eq(42L), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(refreshTokenRepository, never()).deleteByUser(any(User.class));
    }

    @Test
    void refreshRejectsTokenWhenAtomicRevokeFailsWithoutDeletingUserTokens() {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("old-refresh-token");

        User user = User.builder()
                .publicId("ABC123XYZ987LMNO")
                .email("ada@example.com")
                .role(Role.ASPIRING_STUDENT)
                .enabled(true)
                .locked(false)
                .build();
        RefreshToken existingRefreshToken = RefreshToken.builder()
                .id(42L)
                .user(user)
                .tokenHash("old-refresh-hash")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(secureTokenService.hash("old-refresh-token")).thenReturn("old-refresh-hash");
        when(refreshTokenRepository.findByTokenHash("old-refresh-hash"))
                .thenReturn(Optional.of(existingRefreshToken));
        when(refreshTokenRepository.revokeIfActive(eq(42L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0);

        assertThrows(ResponseStatusException.class, () -> authService.refresh(request));
        verify(refreshTokenRepository, never()).deleteByUser(any(User.class));
        verify(jwtTokenProvider, never()).generateToken(any());
    }
}
