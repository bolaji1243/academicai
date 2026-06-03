package com.schoolproject.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.schoolproject.app.enums.AuthProvider;
import com.schoolproject.app.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(unique = true, nullable = false, updatable = false, length = 36)
    private String publicId;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(unique = true, nullable = false, length = 160)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(unique = true, length = 128)
    private String googleSubject;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private LecturerProfile lecturerProfile;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private UniversityStudentProfile universityStudentProfile;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean locked;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    @Column(length = 64)
    private String emailVerificationTokenHash;

    private LocalDateTime emailVerificationExpiresAt;

    @Column(length = 64)
    private String passwordResetTokenHash;

    private LocalDateTime passwordResetExpiresAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
