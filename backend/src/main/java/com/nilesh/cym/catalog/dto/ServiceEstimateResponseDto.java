package com.nilesh.cym.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ServiceEstimateResponse", description = "Basic estimate details returned for a service.")
public record ServiceEstimateResponseDto(
        @Schema(example = "1") Long serviceId,
        @Schema(example = "Flat Tyre Repair") String serviceName,
        @Schema(example = "399.00") Double estimatedAmount,
        @Schema(example = "30") Integer estimatedDurationMinutes,
        @Schema(example = "Basic service estimate generated successfully") String note
) {
}
