package com.nilesh.cym.vehicle.service;

import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.VehicleEntity;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.repository.VehicleRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import com.nilesh.cym.vehicle.dto.VehicleRequestDto;
import com.nilesh.cym.vehicle.dto.VehicleResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleService(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDto> getVehicles(AuthenticatedUser authenticatedUser) {
        Long userId = requireUser(authenticatedUser);
        return vehicleRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VehicleResponseDto addVehicle(AuthenticatedUser authenticatedUser, VehicleRequestDto request) {
        Long userId = requireUser(authenticatedUser);
        validateRegistrationNumber(request.registrationNumber(), null);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setUser(user);
        applyRequest(vehicle, request);
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponseDto updateVehicle(Long vehicleId, AuthenticatedUser authenticatedUser, VehicleRequestDto request) {
        Long userId = requireUser(authenticatedUser);
        VehicleEntity vehicle = vehicleRepository.findByIdAndUser_Id(vehicleId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        validateRegistrationNumber(request.registrationNumber(), vehicleId);
        applyRequest(vehicle, request);
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void deleteVehicle(Long vehicleId, AuthenticatedUser authenticatedUser) {
        Long userId = requireUser(authenticatedUser);
        VehicleEntity vehicle = vehicleRepository.findByIdAndUser_Id(vehicleId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        vehicleRepository.delete(vehicle);
    }

    private void applyRequest(VehicleEntity vehicle, VehicleRequestDto request) {
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setBrand(request.brand().trim());
        vehicle.setModel(request.model().trim());
        vehicle.setRegistrationNumber(request.registrationNumber().trim().toUpperCase());
    }

    private void validateRegistrationNumber(String registrationNumber, Long vehicleId) {
        String normalized = registrationNumber.trim().toUpperCase();
        boolean exists = vehicleId == null
                ? vehicleRepository.existsByRegistrationNumber(normalized)
                : vehicleRepository.existsByRegistrationNumberAndIdNot(normalized, vehicleId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration number already exists");
        }
    }

    private Long requireUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.role() != UserRole.USER) {
            log.warn("vehicle_role_rejected principal={}", LogSanitizer.summarizePrincipal(authenticatedUser));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        }
        return authenticatedUser.userId();
    }

    private VehicleResponseDto toResponse(VehicleEntity vehicle) {
        return new VehicleResponseDto(
                vehicle.getId(),
                vehicle.getVehicleType(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getRegistrationNumber(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
