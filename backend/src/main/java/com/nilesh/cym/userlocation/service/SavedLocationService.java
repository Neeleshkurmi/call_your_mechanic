package com.nilesh.cym.userlocation.service;

import com.nilesh.cym.entity.UserEntity;
import com.nilesh.cym.entity.UserLocationEntity;
import com.nilesh.cym.entity.enums.UserRole;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.repository.UserLocationRepository;
import com.nilesh.cym.repository.UserRepository;
import com.nilesh.cym.token.AuthenticatedUser;
import com.nilesh.cym.userlocation.dto.SavedLocationRequestDto;
import com.nilesh.cym.userlocation.dto.SavedLocationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class SavedLocationService {

    private final UserLocationRepository userLocationRepository;
    private final UserRepository userRepository;

    public SavedLocationService(UserLocationRepository userLocationRepository, UserRepository userRepository) {
        this.userLocationRepository = userLocationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<SavedLocationResponseDto> getSavedLocations(AuthenticatedUser authenticatedUser) {
        Long userId = requireUser(authenticatedUser);
        return userLocationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SavedLocationResponseDto saveLocation(AuthenticatedUser authenticatedUser, SavedLocationRequestDto request) {
        Long userId = requireUser(authenticatedUser);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (Boolean.TRUE.equals(request.isDefault())) {
            clearDefaultLocations(userId);
        }

        UserLocationEntity location = new UserLocationEntity();
        location.setUser(user);
        location.setLabel(request.label().trim());
        location.setAddress(request.address().trim());
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setDefault(Boolean.TRUE.equals(request.isDefault()));
        return toResponse(userLocationRepository.save(location));
    }

    @Transactional
    public void deleteLocation(Long locationId, AuthenticatedUser authenticatedUser) {
        Long userId = requireUser(authenticatedUser);
        UserLocationEntity location = userLocationRepository.findByIdAndUser_Id(locationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved location not found"));
        userLocationRepository.delete(location);
    }

    private void clearDefaultLocations(Long userId) {
        List<UserLocationEntity> locations = userLocationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        locations.stream()
                .filter(saved -> Boolean.TRUE.equals(saved.getDefault()))
                .forEach(saved -> saved.setDefault(Boolean.FALSE));
        userLocationRepository.saveAll(locations);
    }

    private Long requireUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.role() != UserRole.USER) {
            log.warn("saved_location_role_rejected principal={}", LogSanitizer.summarizePrincipal(authenticatedUser));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role to access this resource");
        }
        return authenticatedUser.userId();
    }

    private SavedLocationResponseDto toResponse(UserLocationEntity location) {
        return new SavedLocationResponseDto(
                location.getId(),
                location.getLabel(),
                location.getAddress(),
                location.getLatitude(),
                location.getLongitude(),
                location.getDefault(),
                location.getCreatedAt(),
                location.getUpdatedAt()
        );
    }
}
