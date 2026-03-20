package com.nilesh.cym.mechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MechanicAvailabilityRequest", description = "Payload used by a mechanic to toggle availability.")
public record MechanicAvailabilityRequestDto(
        @NotNull @Schema(example = "true", requiredMode = Schema.RequiredMode.REQUIRED) Boolean available
) {
}
