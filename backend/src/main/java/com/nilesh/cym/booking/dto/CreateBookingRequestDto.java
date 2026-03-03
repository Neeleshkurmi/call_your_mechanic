package com.nilesh.cym.booking.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBookingRequestDto(
        @NotNull Long mechanicId,
        @NotNull Long vehicleId,
        @NotNull Long serviceId,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
