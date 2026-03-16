package com.nilesh.cym.location.service;

import com.nilesh.cym.entity.BookingEntity;
import com.nilesh.cym.entity.MechanicEntity;
import com.nilesh.cym.entity.MechanicLocationEntity;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.UserLocationEntity;
import com.nilesh.cym.entity.enums.BookingStatus;
import com.nilesh.cym.entity.enums.UserRole;
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
import com.nilesh.cym.repository.UserLocationRepository;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@Service
public class LocationService {

    private static final List<BookingStatus> TRACKABLE_STATUSES = List.of(
            BookingStatus.REQUESTED,
            BookingStatus.ACCEPTED
    );

    private final MechanicRepository mechanicRepository;
    private final UserRepository userRepository;
    private final MechanicLocationRepository mechanicLocationRepository;
    private final UserLocationRepository userLocationRepository;
    private final BookingRepository bookingRepository;
    private final LocationEventPublisher locationEventPublisher;
    private final LocationStreamService locationStreamService;

    public LocationService(
            MechanicRepository mechanicRepository,
            UserRepository userRepository,
            MechanicLocationRepository mechanicLocationRepository,
            UserLocationRepository userLocationRepository,
            BookingRepository bookingRepository,
            LocationEventPublisher locationEventPublisher,
            LocationStreamService locationStreamService
    ) {
        this.mechanicRepository = mechanicRepository;
        this.userRepository = userRepository;
        this.mechanicLocationRepository = mechanicLocationRepository;
        this.userLocationRepository = userLocationRepository;
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

        UserLocationEntity location = new UserLocationEntity();
        location.setUser(user);
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setAddress("LIVE");
        location.setDefault(Boolean.FALSE);

        UserLocationEntity saved = userLocationRepository.save(location);

        bookingRepository.findByUser_IdAndStatusInOrderByBookingTimeDesc(user.getId(), TRACKABLE_STATUSES)
                .forEach(booking -> publishForBooking(booking, "USER", user.getId(), saved.getLatitude(), saved.getLongitude(), saved.getCreatedAt()));

        LocationResponseDto response =  new LocationResponseDto(
                user.getId(),
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getCreatedAt(),
                request.timestamp()
        );
        log.info("location_user_update_success userId={} createdAt={}", response.actorId(), response.serverTimestamp());
        return response;
    }

    public BookingLocationSnapshotDto getLatestBookingLocation(Long bookingId, AuthenticatedUser authenticatedUser) {
        BookingEntity booking = findAuthorizedBooking(bookingId, authenticatedUser);

        LocationResponseDto userLocation = userLocationRepository.findTopByUser_IdOrderByCreatedAtDesc(booking.getUser().getId())
                .map(saved -> new LocationResponseDto(
                        saved.getUser().getId(),
                        saved.getLatitude(),
                        saved.getLongitude(),
                        saved.getCreatedAt(),
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


    public SseEmitter subscribeBookingLocationStream(Long bookingId, AuthenticatedUser authenticatedUser) {
        findAuthorizedBooking(bookingId, authenticatedUser);
        return locationStreamService.subscribe(bookingId);
    }

    private BookingEntity findAuthorizedBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }

        if (authenticatedUser.role() == UserRole.USER) {
            return bookingRepository.findByIdAndUser_Id(bookingId, authenticatedUser.userId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access booking tracking"));
        }

        if (authenticatedUser.role() == UserRole.MECHANIC) {
            MechanicEntity mechanic = mechanicRepository.findByUser_Id(authenticatedUser.userId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic profile not found"));
            return bookingRepository.findByIdAndMechanic_Id(bookingId, mechanic.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access booking tracking"));
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
    }

    private void publishForBooking(BookingEntity booking, String actorType, Long actorId, Double latitude, Double longitude, java.time.Instant recordedAt) {
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
