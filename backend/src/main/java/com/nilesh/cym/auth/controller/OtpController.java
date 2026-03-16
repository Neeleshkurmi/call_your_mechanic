package com.nilesh.cym.auth.controller;

import com.nilesh.cym.auth.dto.AuthTokenResponseDto;
import com.nilesh.cym.auth.dto.OtpRequestDto;
import com.nilesh.cym.auth.dto.OtpVerifyDto;
import com.nilesh.cym.auth.dto.RoleUpdateRequestDto;
import com.nilesh.cym.auth.service.AuthService;
import com.nilesh.cym.auth.service.OtpAuthService;
import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiSchemas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Public OTP-based authentication and role selection endpoints.")
public class OtpController {

    private final OtpAuthService otpAuthService;

    public OtpController(OtpAuthService otpAuthService, AuthService authService) {
        this.otpAuthService = otpAuthService;
    }

    @PostMapping("/otp/request")
    @Operation(summary = "Request OTP", description = "Sends a one-time password to the supplied mobile number.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.VoidApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or OTP request rejected", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        log.debug("calling request otp for mobile number {}", request.getMobile());
        otpAuthService.requestOtp(request.getMobile());
        return successResponse("OTP sent successfully");
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP", description = "Verifies the OTP and returns access and refresh tokens for the user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verified successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.AuthTokenApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or OTP is invalid", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AuthTokenResponseDto>> verifyOtp(@Valid @RequestBody OtpVerifyDto request) {
        return successResponse("OTP verified successfully", otpAuthService.verifyOtp(request));
    }

    @PostMapping("/role")
    @Operation(summary = "Update user role", description = "Updates the role associated with a mobile number.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User role updated successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.VoidApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or role update request rejected", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
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
