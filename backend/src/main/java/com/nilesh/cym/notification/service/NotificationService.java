package com.nilesh.cym.notification.service;

import com.nilesh.cym.entity.BookingEntity;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.notification.dto.NotificationResponseDto;
import com.nilesh.cym.repository.BookingRepository;
import com.nilesh.cym.repository.MechanicRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationService {

    private final BookingRepository bookingRepository;
    private final MechanicRepository mechanicRepository;

    public NotificationService(BookingRepository bookingRepository, MechanicRepository mechanicRepository) {
        this.bookingRepository = bookingRepository;
        this.mechanicRepository = mechanicRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }

        List<BookingEntity> bookings = switch (authenticatedUser.role()) {
            case USER -> bookingRepository.findByUser_IdOrderByBookingTimeDesc(authenticatedUser.userId());
            case MECHANIC -> {
                Long mechanicId = mechanicRepository.findByUser_Id(authenticatedUser.userId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic profile not found"))
                        .getId();
                yield bookingRepository.findByMechanic_IdOrderByBookingTimeDesc(mechanicId);
            }
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        };

        return bookings.stream()
                .limit(20)
                .map(booking -> new NotificationResponseDto(
                        "booking-" + booking.getId(),
                        "Booking " + booking.getId() + " is currently " + booking.getStatus(),
                        "BOOKING_STATUS",
                        booking.getUpdatedAt()
                ))
                .toList();
    }
}
