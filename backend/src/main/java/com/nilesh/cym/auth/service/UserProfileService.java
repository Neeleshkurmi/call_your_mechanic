package com.nilesh.cym.auth.service;

import com.nilesh.cym.auth.dto.CurrentUserProfileDto;
import com.nilesh.cym.auth.dto.CurrentUserUpdateRequestDto;
import com.nilesh.cym.auth.dto.ProfileUpdateRequestDto;
import com.nilesh.cym.auth.dto.ProfileUpdateResponseDto;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import com.nilesh.cym.token.JwtService;
import com.nilesh.cym.token.RefreshTokenEntity;
import com.nilesh.cym.token.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserProfileService(
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public ProfileUpdateResponseDto updateCurrentProfile(AuthenticatedUser authenticatedUser, ProfileUpdateRequestDto request) {
        requireAuthenticatedUser(authenticatedUser);

        String normalizedName = normalizeName(request.getName());
        UserRole selectedRole = validateSelectableRole(request.getRole());
        Long userId = authenticatedUser.userId();

        log.info("profile_update_start principal={} requestedRole={} requestedName={}",
                LogSanitizer.summarizePrincipal(authenticatedUser),
                selectedRole,
                normalizedName);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setName(normalizedName);
        user.setRole(selectedRole);
        user.setProfileCompleted(true);
        UserEntity savedUser = userRepository.save(user);
        log.info("profile_update_persisted userId={} newRole={} name={}", savedUser.getId(), savedUser.getRole(), savedUser.getName());

        revokeActiveRefreshTokens(savedUser.getId());

        JwtService.TokenPair tokenPair = jwtService.issueTokenPair(savedUser, null);
        RefreshTokenEntity replacementToken = buildRefreshTokenEntity(savedUser, tokenPair);
        refreshTokenRepository.save(replacementToken);
        log.info("profile_update_tokens_issued userId={} newRefreshJti={}", savedUser.getId(), tokenPair.refreshJti());

        return new ProfileUpdateResponseDto(
                new CurrentUserProfileDto(
                        savedUser.getId(),
                        savedUser.getName(),
                        savedUser.getMob(),
                        savedUser.getRole(),
                        savedUser.isProfileCompleted()
                ),
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.accessExpiresAt().toEpochMilli(),
                tokenPair.refreshExpiresAt().toEpochMilli()
        );
    }

    @Transactional
    public CurrentUserProfileDto updateCurrentUser(AuthenticatedUser authenticatedUser, CurrentUserUpdateRequestDto request) {
        requireAuthenticatedUser(authenticatedUser);
        UserEntity user = userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setName(normalizeName(request.name()));
        user.setRole(validateSelectableRole(request.role()));
        UserEntity saved = userRepository.save(user);
        return new CurrentUserProfileDto(saved.getId(), saved.getName(), saved.getMob(), saved.getRole(), saved.isProfileCompleted());
    }

    @Transactional
    public CurrentUserProfileDto getCurrentUser(AuthenticatedUser authenticatedUser) {
        requireAuthenticatedUser(authenticatedUser);
        UserEntity user = userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new CurrentUserProfileDto(user.getId(), user.getName(), user.getMob(), user.getRole(), user.isProfileCompleted());
    }

    private void requireAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            log.warn("profile_update_rejected reason=missing_authenticated_user");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            log.warn("profile_update_rejected reason=blank_name");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        return name.trim();
    }

    private UserRole validateSelectableRole(UserRole requestedRole) {
        if (requestedRole == UserRole.USER || requestedRole == UserRole.MECHANIC) {
            return requestedRole;
        }

        log.warn("profile_update_rejected reason=unsupported_role requestedRole={}", requestedRole);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only USER and MECHANIC roles can be selected");
    }

    private void revokeActiveRefreshTokens(Long userId) {
        List<RefreshTokenEntity> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalseAndExpiresAtAfter(userId, Instant.now());
        for (RefreshTokenEntity token : activeTokens) {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
        }
        if (!activeTokens.isEmpty()) {
            refreshTokenRepository.saveAll(activeTokens);
        }
        log.debug("profile_update_refresh_tokens_revoked userId={} revokedCount={}", userId, activeTokens.size());
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
}
