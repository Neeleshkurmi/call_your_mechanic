package com.nilesh.cym.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "ReviewResponse", description = "Review details returned by review APIs.")
public record ReviewResponseDto(
        @Schema(example = "12") Long reviewId,
        @Schema(example = "2") Long bookingId,
        @Schema(example = "1") Long mechanicId,
        @Schema(example = "1") Long userId,
        @Schema(example = "5") Integer rating,
        @Schema(example = "Quick arrival and fixed the issue properly.") String review,
        @Schema(example = "2026-03-20T10:15:30Z") Instant createdAt
) {
}
