package com.nilesh.cym.mechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "NearbyMechanicResponse", description = "Mechanic summary returned by the nearby discovery API.")
public record NearbyMechanicResponseDto(
        @Schema(example = "1") Long mechanicId,
        @Schema(example = "Test Mechanic User") String name,
        @Schema(example = "4.80") BigDecimal rating,
        @Schema(example = "6") Integer experienceYears,
        @Schema(example = "Flat tyre, battery jump start") String skills,
        @Schema(example = "1.25") Double distanceKm,
        @Schema(example = "18.5208") Double latitude,
        @Schema(example = "73.8572") Double longitude
) {
}
