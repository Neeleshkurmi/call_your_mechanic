package com.nilesh.cym.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record BookingLocationHistoryDto(
        @Schema(description = "Booking id", example = "42")
        Long bookingId,
        @Schema(description = "Inclusive lower-bound timestamp used for filtering history points", example = "2026-03-15T12:00:00Z")
        Instant since,
        @Schema(description = "Maximum points requested per actor stream", example = "50")
        Integer limit,
        @Schema(description = "User location points ordered by newest first")
        List<LocationResponseDto> userLocation,
        @Schema(description = "Mechanic location points ordered by newest first")
        List<LocationResponseDto> mechanicLocation
) {
}
