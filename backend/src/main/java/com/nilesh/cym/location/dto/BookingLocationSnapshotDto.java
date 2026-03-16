package com.nilesh.cym.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BookingLocationSnapshotDto(
        @Schema(description = "Booking id", example = "42")
        Long bookingId,
        @Schema(description = "Latest user location")
        LocationResponseDto userLocation,
        @Schema(description = "Latest mechanic location")
        LocationResponseDto mechanicLocation
) {
}
