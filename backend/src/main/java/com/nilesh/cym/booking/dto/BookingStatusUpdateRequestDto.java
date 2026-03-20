package com.nilesh.cym.booking.dto;

import com.nilesh.cym.entity.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "BookingStatusUpdateRequest", description = "Payload used to move a booking to the next public lifecycle state.")
public record BookingStatusUpdateRequestDto(
        @NotNull @Schema(example = "ON_THE_WAY", requiredMode = Schema.RequiredMode.REQUIRED) BookingStatus status
) {
}
