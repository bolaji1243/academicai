package com.schoolproject.app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Getter
@Setter
@NoArgsConstructor
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_key", nullable = false, unique = true, length = 160)
    private String key;

    @Column(nullable = false)
    private int failures;

    @Column(nullable = false)
    private LocalDateTime lastFailureAt;

    public LoginAttempt(String key) {
        this.key = key;
        this.failures = 0;
        this.lastFailureAt = LocalDateTime.now();
    }

    public boolean isLocked(int maxAttempts, LocalDateTime lockThreshold) {
        return failures >= maxAttempts && lastFailureAt.isAfter(lockThreshold);
    }

    public boolean isExpired(LocalDateTime lockThreshold) {
        return lastFailureAt.isBefore(lockThreshold);
    }
}
