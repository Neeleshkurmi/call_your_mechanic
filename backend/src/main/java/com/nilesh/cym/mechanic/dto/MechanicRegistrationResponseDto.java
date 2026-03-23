package com.nilesh.cym.mechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MechanicRegistrationResponse", description = "Registration result returned after a user completes mechanic onboarding.")
public record MechanicRegistrationResponseDto(
        @Schema(description = "Created mechanic profile details.")
        MechanicProfileResponseDto mechanic,
        @Schema(description = "Fresh JWT access token with MECHANIC role.", example = "eyJhbGciOiJIUzI1NiJ9.access")
        String accessToken,
        @Schema(description = "Fresh refresh token for future access token rotation.", example = "refresh-token-value")
        String refreshToken,
        @Schema(description = "Access token expiry in epoch milliseconds.", example = "1770000000000")
        long accessTokenExpiresAt,
        @Schema(description = "Refresh token expiry in epoch milliseconds.", example = "1772592000000")
        long refreshTokenExpiresAt
) {
}
