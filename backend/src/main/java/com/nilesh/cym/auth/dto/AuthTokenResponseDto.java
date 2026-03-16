package com.nilesh.cym.auth.dto;

import com.nilesh.cym.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthTokenResponse", description = "Authentication result returned after successful OTP verification.")
public class AuthTokenResponseDto {

    @Schema(description = "JWT access token used for authenticated requests.", example = "eyJhbGciOiJIUzI1NiJ9.access")
    private String accessToken;
    @Schema(description = "Refresh token used to obtain a new access token.", example = "refresh-token-value")
    private String refreshToken;
    @Schema(description = "Authentication scheme for the access token.", example = "Bearer")
    private String tokenType = "Bearer";
    @Schema(description = "Unique identifier of the authenticated user.", example = "42")
    private Long userId;
    @Schema(description = "Verified mobile number of the authenticated user.", example = "+919876543210")
    private String mobile;
    @Schema(description = "Role assigned to the authenticated user.", example = "USER")
    private UserRole role;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
