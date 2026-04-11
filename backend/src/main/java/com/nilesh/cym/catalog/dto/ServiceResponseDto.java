package com.nilesh.cym.catalog.dto;

import com.nilesh.cym.entity.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ServiceResponse", description = "Service catalog item available for a supported vehicle type.")
public record ServiceResponseDto(
        @Schema(description = "Unique identifier of the service catalog item.", example = "4") Long id,
        @Schema(description = "Display name of the service.", example = "Flat Tyre Repair") String name,
        @Schema(description = "Short description of what the service includes.", example = "On-site puncture repair and tyre inflation.") String description,
        @Schema(description = "Vehicle type supported by the service.", example = "BIKE") VehicleType vehicleType,
        @Schema(description = "Base service charge excluding mechanic travel cost.", example = "299.0") Double serviceCharge
) {
}
