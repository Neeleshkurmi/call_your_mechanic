package com.nilesh.cym.mechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MechanicEarningsResponse", description = "Simple earnings summary for the authenticated mechanic.")
public record MechanicEarningsResponseDto(
        @Schema(example = "1") Long mechanicId,
        @Schema(example = "2") Integer completedJobs,
        @Schema(example = "998.0") Double totalEarnings
) {
}
