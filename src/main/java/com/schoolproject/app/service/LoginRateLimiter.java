package com.schoolproject.app.service;

import com.schoolproject.app.entity.LoginAttempt;
import com.schoolproject.app.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class LoginRateLimiter {

    private final LoginAttemptRepository loginAttemptRepository;
    private final int maxAttempts;
    private final long lockMinutes;

    public LoginRateLimiter(
            LoginAttemptRepository loginAttemptRepository,
            @Value("${app.security.login.max-attempts:5}") int maxAttempts,
            @Value("${app.security.login.lock-minutes:15}") long lockMinutes
    ) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.maxAttempts = maxAttempts;
        this.lockMinutes = lockMinutes;
    }

    @Transactional
    public void checkAllowed(String key) {
        loginAttemptRepository.findByKey(key).ifPresent(attempt -> {
            LocalDateTime lockThreshold = LocalDateTime.now().minusMinutes(lockMinutes);

            if (attempt.isLocked(maxAttempts, lockThreshold)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts. Try again later");
            }

            if (attempt.isExpired(lockThreshold)) {
                loginAttemptRepository.delete(attempt);
            }
        });
    }

    @Transactional
    public void recordFailure(String key) {
        LoginAttempt attempt = loginAttemptRepository.findByKey(key)
                .orElseGet(() -> new LoginAttempt(key));

        attempt.setFailures(attempt.getFailures() + 1);
        attempt.setLastFailureAt(LocalDateTime.now());
        loginAttemptRepository.save(attempt);
    }

    @Transactional
    public void recordSuccess(String key) {
        loginAttemptRepository.deleteByKey(key);
    }
}
