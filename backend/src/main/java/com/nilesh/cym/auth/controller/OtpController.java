package com.nilesh.cym.auth.controller;

import com.nilesh.cym.auth.dto.AuthTokenResponseDto;
import com.nilesh.cym.auth.dto.OtpRequestDto;
import com.nilesh.cym.auth.dto.RoleUpdateRequestDto;
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
@RequestMapping("/api/v1/auth")
public class OtpController {

    private final OtpAuthService otpAuthService;

    public OtpController(OtpAuthService otpAuthService, AuthService authService) {
        this.otpAuthService = otpAuthService;
    }

    @PostMapping("/otp/request")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        log.debug("calling request otp for mobile number {}", request.getMobile());
        otpAuthService.requestOtp(request.getMobile());
        return successResponse("OTP sent successfully");
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthTokenResponseDto>> verifyOtp(@Valid @RequestBody OtpVerifyDto request) {
        return successResponse("OTP verified successfully", otpAuthService.verifyOtp(request));
    }

    @PostMapping("/role")
    public ResponseEntity<ApiResponse<Void>> updateRole(@Valid @RequestBody RoleUpdateRequestDto request) {
        otpAuthService.updateRole(request);
        return successResponse("User role updated successfully");
    }

    private ResponseEntity<ApiResponse<Void>> successResponse(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    private <T> ResponseEntity<ApiResponse<T>> successResponse(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
}
