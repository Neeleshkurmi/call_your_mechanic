package com.nilesh.cym.booking.service;

import com.nilesh.cym.booking.dto.BookingStatusUpdateRequestDto;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MechanicRepository mechanicRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void updateBookingStatus_movesAssignedMechanicBookingForward() {
        AuthenticatedUser mechanicPrincipal = new AuthenticatedUser(2L, UserRole.MECHANIC, "+919900000002");
        BookingEntity booking = bookingWithStatus(BookingStatus.ACCEPTED);
        MechanicEntity mechanic = booking.getMechanic();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(mechanicRepository.findByUser_Id(2L)).thenReturn(Optional.of(mechanic));
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = bookingService.updateBookingStatus(1L, mechanicPrincipal, new BookingStatusUpdateRequestDto(BookingStatus.ON_THE_WAY));

        assertEquals(BookingStatus.ON_THE_WAY, response.status());
    }

    @Test
    void completeBooking_rejectsInvalidTransition() {
        AuthenticatedUser mechanicPrincipal = new AuthenticatedUser(2L, UserRole.MECHANIC, "+919900000002");
        BookingEntity booking = bookingWithStatus(BookingStatus.ACCEPTED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(mechanicRepository.findByUser_Id(2L)).thenReturn(Optional.of(booking.getMechanic()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.completeBooking(1L, mechanicPrincipal)
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    private BookingEntity bookingWithStatus(BookingStatus status) {
        UserEntity bookingUser = new UserEntity();
        bookingUser.setId(1L);
        bookingUser.setRole(UserRole.USER);

        UserEntity mechanicUser = new UserEntity();
        mechanicUser.setId(2L);
        mechanicUser.setRole(UserRole.MECHANIC);

        MechanicEntity mechanic = new MechanicEntity();
        mechanic.setId(1L);
        mechanic.setUser(mechanicUser);
        mechanic.setAvailable(Boolean.TRUE);

        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setId(3L);
        vehicle.setUser(bookingUser);

        ServiceEntity service = new ServiceEntity();
        service.setId(4L);

        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setUser(bookingUser);
        booking.setMechanic(mechanic);
        booking.setVehicle(vehicle);
        booking.setService(service);
        booking.setStatus(status);
        booking.setLatitude(18.52);
        booking.setLongitude(73.85);
        return booking;
    }
}
