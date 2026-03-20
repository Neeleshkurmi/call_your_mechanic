package com.nilesh.cym.vehicle.dto;

import com.nilesh.cym.entity.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "VehicleResponse", description = "Vehicle details returned to the authenticated user.")
public record VehicleResponseDto(
        @Schema(example = "10") Long id,
        @Schema(example = "CAR") VehicleType vehicleType,
        @Schema(example = "Hyundai") String brand,
        @Schema(example = "i20") String model,
        @Schema(example = "MH14CD5678") String registrationNumber,
        @Schema(example = "2026-03-20T10:15:30Z") Instant createdAt,
        @Schema(example = "2026-03-20T10:15:30Z") Instant updatedAt
) {
}
