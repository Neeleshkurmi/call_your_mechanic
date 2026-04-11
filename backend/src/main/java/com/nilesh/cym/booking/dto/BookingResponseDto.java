package com.nilesh.cym.booking.dto;

import com.nilesh.cym.entity.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "BookingResponse", description = "Booking details returned by booking management endpoints.")
public record BookingResponseDto(
        @Schema(description = "Unique booking identifier.", example = "101") Long bookingId,
        @Schema(description = "User who created the booking.", example = "42") Long userId,
        @Schema(description = "Mechanic assigned to the booking.", example = "12") Long mechanicId,
        @Schema(description = "Vehicle associated with the booking.", example = "18") Long vehicleId,
        @Schema(description = "Service requested for the booking.", example = "4") Long serviceId,
        @Schema(description = "Current lifecycle status of the booking.", example = "REQUESTED") BookingStatus status,
        @Schema(description = "Timestamp when the booking was created.", example = "2026-03-15T12:34:56Z") Instant bookingTime,
        @Schema(description = "Latitude of the requested service location.", example = "18.5204") Double latitude,
        @Schema(description = "Longitude of the requested service location.", example = "73.8567") Double longitude,
        @Schema(description = "Distance the mechanic is expected to travel for this booking in kilometers.", example = "6.5") Double travelDistanceKm,
        @Schema(description = "Fare component for mechanic travel.", example = "117.0") Double travelCharge,
        @Schema(description = "Base service charge for the selected service.", example = "299.0") Double serviceCharge,
        @Schema(description = "Total fare charged for the booking.", example = "416.0") Double totalFare,
        @Schema(description = "Whether the booking already has a submitted mechanic rating.", example = "false") boolean reviewSubmitted
) {
}
