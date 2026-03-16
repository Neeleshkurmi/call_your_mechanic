package com.nilesh.cym.location.controller;

import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiConfig;
import com.nilesh.cym.config.OpenApiSchemas;
import com.nilesh.cym.logging.LogSanitizer;
import com.nilesh.cym.location.dto.LocationResponseDto;
import com.nilesh.cym.location.dto.LocationUpdateRequestDto;
import com.nilesh.cym.location.service.LocationService;
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
@RequestMapping("/api/v1")
@Tag(name = "Locations", description = "Protected location update endpoints for users and mechanics.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/mechanics/me/location")
    @Operation(summary = "Update mechanic location", description = "Stores the latest mechanic location for the authenticated mechanic.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mechanic location updated successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.LocationApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed for location coordinates", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user does not have mechanic role", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Mechanic profile not found", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<LocationResponseDto>> updateMechanicLocation(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody LocationUpdateRequestDto request
    ) {
        log.info("endpoint_request name=updateMechanicLocation principal={} lat={} lon={} hasTimestamp={}",
                LogSanitizer.summarizePrincipal(authenticatedUser),
                request.latitude(),
                request.longitude(),
                request.timestamp() != null);
        LocationResponseDto response = locationService.updateMechanicLocation(authenticatedUser, request);
        log.info("endpoint_success name=updateMechanicLocation actorId={} serverTimestamp={}",
                response.actorId(),
                response.serverTimestamp());
        return ResponseEntity.ok(ApiResponse.success("Mechanic location updated successfully", response));
    }

    @PostMapping("/users/me/location")
    @Operation(summary = "Update user location", description = "Stores the latest location for the authenticated user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User location updated successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.LocationApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed for location coordinates", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user does not have user role", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<LocationResponseDto>> updateUserLocation(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody LocationUpdateRequestDto request
    ) {
        log.info("endpoint_request name=updateUserLocation principal={} lat={} lon={} hasTimestamp={}",
                LogSanitizer.summarizePrincipal(authenticatedUser),
                request.latitude(),
                request.longitude(),
                request.timestamp() != null);
        LocationResponseDto response = locationService.updateUserLocation(authenticatedUser, request);
        log.info("endpoint_success name=updateUserLocation actorId={} serverTimestamp={}",
                response.actorId(),
                response.serverTimestamp());
        return ResponseEntity.ok(ApiResponse.success("User location updated successfully", response));
    }
}
