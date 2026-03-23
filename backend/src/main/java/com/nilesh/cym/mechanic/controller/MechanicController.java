package com.nilesh.cym.mechanic.controller;

import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiConfig;
import com.nilesh.cym.mechanic.dto.MechanicAvailabilityRequestDto;
import com.nilesh.cym.mechanic.dto.MechanicEarningsResponseDto;
import com.nilesh.cym.mechanic.dto.MechanicProfileRequestDto;
import com.nilesh.cym.mechanic.dto.MechanicProfileResponseDto;
import com.nilesh.cym.mechanic.dto.MechanicRegistrationResponseDto;
import com.nilesh.cym.mechanic.dto.NearbyMechanicResponseDto;
import com.nilesh.cym.mechanic.service.MechanicService;
import com.nilesh.cym.token.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mechanics")
@Tag(name = "Mechanics", description = "Protected mechanic discovery and self-service endpoints.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class MechanicController {

    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register current user as mechanic")
    public ResponseEntity<ApiResponse<MechanicRegistrationResponseDto>> registerMechanic(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody MechanicProfileRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Mechanic registered successfully",
                mechanicService.registerMechanic(authenticatedUser, request)
        ));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get nearby mechanics")
    public ResponseEntity<ApiResponse<List<NearbyMechanicResponseDto>>> getNearbyMechanics(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Nearby mechanics fetched successfully",
                mechanicService.getNearbyMechanics(authenticatedUser, lat, lng)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mechanic details")
    public ResponseEntity<ApiResponse<MechanicProfileResponseDto>> getMechanicDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Mechanic details fetched successfully", mechanicService.getMechanicDetails(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my mechanic profile")
    public ResponseEntity<ApiResponse<MechanicProfileResponseDto>> getMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Mechanic profile fetched successfully", mechanicService.getMyProfile(authenticatedUser)));
    }

    @PatchMapping("/me/availability")
    @Operation(summary = "Update mechanic availability")
    public ResponseEntity<ApiResponse<MechanicProfileResponseDto>> updateAvailability(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody MechanicAvailabilityRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Mechanic availability updated successfully",
                mechanicService.updateAvailability(authenticatedUser, request)
        ));
    }

    @PutMapping("/me")
    @Operation(summary = "Update mechanic profile")
    public ResponseEntity<ApiResponse<MechanicProfileResponseDto>> updateProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody MechanicProfileRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Mechanic profile updated successfully",
                mechanicService.updateProfile(authenticatedUser, request)
        ));
    }

    @GetMapping("/me/earnings")
    @Operation(summary = "Get mechanic earnings")
    public ResponseEntity<ApiResponse<MechanicEarningsResponseDto>> getEarnings(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Mechanic earnings fetched successfully", mechanicService.getEarnings(authenticatedUser)));
    }
}
