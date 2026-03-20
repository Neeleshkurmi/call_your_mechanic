package com.nilesh.cym.vehicle.controller;

import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiConfig;
import com.nilesh.cym.token.AuthenticatedUser;
import com.nilesh.cym.vehicle.dto.VehicleRequestDto;
import com.nilesh.cym.vehicle.dto.VehicleResponseDto;
import com.nilesh.cym.vehicle.service.VehicleService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles", description = "Protected vehicle management endpoints for users.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    @Operation(summary = "Get current user's vehicles")
    public ResponseEntity<ApiResponse<List<VehicleResponseDto>>> getVehicles(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Vehicles fetched successfully", vehicleService.getVehicles(authenticatedUser)));
    }

    @PostMapping
    @Operation(summary = "Add vehicle")
    public ResponseEntity<ApiResponse<VehicleResponseDto>> addVehicle(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody VehicleRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle added successfully", vehicleService.addVehicle(authenticatedUser, request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle")
    public ResponseEntity<ApiResponse<VehicleResponseDto>> updateVehicle(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody VehicleRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", vehicleService.updateVehicle(id, authenticatedUser, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        vehicleService.deleteVehicle(id, authenticatedUser);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully"));
    }
}
