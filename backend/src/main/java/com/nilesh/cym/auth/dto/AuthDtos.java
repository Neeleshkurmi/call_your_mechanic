package com.nilesh.cym.auth.dto;

public class AuthDtos {

    public record RefreshRequest(String refreshToken, String deviceSession) {
    }

    public record LogoutRequest(String refreshToken) {
    }

    public record TokenResponse(String accessToken, String refreshToken, long accessTokenExpiresAt, long refreshTokenExpiresAt) {
    }
}
