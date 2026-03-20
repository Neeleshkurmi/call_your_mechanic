package com.nilesh.cym.userlocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "SavedLocationRequest", description = "Payload used to create a saved user location.")
public record SavedLocationRequestDto(
        @NotBlank @Size(max = 80) @Schema(example = "HOME", requiredMode = Schema.RequiredMode.REQUIRED) String label,
        @NotBlank @Size(max = 300) @Schema(example = "Pune Station Road, Pune", requiredMode = Schema.RequiredMode.REQUIRED) String address,
        @NotNull @Schema(example = "18.5204", requiredMode = Schema.RequiredMode.REQUIRED) Double latitude,
        @NotNull @Schema(example = "73.8567", requiredMode = Schema.RequiredMode.REQUIRED) Double longitude,
        @Schema(example = "true") Boolean isDefault
) {
}
