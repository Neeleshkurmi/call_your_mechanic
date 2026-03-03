package com.nilesh.cym.booking.dto;

import com.nilesh.cym.entity.enums.BookingStatus;

import java.time.Instant;

public record BookingResponseDto(
        Long bookingId,
        Long userId,
        Long mechanicId,
        Long vehicleId,
        Long serviceId,
        BookingStatus status,
        Instant bookingTime,
        Double latitude,
        Double longitude
) {
}
