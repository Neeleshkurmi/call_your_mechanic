package com.nilesh.cym.auth.controller;

import com.nilesh.cym.auth.dto.ProfileUpdateRequestDto;
import com.nilesh.cym.auth.dto.ProfileUpdateResponseDto;
import com.nilesh.cym.auth.service.UserProfileService;
import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiConfig;
import com.nilesh.cym.config.OpenApiSchemas;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.token.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users/me")
@Tag(name = "Profile", description = "Authenticated current-user profile endpoints.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping("/profile")
    @Operation(summary = "Update current user profile", description = "Updates the authenticated user's name and role, then returns fresh JWT tokens.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile updated successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ProfileUpdateApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or role update rejected", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user cannot choose the requested role", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Current user not found", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProfileUpdateResponseDto>> updateCurrentProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ProfileUpdateRequestDto request
    ) {
        log.info("endpoint_request name=updateCurrentProfile principal={} requestedRole={} nameLength={}",
                LogSanitizer.summarizePrincipal(authenticatedUser),
                request.getRole(),
                request.getName() == null ? 0 : request.getName().trim().length());
        ProfileUpdateResponseDto response = userProfileService.updateCurrentProfile(authenticatedUser, request);
        log.info("endpoint_success name=updateCurrentProfile userId={} role={}",
                response.user().userId(),
                response.user().role());
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", response));
    }
}
