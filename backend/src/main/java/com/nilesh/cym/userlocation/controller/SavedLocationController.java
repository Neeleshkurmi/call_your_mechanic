package com.nilesh.cym.userlocation.controller;

import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiConfig;
import com.nilesh.cym.token.AuthenticatedUser;
import com.nilesh.cym.userlocation.dto.SavedLocationRequestDto;
import com.nilesh.cym.userlocation.dto.SavedLocationResponseDto;
import com.nilesh.cym.userlocation.service.SavedLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/locations")
@Tag(name = "Saved Locations", description = "Protected saved-address endpoints for users.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class SavedLocationController {

    private final SavedLocationService savedLocationService;

    public SavedLocationController(SavedLocationService savedLocationService) {
        this.savedLocationService = savedLocationService;
    }

    @GetMapping
    @Operation(summary = "Get saved locations")
    public ResponseEntity<ApiResponse<List<SavedLocationResponseDto>>> getSavedLocations(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Saved locations fetched successfully", savedLocationService.getSavedLocations(authenticatedUser)));
    }

    @PostMapping
    @Operation(summary = "Save location")
    public ResponseEntity<ApiResponse<SavedLocationResponseDto>> saveLocation(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody SavedLocationRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Saved location added successfully", savedLocationService.saveLocation(authenticatedUser, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete saved location")
    public ResponseEntity<ApiResponse<Void>> deleteLocation(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        savedLocationService.deleteLocation(id, authenticatedUser);
        return ResponseEntity.ok(ApiResponse.success("Saved location deleted successfully"));
    }
}
