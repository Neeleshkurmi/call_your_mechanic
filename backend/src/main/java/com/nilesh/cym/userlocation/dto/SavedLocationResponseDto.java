package com.nilesh.cym.userlocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "SavedLocationResponse", description = "Saved location details returned for the authenticated user.")
public record SavedLocationResponseDto(
        @Schema(example = "8") Long id,
        @Schema(example = "HOME") String label,
        @Schema(example = "Pune Station Road, Pune") String address,
        @Schema(example = "18.5204") Double latitude,
        @Schema(example = "73.8567") Double longitude,
        @Schema(example = "true") Boolean isDefault,
        @Schema(example = "2026-03-20T10:15:30Z") Instant createdAt,
        @Schema(example = "2026-03-20T10:15:30Z") Instant updatedAt
) {
}
