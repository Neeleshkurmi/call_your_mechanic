package com.nilesh.cym.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "PaymentCreateRequest", description = "Payload used to initiate a payment for a booking.")
public record PaymentCreateRequestDto(
        @NotNull @Schema(example = "1") Long bookingId
) {
}
