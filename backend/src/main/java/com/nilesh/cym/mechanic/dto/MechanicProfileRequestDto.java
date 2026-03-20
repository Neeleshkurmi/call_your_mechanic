package com.nilesh.cym.mechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "MechanicProfileRequest", description = "Payload used to update mechanic profile details.")
public record MechanicProfileRequestDto(
        @Min(0) @Schema(example = "6") Integer experienceYears,
        @NotBlank @Size(max = 500) @Schema(example = "Flat tyre, battery jump start, engine diagnostics") String skills,
        @NotBlank @Size(max = 1000) @Schema(example = "Roadside mechanic with 6 years of hatchback and bike servicing experience") String bio
) {
}
