package com.nilesh.cym.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "LocationResponse", description = "Normalized location details returned after location upsert.")
public record LocationResponseDto(
        @Schema(description = "Actor identifier (user or mechanic id depending on endpoint).", example = "42")
        Long actorId,
        @Schema(description = "Latitude in decimal degrees.", example = "18.5204")
        Double latitude,
        @Schema(description = "Longitude in decimal degrees.", example = "73.8567")
        Double longitude,
        @Schema(description = "Server-side timestamp when this location update was persisted.", example = "2026-03-15T12:35:00Z")
        Instant serverTimestamp,
        @Schema(description = "Source/client timestamp sent by the device, if available.", example = "2026-03-15T12:34:56Z")
        Instant sourceTimestamp
) {
}
