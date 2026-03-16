package com.nilesh.cym.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CreateBookingRequest", description = "Request payload used by a user to create a mechanic booking.")
public record CreateBookingRequestDto(
        @NotNull @Schema(description = "Mechanic selected for the booking.", example = "12", requiredMode = Schema.RequiredMode.REQUIRED) Long mechanicId,
        @NotNull @Schema(description = "Vehicle for which the service is requested.", example = "18", requiredMode = Schema.RequiredMode.REQUIRED) Long vehicleId,
        @NotNull @Schema(description = "Service catalog item requested by the user.", example = "4", requiredMode = Schema.RequiredMode.REQUIRED) Long serviceId,
        @NotNull @Schema(description = "Latitude of the service location.", example = "18.5204", requiredMode = Schema.RequiredMode.REQUIRED) Double latitude,
        @NotNull @Schema(description = "Longitude of the service location.", example = "73.8567", requiredMode = Schema.RequiredMode.REQUIRED) Double longitude
) {
}
