package com.schoolproject.app.repository;

import com.schoolproject.app.entity.RefreshToken;
import com.schoolproject.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshToken refreshToken
            set refreshToken.revokedAt = :revokedAt
            where refreshToken.id = :id
              and refreshToken.revokedAt is null
              and refreshToken.expiresAt > :now
            """)
    int revokeIfActive(
            @Param("id") Long id,
            @Param("revokedAt") LocalDateTime revokedAt,
            @Param("now") LocalDateTime now
    );

    void deleteByUser(User user);
}
