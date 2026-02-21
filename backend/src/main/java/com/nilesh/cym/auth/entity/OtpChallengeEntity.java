package com.nilesh.cym.auth.entity;

import com.nilesh.cym.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "otp_challenges", indexes = {
        @Index(name = "idx_otp_challenges_mobile", columnList = "mobile"),
        @Index(name = "idx_otp_challenges_expires", columnList = "expiresAt"),
        @Index(name = "idx_otp_challenges_consumed", columnList = "consumed")
})
public class OtpChallengeEntity extends AuditableEntity {

    @Column(nullable = false, length = 20)
    private String mobile;

    @Column(nullable = false, length = 128)
    private String otpHash;

    @Column(nullable = false, length = 64)
    private String salt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant cooldownUntil;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private boolean consumed;

    @Column
    private Instant consumedAt;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCooldownUntil() {
        return cooldownUntil;
    }

    public void setCooldownUntil(Instant cooldownUntil) {
        this.cooldownUntil = cooldownUntil;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }
}
