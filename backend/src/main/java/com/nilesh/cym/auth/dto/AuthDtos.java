package com.nilesh.cym.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AuthDtos {

    @Schema(name = "RefreshRequest", description = "Request payload used to exchange a refresh token for a new token pair.")
    public record RefreshRequest(
            @Schema(description = "Refresh token previously issued to the client.", example = "refresh-token-value", requiredMode = Schema.RequiredMode.REQUIRED)
            String refreshToken,
            @Schema(description = "Optional device session identifier associated with the refresh token.", example = "android-pixel-8")
            String deviceSession
    ) {
    }

    @Schema(name = "LogoutRequest", description = "Request payload used to revoke a refresh token and end the session.")
    public record LogoutRequest(
            @Schema(description = "Refresh token to revoke.", example = "refresh-token-value", requiredMode = Schema.RequiredMode.REQUIRED)
            String refreshToken
    ) {
    }

    @Schema(name = "TokenResponse", description = "Token pair returned after a successful refresh operation.")
    public record TokenResponse(
            @Schema(description = "New JWT access token.", example = "eyJhbGciOiJIUzI1NiJ9.access")
            String accessToken,
            @Schema(description = "New refresh token.", example = "refresh-token-value")
            String refreshToken,
            @Schema(description = "Unix epoch timestamp in milliseconds when the access token expires.", example = "1760000000000")
            long accessTokenExpiresAt,
            @Schema(description = "Unix epoch timestamp in milliseconds when the refresh token expires.", example = "1762592000000")
            long refreshTokenExpiresAt
    ) {
    }
}
