package com.nilesh.cym.booking.service;

import com.nilesh.cym.booking.dto.BookingResponseDto;
import com.nilesh.cym.booking.dto.CreateBookingRequestDto;
import com.nilesh.cym.entity.BookingEntity;
import com.nilesh.cym.entity.MechanicEntity;
import com.nilesh.cym.entity.ServiceEntity;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.VehicleEntity;
import com.nilesh.cym.entity.enums.BookingStatus;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.repository.BookingRepository;
import com.nilesh.cym.repository.MechanicRepository;
import com.nilesh.cym.repository.ServiceRepository;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.repository.VehicleRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final MechanicRepository mechanicRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceRepository serviceRepository;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            MechanicRepository mechanicRepository,
            VehicleRepository vehicleRepository,
            ServiceRepository serviceRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.mechanicRepository = mechanicRepository;
        this.vehicleRepository = vehicleRepository;
        this.serviceRepository = serviceRepository;
    }

    public BookingResponseDto createBooking(AuthenticatedUser authenticatedUser, CreateBookingRequestDto request) {
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle does not belong to user");
        }

        BookingEntity booking = new BookingEntity();
        booking.setUser(user);
        booking.setMechanic(mechanic);
        booking.setVehicle(vehicle);
        booking.setService(service);
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setBookingTime(Instant.now());
        booking.setLatitude(request.latitude());
        booking.setLongitude(request.longitude());

        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponseDto getBooking(Long bookingId) {
        return toResponse(fetchBooking(bookingId));
    }

    public List<BookingResponseDto> getUserBookings(AuthenticatedUser authenticatedUser) {
        requireRole(authenticatedUser, UserRole.USER);
        return bookingRepository.findByUser_IdOrderByBookingTimeDesc(authenticatedUser.userId()).stream().map(this::toResponse).toList();
    }

    public List<BookingResponseDto> getMechanicBookings(AuthenticatedUser authenticatedUser) {
        requireRole(authenticatedUser, UserRole.MECHANIC);
        Long mechanicId = findMechanicByUserId(authenticatedUser.userId()).getId();
        return bookingRepository.findByMechanic_IdOrderByBookingTimeDesc(mechanicId).stream().map(this::toResponse).toList();
    }

    public BookingResponseDto acceptBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        requireRole(authenticatedUser, UserRole.MECHANIC);

        BookingEntity booking = fetchBooking(bookingId);
        Long mechanicId = findMechanicByUserId(authenticatedUser.userId()).getId();

        validateMechanicAction(booking, mechanicId);
        validateTransition(booking.getStatus(), BookingStatus.ACCEPTED);
        booking.setStatus(BookingStatus.ACCEPTED);
        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponseDto rejectBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        requireRole(authenticatedUser, UserRole.MECHANIC);

        BookingEntity booking = fetchBooking(bookingId);
        Long mechanicId = findMechanicByUserId(authenticatedUser.userId()).getId();

        validateMechanicAction(booking, mechanicId);
        validateTransition(booking.getStatus(), BookingStatus.CANCELLED);
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    private MechanicEntity findMechanicByUserId(Long userId) {
        return mechanicRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic profile not found"));
    }

    private void requireRole(AuthenticatedUser authenticatedUser, UserRole requiredRole) {
        if (authenticatedUser == null || authenticatedUser.role() != requiredRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        }
    }

    private void validateMechanicAction(BookingEntity booking, Long mechanicId) {
        if (!booking.getMechanic().getId().equals(mechanicId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mechanic is not assigned to this booking");
        }
    }

    private void validateTransition(BookingStatus current, BookingStatus target) {
        boolean valid = current == BookingStatus.REQUESTED && (target == BookingStatus.ACCEPTED || target == BookingStatus.CANCELLED);
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking status transition");
        }
    }

    private BookingEntity fetchBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private BookingResponseDto toResponse(BookingEntity booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getUser().getId(),
                booking.getMechanic().getId(),
                booking.getVehicle().getId(),
                booking.getService().getId(),
                booking.getStatus(),
                booking.getBookingTime(),
                booking.getLatitude(),
                booking.getLongitude()
        );
    }
}
