package com.nilesh.cym.auth.controller;

import com.nilesh.cym.auth.dto.AuthDtos;
import com.nilesh.cym.auth.dto.AuthTokenResponseDto;
import com.nilesh.cym.auth.dto.OtpRequestDto;
import com.nilesh.cym.auth.dto.OtpVerifyDto;
import com.nilesh.cym.auth.service.AuthService;
import com.nilesh.cym.auth.service.OtpAuthService;
import com.nilesh.cym.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/otp")
public class AuthController {

    private final OtpAuthService otpAuthService;
    private final AuthService authService;

    public AuthController(OtpAuthService otpAuthService, AuthService authService) {
        this.otpAuthService = otpAuthService;
        this.authService = authService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        log.debug("calling request otp for mobile number {}", request.getMobile());
        otpAuthService.requestOtp(request.getMobile());
        return successResponse("OTP sent successfully");
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<AuthTokenResponseDto>> verifyOtp(@Valid @RequestBody OtpVerifyDto request) {
        return successResponse("OTP verified successfully", otpAuthService.verifyOtp(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDtos.TokenResponse>> refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return successResponse("Token refreshed successfully", authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody AuthDtos.LogoutRequest request) {
        authService.logout(request);
        return successResponse("Logged out successfully");
    }

    private ResponseEntity<ApiResponse<Void>> successResponse(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    private <T> ResponseEntity<ApiResponse<T>> successResponse(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
}
