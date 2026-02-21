package com.nilesh.cym.token;

import com.nilesh.cym.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_jti", columnList = "tokenJti", unique = true),
        @Index(name = "idx_refresh_tokens_user", columnList = "userId")
})
public class RefreshTokenEntity extends AuditableEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenJti;

    @Column(nullable = false, length = 64)
    private String deviceSession;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(length = 64)
    private String replacedByJti;

    private Instant revokedAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTokenJti() {
        return tokenJti;
    }

    public void setTokenJti(String tokenJti) {
        this.tokenJti = tokenJti;
    }

    public String getDeviceSession() {
        return deviceSession;
    }

    public void setDeviceSession(String deviceSession) {
        this.deviceSession = deviceSession;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public String getReplacedByJti() {
        return replacedByJti;
    }

    public void setReplacedByJti(String replacedByJti) {
        this.replacedByJti = replacedByJti;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
