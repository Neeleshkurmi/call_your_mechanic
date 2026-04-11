package com.nilesh.cym.booking.service;

import com.nilesh.cym.booking.dto.BookingResponseDto;
import com.nilesh.cym.booking.dto.BookingStatusUpdateRequestDto;
import com.nilesh.cym.booking.dto.CreateBookingRequestDto;
import com.nilesh.cym.entity.BookingEntity;
import com.nilesh.cym.entity.MechanicEntity;
import com.nilesh.cym.entity.MechanicLocationEntity;
import com.nilesh.cym.entity.ServiceEntity;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.VehicleEntity;
import com.nilesh.cym.entity.enums.BookingStatus;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.repository.BookingRepository;
import com.nilesh.cym.repository.MechanicLocationRepository;
import com.nilesh.cym.repository.MechanicRepository;
import com.nilesh.cym.repository.ReviewRepository;
import com.nilesh.cym.repository.ServiceRepository;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.repository.VehicleRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class BookingService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double TRAVEL_RATE_PER_KM = 18.0;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final MechanicRepository mechanicRepository;
    private final MechanicLocationRepository mechanicLocationRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewRepository reviewRepository;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            MechanicRepository mechanicRepository,
            MechanicLocationRepository mechanicLocationRepository,
            VehicleRepository vehicleRepository,
            ServiceRepository serviceRepository,
            ReviewRepository reviewRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.mechanicRepository = mechanicRepository;
        this.mechanicLocationRepository = mechanicLocationRepository;
        this.vehicleRepository = vehicleRepository;
        this.serviceRepository = serviceRepository;
        this.reviewRepository = reviewRepository;
    }

    public BookingResponseDto createBooking(AuthenticatedUser authenticatedUser, CreateBookingRequestDto request) {
        log.debug("booking_create_start principal={} mechanicId={} vehicleId={} serviceId={}",
                LogSanitizer.summarizePrincipal(authenticatedUser),
                request.mechanicId(),
                request.vehicleId(),
                request.serviceId());
        requireRole(authenticatedUser, UserRole.USER);

        Long userId = authenticatedUser.userId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        MechanicEntity mechanic = mechanicRepository.findById(request.mechanicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found"));
        VehicleEntity vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        ServiceEntity service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        if (!vehicle.getUser().getId().equals(userId)) {
            log.warn("booking_create_rejected userId={} vehicleId={} reason=vehicle_owner_mismatch", userId, request.vehicleId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle does not belong to user");
        }

        if (!Boolean.TRUE.equals(mechanic.getAvailable())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mechanic is currently unavailable");
        }

        BookingEntity booking = prepareBooking(user, mechanic, vehicle, service, request.latitude(), request.longitude(), Instant.now());

        BookingEntity saved = bookingRepository.save(booking);
        log.info("booking_create_success bookingId={} userId={} mechanicId={} serviceId={} status={}",
                saved.getId(),
                userId,
                mechanic.getId(),
                service.getId(),
                saved.getStatus());
        return toResponse(saved);
    }

    public BookingResponseDto getBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        log.debug("booking_fetch_start bookingId={}", bookingId);
        BookingResponseDto response = toResponse(findParticipantBooking(bookingId, authenticatedUser));
        log.debug("booking_fetch_success bookingId={} status={}", response.bookingId(), response.status());
        return response;
    }

    public List<BookingResponseDto> getUserBookings(AuthenticatedUser authenticatedUser) {
        return getBookings(authenticatedUser, null);
    }

    public List<BookingResponseDto> getMechanicBookings(AuthenticatedUser authenticatedUser) {
        return getBookings(authenticatedUser, null);
    }

    public List<BookingResponseDto> getBookings(AuthenticatedUser authenticatedUser, BookingStatus status) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }
        if (authenticatedUser.role() == UserRole.USER) {
            List<BookingEntity> bookings = status == null
                    ? bookingRepository.findByUser_IdOrderByBookingTimeDesc(authenticatedUser.userId())
                    : bookingRepository.findByUser_IdAndStatusOrderByBookingTimeDesc(authenticatedUser.userId(), status);
            return bookings.stream().map(this::toResponse).toList();
        }
        if (authenticatedUser.role() == UserRole.MECHANIC) {
            Long mechanicId = findMechanicByUserId(authenticatedUser.userId()).getId();
            List<BookingEntity> bookings = status == null
                    ? bookingRepository.findByMechanic_IdOrderByBookingTimeDesc(mechanicId)
                    : bookingRepository.findByMechanic_IdAndStatusOrderByBookingTimeDesc(mechanicId, status);
            return bookings.stream().map(this::toResponse).toList();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
    }

    public List<BookingResponseDto> getActiveBookings(AuthenticatedUser authenticatedUser) {
        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.REQUESTED,
                BookingStatus.ACCEPTED,
                BookingStatus.ON_THE_WAY,
                BookingStatus.STARTED
        );
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }
        if (authenticatedUser.role() == UserRole.USER) {
            return bookingRepository.findByUser_IdAndStatusInOrderByBookingTimeDesc(authenticatedUser.userId(), activeStatuses)
                    .stream().map(this::toResponse).toList();
        }
        if (authenticatedUser.role() == UserRole.MECHANIC) {
            Long mechanicId = findMechanicByUserId(authenticatedUser.userId()).getId();
            return bookingRepository.findByMechanic_IdAndStatusInOrderByBookingTimeDesc(mechanicId, activeStatuses)
                    .stream().map(this::toResponse).toList();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
    }

    public BookingResponseDto acceptBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        return updateBookingStatus(bookingId, authenticatedUser, new BookingStatusUpdateRequestDto(BookingStatus.ACCEPTED));
    }

    public BookingResponseDto rejectBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        BookingEntity booking = findAssignedMechanicBooking(bookingId, authenticatedUser);
        validateTransition(booking.getStatus(), BookingStatus.CANCELLED, authenticatedUser.role(), true);
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponseDto updateBookingStatus(Long bookingId, AuthenticatedUser authenticatedUser, BookingStatusUpdateRequestDto request) {
        BookingEntity booking = findAssignedMechanicBooking(bookingId, authenticatedUser);
        validateTransition(booking.getStatus(), request.status(), authenticatedUser.role(), false);
        booking.setStatus(request.status());
        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponseDto cancelBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        BookingEntity booking = findUserBooking(bookingId, authenticatedUser);
        validateTransition(booking.getStatus(), BookingStatus.CANCELLED, authenticatedUser.role(), false);
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponseDto completeBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        return updateBookingStatus(bookingId, authenticatedUser, new BookingStatusUpdateRequestDto(BookingStatus.COMPLETED));
    }

    public BookingResponseDto rebook(Long bookingId, AuthenticatedUser authenticatedUser) {
        BookingEntity original = findUserBooking(bookingId, authenticatedUser);
        MechanicEntity mechanic = original.getMechanic();
        if (!Boolean.TRUE.equals(mechanic.getAvailable())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mechanic is currently unavailable");
        }

        BookingEntity booking = prepareBooking(
                original.getUser(),
                mechanic,
                original.getVehicle(),
                original.getService(),
                original.getLatitude(),
                original.getLongitude(),
                Instant.now()
        );
        return toResponse(bookingRepository.save(booking));
    }

    private BookingEntity prepareBooking(
            UserEntity user,
            MechanicEntity mechanic,
            VehicleEntity vehicle,
            ServiceEntity service,
            Double latitude,
            Double longitude,
            Instant bookingTime
    ) {
        FareBreakdown fareBreakdown = calculateFareBreakdown(mechanic, service, latitude, longitude);
        BookingEntity booking = new BookingEntity();
        booking.setUser(user);
        booking.setMechanic(mechanic);
        booking.setVehicle(vehicle);
        booking.setService(service);
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setBookingTime(bookingTime);
        booking.setLatitude(latitude);
        booking.setLongitude(longitude);
        booking.setTravelDistanceKm(fareBreakdown.travelDistanceKm());
        booking.setTravelCharge(fareBreakdown.travelCharge());
        booking.setServiceCharge(fareBreakdown.serviceCharge());
        booking.setTotalFare(fareBreakdown.totalFare());
        return booking;
    }

    private MechanicEntity findMechanicByUserId(Long userId) {
        log.debug("booking_mechanic_lookup userId={}", userId);
        return mechanicRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic profile not found"));
    }

    private void requireRole(AuthenticatedUser authenticatedUser, UserRole requiredRole) {
        if (authenticatedUser == null || authenticatedUser.role() != requiredRole) {
            log.warn("booking_role_rejected principal={} requiredRole={}",
                    LogSanitizer.summarizePrincipal(authenticatedUser),
                    requiredRole);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        }
    }

    private void validateMechanicAction(BookingEntity booking, Long mechanicId) {
        if (!booking.getMechanic().getId().equals(mechanicId)) {
            log.warn("booking_mechanic_rejected bookingId={} assignedMechanicId={} actingMechanicId={}",
                    booking.getId(),
                    booking.getMechanic().getId(),
                    mechanicId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mechanic is not assigned to this booking");
        }
    }

    private void validateTransition(BookingStatus current, BookingStatus target, UserRole actorRole, boolean legacyReject) {
        boolean valid;
        if (target == BookingStatus.CANCELLED) {
            valid = legacyReject
                    ? actorRole == UserRole.MECHANIC && Set.of(BookingStatus.REQUESTED, BookingStatus.ACCEPTED).contains(current)
                    : actorRole == UserRole.USER && current != BookingStatus.COMPLETED && current != BookingStatus.CANCELLED;
        } else if (actorRole == UserRole.MECHANIC) {
            valid = switch (target) {
                case ACCEPTED -> current == BookingStatus.REQUESTED;
                case ON_THE_WAY -> current == BookingStatus.ACCEPTED;
                case STARTED -> current == BookingStatus.ON_THE_WAY;
                case COMPLETED -> current == BookingStatus.STARTED;
                default -> false;
            };
        } else {
            valid = false;
        }

        if (!valid) {
            log.warn("booking_transition_rejected currentStatus={} targetStatus={}", current, target);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking status transition");
        }
    }

    private BookingEntity fetchBooking(Long bookingId) {
        log.debug("booking_lookup bookingId={}", bookingId);
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private BookingEntity findParticipantBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }
        BookingEntity booking = bookingRepository.findWithParticipantsById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        boolean userParticipant = booking.getUser().getId().equals(authenticatedUser.userId());
        boolean mechanicParticipant = booking.getMechanic().getUser().getId().equals(authenticatedUser.userId());
        if (!userParticipant && !mechanicParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this booking");
        }
        return booking;
    }

    private BookingEntity findAssignedMechanicBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        requireRole(authenticatedUser, UserRole.MECHANIC);
        BookingEntity booking = fetchBooking(bookingId);
        Long mechanicId = findMechanicByUserId(authenticatedUser.userId()).getId();
        validateMechanicAction(booking, mechanicId);
        return booking;
    }

    private BookingEntity findUserBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        requireRole(authenticatedUser, UserRole.USER);
        return bookingRepository.findByIdAndUser_Id(bookingId, authenticatedUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private BookingResponseDto toResponse(BookingEntity booking) {
        FareBreakdown fareBreakdown = resolveFareBreakdown(booking);
        return new BookingResponseDto(
                booking.getId(),
                booking.getUser().getId(),
                booking.getMechanic().getId(),
                booking.getVehicle().getId(),
                booking.getService().getId(),
                booking.getStatus(),
                booking.getBookingTime(),
                booking.getLatitude(),
                booking.getLongitude(),
                fareBreakdown.travelDistanceKm(),
                fareBreakdown.travelCharge(),
                fareBreakdown.serviceCharge(),
                fareBreakdown.totalFare(),
                reviewRepository.existsByBooking_Id(booking.getId())
        );
    }

    private FareBreakdown resolveFareBreakdown(BookingEntity booking) {
        Double travelDistanceKm = booking.getTravelDistanceKm();
        Double travelCharge = booking.getTravelCharge();
        Double serviceCharge = booking.getServiceCharge();
        Double totalFare = booking.getTotalFare();

        if (travelDistanceKm != null && travelCharge != null && serviceCharge != null && totalFare != null) {
            return new FareBreakdown(
                    roundMoney(travelDistanceKm),
                    roundMoney(travelCharge),
                    roundMoney(serviceCharge),
                    roundMoney(totalFare)
            );
        }

        return calculateFareBreakdown(booking.getMechanic(), booking.getService(), booking.getLatitude(), booking.getLongitude());
    }

    private FareBreakdown calculateFareBreakdown(MechanicEntity mechanic, ServiceEntity service, Double userLatitude, Double userLongitude) {
        MechanicLocationEntity mechanicLocation = mechanicLocationRepository.findTopByMechanic_IdOrderByRecordedAtDesc(mechanic.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mechanic location unavailable for fare calculation"));
        double distanceKm = calculateDistanceKm(
                mechanicLocation.getLatitude(),
                mechanicLocation.getLongitude(),
                userLatitude,
                userLongitude
        );
        double roundedDistance = roundMoney(distanceKm);
        double serviceCharge = resolveServiceCharge(service);
        double travelCharge = roundMoney(roundedDistance * TRAVEL_RATE_PER_KM);
        double totalFare = roundMoney(serviceCharge + travelCharge);
        return new FareBreakdown(roundedDistance, travelCharge, serviceCharge, totalFare);
    }

    private double resolveServiceCharge(ServiceEntity service) {
        if (service.getServiceCharge() != null) {
            return roundMoney(service.getServiceCharge());
        }
        return switch (service.getVehicleType()) {
            case BIKE -> 299D;
            case CAR -> 499D;
            case TRUCK -> 899D;
            case ALL -> 399D;
        };
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

    private double roundMoney(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private record FareBreakdown(
            Double travelDistanceKm,
            Double travelCharge,
            Double serviceCharge,
            Double totalFare
    ) {
    }
}
