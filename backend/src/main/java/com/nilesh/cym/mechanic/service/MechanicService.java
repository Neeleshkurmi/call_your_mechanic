package com.nilesh.cym.mechanic.service;

import com.nilesh.cym.entity.MechanicEntity;
import com.nilesh.cym.entity.MechanicLocationEntity;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.mechanic.dto.MechanicAvailabilityRequestDto;
import com.nilesh.cym.mechanic.dto.MechanicEarningsResponseDto;
import com.nilesh.cym.mechanic.dto.MechanicProfileRequestDto;
import com.nilesh.cym.mechanic.dto.MechanicProfileResponseDto;
import com.nilesh.cym.mechanic.dto.MechanicRegistrationResponseDto;
import com.nilesh.cym.mechanic.dto.NearbyMechanicResponseDto;
import com.nilesh.cym.repository.BookingRepository;
import com.nilesh.cym.entity.enums.BookingStatus;
import com.nilesh.cym.repository.MechanicLocationRepository;
import com.nilesh.cym.repository.MechanicRepository;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import com.nilesh.cym.token.JwtService;
import com.nilesh.cym.token.RefreshTokenEntity;
import com.nilesh.cym.token.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class MechanicService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final MechanicRepository mechanicRepository;
    private final MechanicLocationRepository mechanicLocationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public MechanicService(
            MechanicRepository mechanicRepository,
            MechanicLocationRepository mechanicLocationRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.mechanicRepository = mechanicRepository;
        this.mechanicLocationRepository = mechanicLocationRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public MechanicRegistrationResponseDto registerMechanic(AuthenticatedUser authenticatedUser, MechanicProfileRequestDto request) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        if (mechanicRepository.findByUser_Id(authenticatedUser.userId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mechanic profile already exists");
        }

        UserEntity user = userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin users cannot register as mechanics");
        }

        user.setRole(UserRole.MECHANIC);
        UserEntity savedUser = userRepository.save(user);

        MechanicEntity mechanic = new MechanicEntity();
        mechanic.setUser(savedUser);
        mechanic.setAvailable(Boolean.FALSE);
        mechanic.setExperienceYears(request.experienceYears());
        mechanic.setSkills(request.skills().trim());
        mechanic.setBio(request.bio().trim());
        MechanicEntity savedMechanic = mechanicRepository.save(mechanic);

        revokeActiveRefreshTokens(savedUser.getId());
        JwtService.TokenPair tokenPair = jwtService.issueTokenPair(savedUser, null);
        refreshTokenRepository.save(buildRefreshTokenEntity(savedUser, tokenPair));

        return new MechanicRegistrationResponseDto(
                toProfileResponse(savedMechanic),
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.accessExpiresAt().toEpochMilli(),
                tokenPair.refreshExpiresAt().toEpochMilli()
        );
    }

    @Transactional(readOnly = true)
    public List<NearbyMechanicResponseDto> getNearbyMechanics(AuthenticatedUser authenticatedUser, Double latitude, Double longitude) {
        requireRole(authenticatedUser, UserRole.USER);

        return mechanicRepository.findAvailableMechanics().stream()
                .map(mechanic -> toNearbyResponse(mechanic, latitude, longitude))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(NearbyMechanicResponseDto::distanceKm)
                        .thenComparing(NearbyMechanicResponseDto::rating, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public MechanicProfileResponseDto getMechanicDetails(Long mechanicId) {
        return toProfileResponse(fetchMechanicWithUser(mechanicId));
    }

    @Transactional(readOnly = true)
    public MechanicProfileResponseDto getMyProfile(AuthenticatedUser authenticatedUser) {
        Long mechanicUserId = requireRole(authenticatedUser, UserRole.MECHANIC);
        return toProfileResponse(fetchMechanicByUserId(mechanicUserId));
    }

    @Transactional
    public MechanicProfileResponseDto updateAvailability(AuthenticatedUser authenticatedUser, MechanicAvailabilityRequestDto request) {
        Long mechanicUserId = requireRole(authenticatedUser, UserRole.MECHANIC);
        MechanicEntity mechanic = fetchMechanicByUserId(mechanicUserId);
        mechanic.setAvailable(request.available());
        return toProfileResponse(mechanicRepository.save(mechanic));
    }

    @Transactional
    public MechanicProfileResponseDto updateProfile(AuthenticatedUser authenticatedUser, MechanicProfileRequestDto request) {
        Long mechanicUserId = requireRole(authenticatedUser, UserRole.MECHANIC);
        MechanicEntity mechanic = fetchMechanicByUserId(mechanicUserId);
        mechanic.setExperienceYears(request.experienceYears());
        mechanic.setSkills(request.skills().trim());
        mechanic.setBio(request.bio().trim());
        return toProfileResponse(mechanicRepository.save(mechanic));
    }

    @Transactional(readOnly = true)
    public MechanicEarningsResponseDto getEarnings(AuthenticatedUser authenticatedUser) {
        Long mechanicUserId = requireRole(authenticatedUser, UserRole.MECHANIC);
        MechanicEntity mechanic = fetchMechanicByUserId(mechanicUserId);
        int completedJobs = bookingRepository.findByMechanic_IdAndStatusOrderByBookingTimeDesc(mechanic.getId(), BookingStatus.COMPLETED).size();
        double total = completedJobs * 499D;
        return new MechanicEarningsResponseDto(mechanic.getId(), completedJobs, total);
    }

    private Optional<NearbyMechanicResponseDto> toNearbyResponse(MechanicEntity mechanic, Double latitude, Double longitude) {
        return mechanicLocationRepository.findTopByMechanic_IdOrderByRecordedAtDesc(mechanic.getId())
                .map(location -> {
                    double distance = calculateDistanceKm(latitude, longitude, location.getLatitude(), location.getLongitude());
                    return new NearbyMechanicResponseDto(
                            mechanic.getId(),
                            mechanic.getUser().getName(),
                            mechanic.getRating(),
                            mechanic.getExperienceYears(),
                            mechanic.getSkills(),
                            roundDistance(distance),
                            location.getLatitude(),
                            location.getLongitude()
                    );
                });
    }

    private MechanicEntity fetchMechanicWithUser(Long mechanicId) {
        return mechanicRepository.findWithUserById(mechanicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found"));
    }

    private MechanicEntity fetchMechanicByUserId(Long userId) {
        return mechanicRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic profile not found"));
    }

    private Long requireRole(AuthenticatedUser authenticatedUser, UserRole role) {
        if (authenticatedUser == null || authenticatedUser.role() != role) {
            log.warn("mechanic_role_rejected principal={} requiredRole={}",
                    LogSanitizer.summarizePrincipal(authenticatedUser),
                    role);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        }
        return authenticatedUser.userId();
    }

    private MechanicProfileResponseDto toProfileResponse(MechanicEntity mechanic) {
        return new MechanicProfileResponseDto(
                mechanic.getId(),
                mechanic.getUser().getId(),
                mechanic.getUser().getName(),
                mechanic.getUser().getMob(),
                mechanic.getAvailable(),
                mechanic.getExperienceYears(),
                mechanic.getRating(),
                mechanic.getSkills(),
                mechanic.getBio()
        );
    }

    private double calculateDistanceKm(Double startLat, Double startLng, Double endLat, Double endLng) {
        double dLat = Math.toRadians(endLat - startLat);
        double dLng = Math.toRadians(endLng - startLng);
        double originLat = Math.toRadians(startLat);
        double targetLat = Math.toRadians(endLat);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(originLat) * Math.cos(targetLat) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private double roundDistance(double distanceKm) {
        return BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP).doubleValue();
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
