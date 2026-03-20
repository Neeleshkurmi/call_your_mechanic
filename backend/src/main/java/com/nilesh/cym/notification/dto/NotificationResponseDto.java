package com.nilesh.cym.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "NotificationResponse", description = "Notification item returned for the authenticated actor.")
public record NotificationResponseDto(
        @Schema(example = "booking-1") String id,
        @Schema(example = "Booking 1 is currently ACCEPTED") String message,
        @Schema(example = "BOOKING_STATUS") String type,
        @Schema(example = "2026-03-20T12:00:00Z") Instant createdAt
) {
}
