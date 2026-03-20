package com.nilesh.cym.vehicle.service;

import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.entity.enums.VehicleType;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.repository.VehicleRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import com.nilesh.cym.vehicle.dto.VehicleRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void addVehicle_rejectsDuplicateRegistrationNumber() {
        when(vehicleRepository.existsByRegistrationNumber("MH14CD5678")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> vehicleService.addVehicle(
                        new AuthenticatedUser(1L, UserRole.USER, "+919900000001"),
                        new VehicleRequestDto(VehicleType.CAR, "Hyundai", "i20", "mh14cd5678")
                )
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}
