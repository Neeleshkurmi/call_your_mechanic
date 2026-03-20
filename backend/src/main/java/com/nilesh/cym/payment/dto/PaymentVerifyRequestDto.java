package com.nilesh.cym.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "PaymentVerifyRequest", description = "Payload used to verify a payment attempt.")
public record PaymentVerifyRequestDto(
        @NotNull @Schema(example = "1") Long bookingId,
        @NotBlank @Schema(example = "PAY-1-1710930000") String reference,
        @NotBlank @Schema(example = "SUCCESS") String status
) {
}
