package com.schoolproject.app.repository;

import com.schoolproject.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPublicId(String publicId);

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleSubject(String googleSubject);

    Optional<User> findByEmailVerificationTokenHash(String tokenHash);

    Optional<User> findByPasswordResetTokenHash(String tokenHash);

    boolean existsByEmail(String email);
}
