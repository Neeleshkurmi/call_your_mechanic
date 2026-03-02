package com.nilesh.cym.auth.service;

import com.nilesh.cym.auth.config.AuthProperties;
import com.nilesh.cym.auth.dto.AuthTokenResponseDto;
import com.nilesh.cym.auth.dto.OtpVerifyDto;
import com.nilesh.cym.auth.entity.OtpChallengeEntity;
import com.nilesh.cym.auth.repository.OtpChallengeRepository;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class OtpAuthService {

    @Value("${authToken}")
    private String authToken;

    @Value("${fromNumber}")
    private String fromNumber;

    @Value("${accountSid}")
    private String accountSid;

    private static final int OTP_LENGTH = 6;
    private static final int ACCESS_TOKEN_BYTES = 32;
    private static final int REFRESH_TOKEN_BYTES = 48;
    private static final int SALT_BYTES = 16;

    private final OtpChallengeRepository otpChallengeRepository;
    private final UserRepository userRepository;
    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpAuthService(
            OtpChallengeRepository otpChallengeRepository,
            UserRepository userRepository,
            AuthProperties authProperties
    ) {
        this.otpChallengeRepository = otpChallengeRepository;
        this.userRepository = userRepository;
        this.authProperties = authProperties;
    }

// ... inside the class ...

    private void sendSmsViaTwilio(String mobile, String otp) {
        // These should ideally be moved to application.yaml and injected via @Value


        mobile = "+91" + mobile;

        String message = "Here is your Call Your Mechanic one time password is: " + otp + " don't share it with anyone, Himanshu ko to bilkul mat dena";

        Twilio.init(accountSid, authToken);

        Message.creator(
                new PhoneNumber(mobile),
                new PhoneNumber(fromNumber),
                message
        ).create();
    }

    @Transactional
    public void requestOtp(String mobile) {
        String normalizedMobile = normalizeMobile(mobile);
        Instant now = Instant.now();

        otpChallengeRepository.findTopByMobileAndConsumedFalseOrderByCreatedAtDesc(normalizedMobile)
                .ifPresent(existingChallenge -> {
                    if (now.isBefore(existingChallenge.getCooldownUntil())) {
                        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "OTP resend cooldown active");
                    }
                    existingChallenge.setConsumed(true);
                    existingChallenge.setConsumedAt(now);
                    otpChallengeRepository.save(existingChallenge);
                });

        String otp = generateOtp();
        System.out.println("DEBUG: Generated OTP is: " + otp);
        String salt = randomToken(SALT_BYTES);

        OtpChallengeEntity challenge = new OtpChallengeEntity();
        challenge.setMobile(normalizedMobile);
        challenge.setSalt(salt);
        challenge.setOtpHash(hashOtp(otp, salt));
        challenge.setAttempts(0);
        challenge.setConsumed(false);
        challenge.setExpiresAt(now.plusSeconds(authProperties.getOtpExpirySeconds()));
        challenge.setCooldownUntil(now.plusSeconds(authProperties.getOtpResendCooldownSeconds()));

        otpChallengeRepository.save(challenge);

        sendSmsViaTwilio(normalizedMobile, otp);

        // Integrate SMS provider here without logging OTP in plaintext.
    }

    @Transactional
    public AuthTokenResponseDto verifyOtp(OtpVerifyDto request) {
        String normalizedMobile = normalizeMobile(request.getMobile());
        Instant now = Instant.now();

        OtpChallengeEntity challenge = otpChallengeRepository
                .findTopByMobileAndConsumedFalseOrderByCreatedAtDesc(normalizedMobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP challenge not found"));

        if (challenge.isConsumed() || now.isAfter(challenge.getExpiresAt())) {
            challenge.setConsumed(true);
            challenge.setConsumedAt(now);
            otpChallengeRepository.save(challenge);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP expired");
        }

        if (challenge.getAttempts() >= authProperties.getOtpMaxAttempts()) {
            challenge.setConsumed(true);
            challenge.setConsumedAt(now);
            otpChallengeRepository.save(challenge);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Maximum OTP attempts exceeded");
        }

        String submittedHash = hashOtp(request.getOtp(), challenge.getSalt());
        if (!constantTimeEquals(submittedHash, challenge.getOtpHash())) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            if (challenge.getAttempts() >= authProperties.getOtpMaxAttempts()) {
                challenge.setConsumed(true);
                challenge.setConsumedAt(now);
            }
            otpChallengeRepository.save(challenge);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }

        challenge.setConsumed(true);
        challenge.setConsumedAt(now);
        otpChallengeRepository.save(challenge);

        UserRole desiredRole = request.getRole() == null ? UserRole.USER : request.getRole();
        UserEntity user = userRepository.findByMob(normalizedMobile)
                .orElseGet(() -> createUser(normalizedMobile, desiredRole));

        AuthTokenResponseDto response = new AuthTokenResponseDto();
        response.setAccessToken(randomToken(ACCESS_TOKEN_BYTES));
        response.setRefreshToken(randomToken(REFRESH_TOKEN_BYTES));
        response.setUserId(user.getId());
        response.setMobile(user.getMob());
        response.setRole(user.getRole());
        return response;
    }

    private UserEntity createUser(String mobile, UserRole role) {
        UserEntity userEntity = new UserEntity();
        userEntity.setMob(mobile);
        userEntity.setRole(role);
        userEntity.setName("User " + mobile.substring(Math.max(0, mobile.length() - 4)));
        return userRepository.save(userEntity);
    }

    private String generateOtp() {
        int upperBound = (int) Math.pow(10, OTP_LENGTH);
        int lowerBound = upperBound / 10;
        int otp = lowerBound + secureRandom.nextInt(upperBound - lowerBound);
        return String.valueOf(otp);
    }

    private String hashOtp(String otp, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((salt + ":" + otp).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String randomToken(int bytes) {
        byte[] token = new byte[bytes];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private String normalizeMobile(String mobile) {
        return mobile == null ? null : mobile.trim();
    }
}
