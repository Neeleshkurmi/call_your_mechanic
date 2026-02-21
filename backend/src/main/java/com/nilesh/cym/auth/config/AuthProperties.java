package com.nilesh.cym.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /** OTP validity window in seconds. */
    private long otpExpirySeconds = 300;

    /** Minimum delay between OTP request attempts for same mobile in seconds. */
    private long otpResendCooldownSeconds = 60;

    /** Maximum incorrect OTP submissions before challenge is locked. */
    private int otpMaxAttempts = 5;

    public long getOtpExpirySeconds() {
        return otpExpirySeconds;
    }

    public void setOtpExpirySeconds(long otpExpirySeconds) {
        this.otpExpirySeconds = otpExpirySeconds;
    }

    public long getOtpResendCooldownSeconds() {
        return otpResendCooldownSeconds;
    }

    public void setOtpResendCooldownSeconds(long otpResendCooldownSeconds) {
        this.otpResendCooldownSeconds = otpResendCooldownSeconds;
    }

    public int getOtpMaxAttempts() {
        return otpMaxAttempts;
    }

    public void setOtpMaxAttempts(int otpMaxAttempts) {
        this.otpMaxAttempts = otpMaxAttempts;
    }
}
