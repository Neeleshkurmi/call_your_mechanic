package com.nilesh.cym.location.controller;

import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiConfig;
import com.nilesh.cym.config.OpenApiSchemas;
import com.nilesh.cym.location.dto.BookingLocationSnapshotDto;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @GetMapping("/bookings/{bookingId}/location/latest")
    @Operation(summary = "Get latest booking locations", description = "Returns latest user and mechanic locations for a booking if caller is one of the participants.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking locations fetched successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.BookingApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to access this booking", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Mechanic profile not found", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookingLocationSnapshotDto>> getLatestBookingLocation(
            @PathVariable Long bookingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking location snapshot fetched successfully",
                locationService.getLatestBookingLocation(bookingId, authenticatedUser)
        ));
    }

    @GetMapping(path = "/bookings/{bookingId}/location/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream booking locations", description = "Streams booking location updates for the authenticated booking participant using Server-Sent Events.")
    public SseEmitter streamBookingLocation(
            @PathVariable Long bookingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return locationService.subscribeBookingLocationStream(bookingId, authenticatedUser);
    }
}
