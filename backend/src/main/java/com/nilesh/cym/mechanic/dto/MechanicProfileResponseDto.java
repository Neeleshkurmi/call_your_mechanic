package com.nilesh.cym.mechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "MechanicProfileResponse", description = "Mechanic profile details returned by public and self-service APIs.")
public record MechanicProfileResponseDto(
        @Schema(example = "3") Long mechanicId,
        @Schema(example = "2") Long userId,
        @Schema(example = "Test Mechanic User") String name,
        @Schema(example = "+919900000002") String mobile,
        @Schema(example = "true") Boolean available,
        @Schema(example = "6") Integer experienceYears,
        @Schema(example = "4.75") BigDecimal rating,
        @Schema(example = "Flat tyre, battery jump start, engine diagnostics") String skills,
        @Schema(example = "Roadside mechanic with 6 years of hatchback and bike servicing experience") String bio
) {
}
