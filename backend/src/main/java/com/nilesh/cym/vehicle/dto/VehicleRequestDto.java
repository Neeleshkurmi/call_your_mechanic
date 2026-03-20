package com.nilesh.cym.vehicle.dto;

import com.nilesh.cym.entity.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "VehicleRequest", description = "Vehicle payload used to create or update a user's vehicle.")
public record VehicleRequestDto(
        @NotNull @Schema(example = "CAR", requiredMode = Schema.RequiredMode.REQUIRED) VehicleType vehicleType,
        @NotBlank @Size(max = 80) @Schema(example = "Hyundai", requiredMode = Schema.RequiredMode.REQUIRED) String brand,
        @NotBlank @Size(max = 80) @Schema(example = "i20", requiredMode = Schema.RequiredMode.REQUIRED) String model,
        @NotBlank @Size(max = 30) @Schema(example = "MH14CD5678", requiredMode = Schema.RequiredMode.REQUIRED) String registrationNumber
) {
}
