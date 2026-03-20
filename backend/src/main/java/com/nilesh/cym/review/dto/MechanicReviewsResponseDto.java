package com.nilesh.cym.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "MechanicReviewsResponse", description = "Review summary returned for a mechanic.")
public record MechanicReviewsResponseDto(
        @Schema(example = "1") Long mechanicId,
        @Schema(example = "4.75") BigDecimal averageRating,
        @Schema(example = "12") Integer totalReviews,
        List<ReviewResponseDto> reviews
) {
}
