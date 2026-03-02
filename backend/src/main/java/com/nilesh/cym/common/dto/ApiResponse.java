package com.nilesh.cym.common.dto;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(
        Instant timestamp,
        boolean success,
        String message,
        T data,
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
