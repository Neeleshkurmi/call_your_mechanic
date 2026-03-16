package com.nilesh.cym.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProfileUpdateResponse", description = "Updated profile details and fresh JWT tokens returned after a successful profile update.")
public record ProfileUpdateResponseDto(
        CurrentUserProfileDto user,
        @Schema(description = "New JWT access token for the updated user.", example = "eyJhbGciOiJIUzI1NiJ9.access")
        String accessToken,
        @Schema(description = "New refresh token for the updated user.", example = "refresh-token-value")
        String refreshToken,
        @Schema(description = "Unix epoch timestamp in milliseconds when the access token expires.", example = "1760000000000")
        long accessTokenExpiresAt,
        @Schema(description = "Unix epoch timestamp in milliseconds when the refresh token expires.", example = "1762592000000")
        long refreshTokenExpiresAt
) {
}
