package com.nilesh.cym.location.service;

import com.nilesh.cym.entity.MechanicEntity;
import com.nilesh.cym.entity.MechanicLocationEntity;
import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.UserLocationEntity;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.location.dto.LocationResponseDto;
import com.nilesh.cym.location.dto.LocationUpdateRequestDto;
import com.nilesh.cym.repository.MechanicLocationRepository;
import com.nilesh.cym.repository.MechanicRepository;
import com.nilesh.cym.repository.UserLocationRepository;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LocationService {

    private final MechanicRepository mechanicRepository;
    private final UserRepository userRepository;
    private final MechanicLocationRepository mechanicLocationRepository;
    private final UserLocationRepository userLocationRepository;

    public LocationService(
            MechanicRepository mechanicRepository,
            UserRepository userRepository,
            MechanicLocationRepository mechanicLocationRepository,
            UserLocationRepository userLocationRepository
    ) {
        this.mechanicRepository = mechanicRepository;
        this.userRepository = userRepository;
        this.mechanicLocationRepository = mechanicLocationRepository;
        this.userLocationRepository = userLocationRepository;
    }

    public LocationResponseDto updateMechanicLocation(AuthenticatedUser authenticatedUser, LocationUpdateRequestDto request) {
        requireRole(authenticatedUser, UserRole.MECHANIC);

        MechanicEntity mechanic = mechanicRepository.findByUser_Id(authenticatedUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic profile not found"));

        MechanicLocationEntity location = new MechanicLocationEntity();
        location.setMechanic(mechanic);
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());

        MechanicLocationEntity saved = mechanicLocationRepository.save(location);
        return new LocationResponseDto(
                mechanic.getId(),
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getRecordedAt(),
                request.timestamp()
        );
    }

    public LocationResponseDto updateUserLocation(AuthenticatedUser authenticatedUser, LocationUpdateRequestDto request) {
        requireRole(authenticatedUser, UserRole.USER);

        UserEntity user = userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserLocationEntity location = new UserLocationEntity();
        location.setUser(user);
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setAddress("N/A");

        UserLocationEntity saved = userLocationRepository.save(location);
        return new LocationResponseDto(
                user.getId(),
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getCreatedAt(),
                request.timestamp()
        );
    }

    private void requireRole(AuthenticatedUser authenticatedUser, UserRole requiredRole) {
        if (authenticatedUser == null || authenticatedUser.role() != requiredRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        }
    }
}
