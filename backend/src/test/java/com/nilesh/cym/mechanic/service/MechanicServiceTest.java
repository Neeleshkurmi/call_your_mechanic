package com.nilesh.cym.mechanic.service;

import com.nilesh.cym.entity.MechanicEntity;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.mechanic.dto.MechanicProfileRequestDto;
import com.nilesh.cym.mechanic.dto.MechanicRegistrationResponseDto;
import com.nilesh.cym.repository.BookingRepository;
import com.nilesh.cym.repository.MechanicLocationRepository;
import com.nilesh.cym.repository.MechanicRepository;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import com.nilesh.cym.token.JwtService;
import com.nilesh.cym.token.RefreshTokenEntity;
import com.nilesh.cym.token.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MechanicServiceTest {

    @Mock
    private MechanicRepository mechanicRepository;
    @Mock
    private MechanicLocationRepository mechanicLocationRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private MechanicService mechanicService;

    @Test
    void registerMechanic_createsProfileAndRotatesTokens() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(7L, UserRole.USER, "+919900000001");
        MechanicProfileRequestDto request = new MechanicProfileRequestDto(5, "  Tyre change, battery jump start  ", "  Roadside help expert  ");

        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setName("Nilesh");
        user.setMob("+919900000001");
        user.setRole(UserRole.USER);

        JwtService.TokenPair tokenPair = new JwtService.TokenPair(
                "access-token",
                "refresh-token",
                "access-jti",
                "refresh-jti",
                Instant.parse("2026-03-23T10:15:30Z"),
                Instant.parse("2026-03-30T10:15:30Z"),
                null
        );

        when(mechanicRepository.findByUser_Id(7L)).thenReturn(Optional.empty());
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mechanicRepository.save(any(MechanicEntity.class))).thenAnswer(invocation -> {
            MechanicEntity mechanic = invocation.getArgument(0);
            mechanic.setId(11L);
            return mechanic;
        });
        when(refreshTokenRepository.findByUserIdAndRevokedFalseAndExpiresAtAfter(any(Long.class), any(Instant.class))).thenReturn(List.of());
        when(jwtService.issueTokenPair(any(UserEntity.class), any())).thenReturn(tokenPair);

        MechanicRegistrationResponseDto response = mechanicService.registerMechanic(authenticatedUser, request);

        assertEquals(UserRole.MECHANIC, user.getRole());
        assertEquals(11L, response.mechanic().mechanicId());
        assertEquals(false, response.mechanic().available());
        assertEquals("Tyre change, battery jump start", response.mechanic().skills());
        assertEquals("Roadside help expert", response.mechanic().bio());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());

        ArgumentCaptor<RefreshTokenEntity> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        assertEquals(7L, refreshTokenCaptor.getValue().getUserId());
        assertEquals("refresh-jti", refreshTokenCaptor.getValue().getTokenJti());
        assertFalse(refreshTokenCaptor.getValue().isRevoked());
    }

    @Test
    void registerMechanic_rejectsDuplicateProfile() {
        MechanicEntity existingMechanic = new MechanicEntity();
        when(mechanicRepository.findByUser_Id(7L)).thenReturn(Optional.of(existingMechanic));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> mechanicService.registerMechanic(
                        new AuthenticatedUser(7L, UserRole.USER, "+919900000001"),
                        new MechanicProfileRequestDto(3, "Diagnostics", "Mechanic")
                )
        );

        assertEquals(409, exception.getStatusCode().value());
    }
}
