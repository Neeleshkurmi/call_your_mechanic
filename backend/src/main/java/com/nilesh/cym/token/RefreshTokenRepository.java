package com.nilesh.cym.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenJti(String tokenJti);

    Optional<RefreshTokenEntity> findByTokenJtiAndRevokedFalseAndExpiresAtAfter(String tokenJti, Instant now);

    java.util.List<RefreshTokenEntity> findByUserIdAndRevokedFalseAndExpiresAtAfter(Long userId, Instant now);
}
