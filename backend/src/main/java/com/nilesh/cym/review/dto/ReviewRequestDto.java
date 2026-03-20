package com.nilesh.cym.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "ReviewRequest", description = "Payload used by the user to rate a completed booking.")
public record ReviewRequestDto(
        @NotNull @Min(1) @Max(5) @Schema(example = "5", requiredMode = Schema.RequiredMode.REQUIRED) Integer rating,
        @Size(max = 1000) @Schema(example = "Quick arrival and fixed the issue properly.") String review
) {
}
