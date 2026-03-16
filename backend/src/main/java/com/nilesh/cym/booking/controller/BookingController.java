package com.nilesh.cym.booking.controller;

import com.nilesh.cym.booking.dto.BookingResponseDto;
import com.nilesh.cym.booking.dto.CreateBookingRequestDto;
import com.nilesh.cym.booking.service.BookingService;
import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiConfig;
import com.nilesh.cym.config.OpenApiSchemas;
import com.nilesh.cym.token.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Bookings", description = "Protected booking management endpoints for users and mechanics.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    @Operation(summary = "Create booking", description = "Creates a new booking for the authenticated user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking created successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.BookingApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or booking request is invalid", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to create this booking", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookingResponseDto>> createBooking(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateBookingRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Booking created successfully", bookingService.createBooking(authenticatedUser, request)));
    }

    @GetMapping("/bookings/{bookingId}")
    @Operation(summary = "Get booking by id", description = "Returns a single booking by its identifier.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking fetched successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.BookingApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to view this booking", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookingResponseDto>> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Booking fetched successfully", bookingService.getBooking(bookingId)));
    }

    @GetMapping("/users/me/bookings")
    @Operation(summary = "List current user's bookings", description = "Returns all bookings created by the authenticated user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User bookings fetched successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.BookingListApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to view these bookings", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getUserBookings(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("User bookings fetched successfully", bookingService.getUserBookings(authenticatedUser)));
    }

    @GetMapping("/mechanics/me/bookings")
    @Operation(summary = "List current mechanic's bookings", description = "Returns bookings assigned to the authenticated mechanic.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mechanic bookings fetched successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.BookingListApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to view mechanic bookings", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getMechanicBookings(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Mechanic bookings fetched successfully", bookingService.getMechanicBookings(authenticatedUser)));
    }

    @PatchMapping("/bookings/{bookingId}/accept")
    @Operation(summary = "Accept booking", description = "Marks a booking as accepted by the authenticated mechanic.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking accepted successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.BookingApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to accept this booking", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookingResponseDto>> acceptBooking(
            @PathVariable Long bookingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Booking accepted successfully", bookingService.acceptBooking(bookingId, authenticatedUser)));
    }

    @PatchMapping("/bookings/{bookingId}/reject")
    @Operation(summary = "Reject booking", description = "Marks a booking as rejected by the authenticated mechanic.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking rejected successfully", content = @Content(schema = @Schema(implementation = OpenApiSchemas.BookingApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to reject this booking", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = OpenApiSchemas.ErrorApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookingResponseDto>> rejectBooking(
            @PathVariable Long bookingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Booking rejected successfully", bookingService.rejectBooking(bookingId, authenticatedUser)));
    }
}
