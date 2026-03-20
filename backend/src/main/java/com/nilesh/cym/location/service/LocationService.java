package com.nilesh.cym.location.service;

import com.nilesh.cym.entity.BookingEntity;
import com.nilesh.cym.entity.MechanicEntity;
import com.nilesh.cym.entity.MechanicLocationEntity;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.UserLiveLocationEntity;
import com.nilesh.cym.entity.enums.BookingStatus;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.location.dto.BookingLocationHistoryDto;
import com.nilesh.cym.location.dto.BookingLocationSnapshotDto;
import com.nilesh.cym.location.dto.LocationResponseDto;
import com.nilesh.cym.location.dto.LocationUpdateRequestDto;
import com.nilesh.cym.location.realtime.LocationBroadcastEvent;
import com.nilesh.cym.location.realtime.LocationEventPublisher;
import com.nilesh.cym.location.realtime.LocationStreamService;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.repository.BookingRepository;
import com.nilesh.cym.repository.MechanicLocationRepository;
import com.nilesh.cym.repository.MechanicRepository;
import com.nilesh.cym.repository.UserLiveLocationRepository;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class LocationService {

    private static final List<BookingStatus> TRACKABLE_STATUSES = List.of(
            BookingStatus.REQUESTED,
            BookingStatus.ACCEPTED,
            BookingStatus.ON_THE_WAY,
            BookingStatus.STARTED
    );

    private static final int DEFAULT_HISTORY_LIMIT = 50;
    private static final int MAX_HISTORY_LIMIT = 200;

    private final MechanicRepository mechanicRepository;
    private final UserRepository userRepository;
    private final MechanicLocationRepository mechanicLocationRepository;
    private final UserLiveLocationRepository userLiveLocationRepository;
    private final BookingRepository bookingRepository;
    private final LocationEventPublisher locationEventPublisher;
    private final LocationStreamService locationStreamService;

    public LocationService(
            MechanicRepository mechanicRepository,
            UserRepository userRepository,
            MechanicLocationRepository mechanicLocationRepository,
            UserLiveLocationRepository userLiveLocationRepository,
            BookingRepository bookingRepository,
            LocationEventPublisher locationEventPublisher,
            LocationStreamService locationStreamService
    ) {
        this.mechanicRepository = mechanicRepository;
        this.userRepository = userRepository;
        this.mechanicLocationRepository = mechanicLocationRepository;
        this.userLiveLocationRepository = userLiveLocationRepository;
        this.bookingRepository = bookingRepository;
        this.locationEventPublisher = locationEventPublisher;
        this.locationStreamService = locationStreamService;
    }

    public LocationResponseDto updateMechanicLocation(AuthenticatedUser authenticatedUser, LocationUpdateRequestDto request) {
        log.debug("location_mechanic_update_start principal={} lat={} lon={}",
                LogSanitizer.summarizePrincipal(authenticatedUser),
                request.latitude(),
                request.longitude());
        requireRole(authenticatedUser, UserRole.MECHANIC);

        MechanicEntity mechanic = mechanicRepository.findByUser_Id(authenticatedUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic profile not found"));

        MechanicLocationEntity location = new MechanicLocationEntity();
        location.setMechanic(mechanic);
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());

        MechanicLocationEntity saved = mechanicLocationRepository.save(location);

        bookingRepository.findByMechanic_IdAndStatusInOrderByBookingTimeDesc(mechanic.getId(), TRACKABLE_STATUSES)
                .forEach(booking -> publishForBooking(booking, "MECHANIC", mechanic.getId(), saved.getLatitude(), saved.getLongitude(), saved.getRecordedAt()));

        LocationResponseDto response = new LocationResponseDto(
                mechanic.getId(),
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getRecordedAt(),
                request.timestamp()
        );
        log.info("location_mechanic_update_success mechanicId={} recordedAt={}", response.actorId(), response.serverTimestamp());
        return response;
    }

    public LocationResponseDto updateUserLocation(AuthenticatedUser authenticatedUser, LocationUpdateRequestDto request) {
        log.debug("location_user_update_start principal={} lat={} lon={}",
                LogSanitizer.summarizePrincipal(authenticatedUser),
                request.latitude(),
                request.longitude());
        requireRole(authenticatedUser, UserRole.USER);

        UserEntity user = userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserLiveLocationEntity location = new UserLiveLocationEntity();
        location.setUser(user);
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());

        UserLiveLocationEntity saved = userLiveLocationRepository.save(location);

        bookingRepository.findByUser_IdAndStatusInOrderByBookingTimeDesc(user.getId(), TRACKABLE_STATUSES)
                .forEach(booking -> publishForBooking(booking, "USER", user.getId(), saved.getLatitude(), saved.getLongitude(), saved.getRecordedAt()));

        LocationResponseDto response = new LocationResponseDto(
                user.getId(),
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getRecordedAt(),
                request.timestamp()
        );
        log.info("location_user_update_success userId={} createdAt={}", response.actorId(), response.serverTimestamp());
        return response;
    }

    public BookingLocationSnapshotDto getLatestBookingLocation(Long bookingId, AuthenticatedUser authenticatedUser) {
        BookingEntity booking = findAuthorizedBooking(bookingId, authenticatedUser);

        LocationResponseDto userLocation = userLiveLocationRepository.findTopByUser_IdOrderByRecordedAtDesc(booking.getUser().getId())
                .map(saved -> new LocationResponseDto(
                        saved.getUser().getId(),
                        saved.getLatitude(),
                        saved.getLongitude(),
                        saved.getRecordedAt(),
                        null
                ))
                .orElse(null);

        LocationResponseDto mechanicLocation = mechanicLocationRepository.findTopByMechanic_IdOrderByRecordedAtDesc(booking.getMechanic().getId())
                .map(saved -> new LocationResponseDto(
                        saved.getMechanic().getId(),
                        saved.getLatitude(),
                        saved.getLongitude(),
                        saved.getRecordedAt(),
                        null
                ))
                .orElse(null);

        return new BookingLocationSnapshotDto(booking.getId(), userLocation, mechanicLocation);
    }

    public BookingLocationHistoryDto getBookingLocationHistory(Long bookingId, AuthenticatedUser authenticatedUser, Instant since, Integer limit) {
        BookingEntity booking = findAuthorizedBooking(bookingId, authenticatedUser);
        Instant effectiveSince = since == null ? Instant.EPOCH : since;
        int effectiveLimit = sanitizeLimit(limit);

        List<LocationResponseDto> userLocations = userLiveLocationRepository
                .findByUser_IdAndRecordedAtGreaterThanEqualOrderByRecordedAtDesc(booking.getUser().getId(), effectiveSince, PageRequest.of(0, effectiveLimit))
                .stream()
                .map(saved -> new LocationResponseDto(
                        saved.getUser().getId(),
                        saved.getLatitude(),
                        saved.getLongitude(),
                        saved.getRecordedAt(),
                        null
                ))
                .toList();

        List<LocationResponseDto> mechanicLocations = mechanicLocationRepository
                .findByMechanic_IdAndRecordedAtGreaterThanEqualOrderByRecordedAtDesc(booking.getMechanic().getId(), effectiveSince, PageRequest.of(0, effectiveLimit))
                .stream()
                .map(saved -> new LocationResponseDto(
                        saved.getMechanic().getId(),
                        saved.getLatitude(),
                        saved.getLongitude(),
                        saved.getRecordedAt(),
                        null
                ))
                .toList();

        return new BookingLocationHistoryDto(booking.getId(), effectiveSince, effectiveLimit, userLocations, mechanicLocations);
    }

    public SseEmitter subscribeBookingLocationStream(Long bookingId, AuthenticatedUser authenticatedUser) {
        findAuthorizedBooking(bookingId, authenticatedUser);
        return locationStreamService.subscribe(bookingId);
    }

    private BookingEntity findAuthorizedBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }

        BookingEntity booking = bookingRepository.findWithParticipantsById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        Long actorUserId = authenticatedUser.userId();
        boolean isBookingUser = booking.getUser().getId().equals(actorUserId);
        boolean isBookingMechanicUser = booking.getMechanic().getUser().getId().equals(actorUserId);

        if (!isBookingUser && !isBookingMechanicUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access booking tracking");
        }

        return booking;
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_HISTORY_LIMIT;
        }
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, MAX_HISTORY_LIMIT);
    }

    private void publishForBooking(BookingEntity booking, String actorType, Long actorId, Double latitude, Double longitude, Instant recordedAt) {
        locationEventPublisher.publish(new LocationBroadcastEvent(
                booking.getId(),
                actorType,
                actorId,
                latitude,
                longitude,
                recordedAt
        ));
    }

    private void requireRole(AuthenticatedUser authenticatedUser, UserRole requiredRole) {
        if (authenticatedUser == null || authenticatedUser.role() != requiredRole) {
            log.warn("location_role_rejected principal={} requiredRole={}",
                    LogSanitizer.summarizePrincipal(authenticatedUser),
                    requiredRole);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        }
    }
}
