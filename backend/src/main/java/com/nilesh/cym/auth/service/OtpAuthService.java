package com.nilesh.cym.auth.service;

import com.nilesh.cym.auth.config.AuthProperties;
import com.nilesh.cym.auth.dto.AuthTokenResponseDto;
import com.nilesh.cym.auth.dto.OtpVerifyDto;
import com.nilesh.cym.auth.dto.RoleUpdateRequestDto;
import com.nilesh.cym.auth.entity.OtpChallengeEntity;
import com.nilesh.cym.auth.repository.OtpChallengeRepository;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class OtpAuthService {

    @Value("${sms.authToken}")
    private String authToken;

    @Value("${sms.fromNumber}")
    private String fromNumber;

    @Value("${sms.accountSid}")
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


//        mobile = "+91" + mobile;

        String message = "Here is your Call Your Mechanic one time password is: " + otp + " don't share it with anyone, Himanshu ko to bilkul mat dena";
        log.info("otp_sms_send_start mobile={}", LogSanitizer.maskMobile(mobile));

        Twilio.init(accountSid, authToken);

        Message.creator(
                new PhoneNumber(mobile),
                new PhoneNumber(fromNumber),
                message
        ).create();
        log.info("otp_sms_send_success mobile={}", LogSanitizer.maskMobile(mobile));
    }

    @Transactional
    public void requestOtp(String mobile) {
        String normalizedMobile = normalizeMobile(mobile);
        Instant now = Instant.now();
        log.info("otp_request_start mobile={}", LogSanitizer.maskMobile(normalizedMobile));

        otpChallengeRepository.findTopByMobileAndConsumedFalseOrderByCreatedAtDesc(normalizedMobile)
                .ifPresent(existingChallenge -> {
                    if (now.isBefore(existingChallenge.getCooldownUntil())) {
                        log.warn("otp_request_rejected mobile={} reason=cooldown_active cooldownUntil={}",
                                LogSanitizer.maskMobile(normalizedMobile),
                                existingChallenge.getCooldownUntil());
                        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "OTP resend cooldown active");
                    }
                    existingChallenge.setConsumed(true);
                    existingChallenge.setConsumedAt(now);
                    otpChallengeRepository.save(existingChallenge);
                    log.debug("otp_request_previous_challenge_consumed mobile={} challengeId={}",
                            LogSanitizer.maskMobile(normalizedMobile),
                            existingChallenge.getId());
                });

        String otp = generateOtp();
        String salt = randomToken(SALT_BYTES);

        OtpChallengeEntity challenge = new OtpChallengeEntity();
        challenge.setMobile(normalizedMobile);
        challenge.setSalt(salt);
        challenge.setOtpHash(hashOtp(otp, salt));
        challenge.setAttempts(0);
        challenge.setConsumed(false);
        challenge.setExpiresAt(now.plusSeconds(authProperties.getOtpExpirySeconds()));
        challenge.setCooldownUntil(now.plusSeconds(authProperties.getOtpResendCooldownSeconds()));

        OtpChallengeEntity savedChallenge = otpChallengeRepository.save(challenge);
        log.debug("otp_request_challenge_created mobile={} challengeId={} expiresAt={} cooldownUntil={}",
                LogSanitizer.maskMobile(normalizedMobile),
                savedChallenge.getId(),
                savedChallenge.getExpiresAt(),
                savedChallenge.getCooldownUntil());

        sendSmsViaTwilio(normalizedMobile, otp);
        log.info("otp_request_complete mobile={}", LogSanitizer.maskMobile(normalizedMobile));

        // Integrate SMS provider here without logging OTP in plaintext.
    }

    @Transactional
    public AuthTokenResponseDto verifyOtp(OtpVerifyDto request) {
        String normalizedMobile = normalizeMobile(request.getMobile());
        Instant now = Instant.now();
        log.info("otp_verify_start mobile={} requestedRole={}",
                LogSanitizer.maskMobile(normalizedMobile),
                request.getRole() == null ? UserRole.USER : request.getRole());

        OtpChallengeEntity challenge = otpChallengeRepository
                .findTopByMobileAndConsumedFalseOrderByCreatedAtDesc(normalizedMobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP challenge not found"));
        log.debug("otp_verify_challenge_loaded mobile={} challengeId={} attempts={} expiresAt={}",
                LogSanitizer.maskMobile(normalizedMobile),
                challenge.getId(),
                challenge.getAttempts(),
                challenge.getExpiresAt());

        if (challenge.isConsumed() || now.isAfter(challenge.getExpiresAt())) {
            challenge.setConsumed(true);
            challenge.setConsumedAt(now);
            otpChallengeRepository.save(challenge);
            log.warn("otp_verify_rejected mobile={} challengeId={} reason=expired_or_consumed",
                    LogSanitizer.maskMobile(normalizedMobile),
                    challenge.getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP expired");
        }

        if (challenge.getAttempts() >= authProperties.getOtpMaxAttempts()) {
            challenge.setConsumed(true);
            challenge.setConsumedAt(now);
            otpChallengeRepository.save(challenge);
            log.warn("otp_verify_rejected mobile={} challengeId={} reason=max_attempts_exceeded attempts={}",
                    LogSanitizer.maskMobile(normalizedMobile),
                    challenge.getId(),
                    challenge.getAttempts());
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
            log.warn("otp_verify_rejected mobile={} challengeId={} reason=invalid_otp attempts={}",
                    LogSanitizer.maskMobile(normalizedMobile),
                    challenge.getId(),
                    challenge.getAttempts());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }

        challenge.setConsumed(true);
        challenge.setConsumedAt(now);
        otpChallengeRepository.save(challenge);

        UserRole desiredRole = request.getRole() == null ? UserRole.USER : request.getRole();
        UserEntity user = userRepository.findByMob(normalizedMobile)
                .orElseGet(() -> createUser(normalizedMobile, desiredRole));
        log.debug("otp_verify_user_resolved mobile={} userId={} role={} existingUser={}",
                LogSanitizer.maskMobile(normalizedMobile),
                user.getId(),
                user.getRole(),
                userRepository.findByMob(normalizedMobile).isPresent());

        AuthTokenResponseDto response = new AuthTokenResponseDto();
        response.setAccessToken(randomToken(ACCESS_TOKEN_BYTES));
        response.setRefreshToken(randomToken(REFRESH_TOKEN_BYTES));
        response.setUserId(user.getId());
        response.setMobile(user.getMob());
        response.setRole(user.getRole());
        log.info("otp_verify_success mobile={} userId={} role={}",
                LogSanitizer.maskMobile(normalizedMobile),
                user.getId(),
                user.getRole());
        return response;
    }


    @Transactional
    public void updateRole(RoleUpdateRequestDto request) {
        UserRole selectedRole = validateSelectableRole(request.getRole());
        String normalizedMobile = normalizeMobile(request.getMobile());
        log.info("role_update_start mobile={} requestedRole={}",
                LogSanitizer.maskMobile(normalizedMobile),
                selectedRole);

        UserEntity user = userRepository.findByMob(normalizedMobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setRole(selectedRole);
        userRepository.save(user);
        log.info("role_update_success mobile={} userId={} newRole={}",
                LogSanitizer.maskMobile(normalizedMobile),
                user.getId(),
                selectedRole);
    }

    private UserRole validateSelectableRole(UserRole requestedRole) {
        if (requestedRole == UserRole.USER || requestedRole == UserRole.MECHANIC) {
            return requestedRole;
        }

        log.warn("role_update_rejected reason=unsupported_role requestedRole={}", requestedRole);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only USER and MECHANIC roles can be selected");
    }

    private UserEntity createUser(String mobile, UserRole role) {
        UserEntity userEntity = new UserEntity();
        userEntity.setMob(mobile);
        userEntity.setRole(role);
        userEntity.setName("User " + mobile.substring(Math.max(0, mobile.length() - 4)));
        UserEntity saved = userRepository.save(userEntity);
        log.info("user_created_from_otp userId={} mobile={} role={}",
                saved.getId(),
                LogSanitizer.maskMobile(saved.getMob()),
                saved.getRole());
        return saved;
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
