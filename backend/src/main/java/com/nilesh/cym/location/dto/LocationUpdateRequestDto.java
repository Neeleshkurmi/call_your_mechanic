package com.nilesh.cym.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record LocationUpdateRequestDto(
        @NotNull
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        @Schema(description = "Latitude", example = "18.5204", requiredMode = Schema.RequiredMode.REQUIRED)
        Double latitude,

        @NotNull
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        @Schema(description = "Longitude", example = "73.8567", requiredMode = Schema.RequiredMode.REQUIRED)
        Double longitude,

        @Schema(description = "Source/client timestamp sent by device.", example = "2026-03-15T12:34:56Z")
        Instant timestamp
) {
}
