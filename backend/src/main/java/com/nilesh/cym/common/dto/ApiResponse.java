package com.nilesh.cym.common.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(name = "ApiResponse", description = "Standard response envelope returned by all API endpoints.")
public record ApiResponse<T>(
        @Schema(description = "Timestamp when the response was generated.", example = "2026-03-15T12:34:56Z")
        Instant timestamp,
        @Schema(description = "Indicates whether the operation succeeded.", example = "true")
        boolean success,
        @Schema(description = "Human-readable summary of the outcome.", example = "Booking created successfully")
        String message,
        @Schema(description = "Payload returned for successful responses. Null for error responses.")
        T data,
        @ArraySchema(arraySchema = @Schema(description = "Validation or error messages."), schema = @Schema(example = "mobile: Mobile must be in E.164 or digits format"))
        List<String> errors
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(Instant.now(), true, message, data, List.of());
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(Instant.now(), true, message, null, List.of());
    }

    public static ApiResponse<Void> error(String message, List<String> errors) {
        return new ApiResponse<>(Instant.now(), false, message, null, errors == null ? List.of() : errors);
    }
}
