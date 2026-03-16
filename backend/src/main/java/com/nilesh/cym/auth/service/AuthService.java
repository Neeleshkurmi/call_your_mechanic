package com.nilesh.cym.auth.service;

import com.nilesh.cym.auth.dto.AuthDtos;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.token.JwtService;
import com.nilesh.cym.token.RefreshTokenEntity;
import com.nilesh.cym.token.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public AuthService(JwtService jwtService, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public AuthDtos.TokenResponse refresh(AuthDtos.RefreshRequest request) {
        String refreshToken = requireValue(request.refreshToken(), "refreshToken");
        log.info("session_refresh_start deviceSessionProvided={}", request.deviceSession() != null && !request.deviceSession().isBlank());

        Jws<Claims> parsedClaims = parseRefreshToken(refreshToken);
        String oldJti = parsedClaims.getPayload().getId();
        RefreshTokenEntity existing = refreshTokenRepository
                .findByTokenJtiAndRevokedFalseAndExpiresAtAfter(oldJti, Instant.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is revoked or expired"));

        Long userId = Long.parseLong(parsedClaims.getPayload().getSubject());
        log.debug("session_refresh_token_valid userId={} oldRefreshJti={}", userId, oldJti);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        String deviceSession = Optional.ofNullable(request.deviceSession())
                .filter(s -> !s.isBlank())
                .orElse(existing.getDeviceSession());

        JwtService.TokenPair tokenPair = jwtService.issueTokenPair(user, deviceSession);
        log.debug("session_refresh_new_pair_issued userId={} replacementRefreshJti={} deviceSessionResolved={}",
                userId,
                tokenPair.refreshJti(),
                deviceSession != null);

        existing.setRevoked(true);
        existing.setRevokedAt(Instant.now());
        existing.setReplacedByJti(tokenPair.refreshJti());
        refreshTokenRepository.save(existing);

        RefreshTokenEntity replacement = buildRefreshTokenEntity(user, tokenPair);
        refreshTokenRepository.save(replacement);
        log.info("session_refresh_success userId={} replacedRefreshJti={} newRefreshJti={}",
                userId,
                oldJti,
                tokenPair.refreshJti());

        return new AuthDtos.TokenResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.accessExpiresAt().toEpochMilli(),
                tokenPair.refreshExpiresAt().toEpochMilli()
        );
    }

    public void logout(AuthDtos.LogoutRequest request) {
        String refreshToken = requireValue(request.refreshToken(), "refreshToken");
        log.info("session_logout_start");

        Jws<Claims> parsedClaims = parseRefreshToken(refreshToken);
        String jti = parsedClaims.getPayload().getId();
        Long userId = Long.parseLong(parsedClaims.getPayload().getSubject());

        refreshTokenRepository.findByTokenJti(jti).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
            log.info("session_logout_revoked userId={} refreshJti={}", userId, jti);
        });
        log.debug("session_logout_complete userId={} refreshJti={}", userId, jti);
    }

    private Jws<Claims> parseRefreshToken(String refreshToken) {
        try {
            Jws<Claims> parsedClaims = jwtService.parse(refreshToken);
            if (!jwtService.isRefreshToken(parsedClaims)) {
                log.warn("session_token_parse_failed reason=not_refresh_token");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is not a refresh token");
            }
            return parsedClaims;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("session_token_parse_failed reason={}", ex.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", ex);
        }
    }

    private RefreshTokenEntity buildRefreshTokenEntity(UserEntity user, JwtService.TokenPair tokenPair) {
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setUserId(user.getId());
        refreshTokenEntity.setTokenJti(tokenPair.refreshJti());
        refreshTokenEntity.setDeviceSession(Objects.requireNonNullElseGet(tokenPair.deviceSession(), () -> UUID.randomUUID().toString()));
        refreshTokenEntity.setExpiresAt(tokenPair.refreshExpiresAt());
        refreshTokenEntity.setRevoked(false);
        return refreshTokenEntity;
    }

    private String requireValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            log.warn("session_request_invalid missingField={}", fieldName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value;
    }
}
