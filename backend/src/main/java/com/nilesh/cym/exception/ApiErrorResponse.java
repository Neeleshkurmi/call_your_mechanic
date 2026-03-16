package com.nilesh.cym.exception;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(name = "LegacyApiErrorResponse", description = "Legacy standalone error payload retained for compatibility.")
public record ApiErrorResponse(
        @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Validation failed") String message,
        @Schema(example = "/api/v1/auth/otp/request") String path,
        @ArraySchema(schema = @Schema(example = "mobile: Mobile must be in E.164 or digits format")) List<String> details
) {
}
