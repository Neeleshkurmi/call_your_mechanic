package com.nilesh.cym.auth.controller;

import com.nilesh.cym.auth.dto.AuthDtos;
import com.nilesh.cym.auth.service.AuthService;
import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiConfig;
import com.nilesh.cym.config.OpenApiSchemas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
@Tag(name = "Sessions", description = "Token lifecycle endpoints.")
public class SessionController {

    private final AuthService authService;

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchanges a refresh token for a new access and refresh token pair.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.RefreshTokenApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Refresh token is invalid or malformed", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AuthDtos.TokenResponse>> refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return successResponse("Token refreshed successfully", authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout session", description = "Revokes the provided refresh token and invalidates the active session.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged out successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.VoidApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Logout request is invalid", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to logout this session", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
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
