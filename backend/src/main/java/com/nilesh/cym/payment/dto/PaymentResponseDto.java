package com.nilesh.cym.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "PaymentResponse", description = "Payment state returned by payment APIs.")
public record PaymentResponseDto(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long bookingId,
        @Schema(example = "PAY-1-1710930000") String reference,
        @Schema(example = "499.0") Double amount,
        @Schema(example = "PENDING") String status,
        @Schema(example = "2026-03-20T12:00:00Z") Instant createdAt,
        @Schema(example = "2026-03-20T12:05:00Z") Instant updatedAt
) {
}
