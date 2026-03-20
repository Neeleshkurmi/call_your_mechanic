package com.nilesh.cym.booking.controller;

import com.nilesh.cym.booking.dto.BookingResponseDto;
import com.nilesh.cym.booking.dto.BookingStatusUpdateRequestDto;
import com.nilesh.cym.booking.dto.CreateBookingRequestDto;
import com.nilesh.cym.entity.enums.BookingStatus;
import com.nilesh.cym.booking.service.BookingService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
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
        log.info("endpoint_request name=createBooking principal={} mechanicId={} vehicleId={} serviceId={}",
                LogSanitizer.summarizePrincipal(authenticatedUser),
                request.mechanicId(),
                request.vehicleId(),
                request.serviceId());
        BookingResponseDto response = bookingService.createBooking(authenticatedUser, request);
        log.info("endpoint_success name=createBooking bookingId={} status={}", response.bookingId(), response.status());
        return ResponseEntity.ok(ApiResponse.success("Booking created successfully", response));
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
    public ResponseEntity<ApiResponse<BookingResponseDto>> getBooking(
            @PathVariable Long bookingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("endpoint_request name=getBooking bookingId={}", bookingId);
        BookingResponseDto response = bookingService.getBooking(bookingId, authenticatedUser);
        log.info("endpoint_success name=getBooking bookingId={} status={}", response.bookingId(), response.status());
        return ResponseEntity.ok(ApiResponse.success("Booking fetched successfully", response));
    }

    @GetMapping("/bookings")
    @Operation(summary = "List bookings for authenticated actor", description = "Returns bookings for the authenticated user or mechanic and optionally filters by status.")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getBookings(
            @RequestParam(required = false) BookingStatus status,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        List<BookingResponseDto> responses = bookingService.getBookings(authenticatedUser, status);
        return ResponseEntity.ok(ApiResponse.success("Bookings fetched successfully", responses));
    }

    @GetMapping("/bookings/active")
    @Operation(summary = "Get active bookings", description = "Returns active bookings for the authenticated user or mechanic.")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getActiveBookings(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Active bookings fetched successfully", bookingService.getActiveBookings(authenticatedUser)));
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
        log.info("endpoint_request name=getUserBookings principal={}", LogSanitizer.summarizePrincipal(authenticatedUser));
        List<BookingResponseDto> responses = bookingService.getUserBookings(authenticatedUser);
        log.info("endpoint_success name=getUserBookings bookingCount={}", responses.size());
        return ResponseEntity.ok(ApiResponse.success("User bookings fetched successfully", responses));
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
        log.info("endpoint_request name=getMechanicBookings principal={}", LogSanitizer.summarizePrincipal(authenticatedUser));
        List<BookingResponseDto> responses = bookingService.getMechanicBookings(authenticatedUser);
        log.info("endpoint_success name=getMechanicBookings bookingCount={}", responses.size());
        return ResponseEntity.ok(ApiResponse.success("Mechanic bookings fetched successfully", responses));
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
        log.info("endpoint_request name=acceptBooking bookingId={} principal={}",
                bookingId,
                LogSanitizer.summarizePrincipal(authenticatedUser));
        BookingResponseDto response = bookingService.acceptBooking(bookingId, authenticatedUser);
        log.info("endpoint_success name=acceptBooking bookingId={} status={}", response.bookingId(), response.status());
        return ResponseEntity.ok(ApiResponse.success("Booking accepted successfully", response));
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
        log.info("endpoint_request name=rejectBooking bookingId={} principal={}",
                bookingId,
                LogSanitizer.summarizePrincipal(authenticatedUser));
        BookingResponseDto response = bookingService.rejectBooking(bookingId, authenticatedUser);
        log.info("endpoint_success name=rejectBooking bookingId={} status={}", response.bookingId(), response.status());
        return ResponseEntity.ok(ApiResponse.success("Booking rejected successfully", response));
    }

    @PatchMapping("/bookings/{bookingId}/status")
    @Operation(summary = "Update booking status", description = "Updates a booking status according to the mechanic lifecycle rules.")
    public ResponseEntity<ApiResponse<BookingResponseDto>> updateBookingStatus(
            @PathVariable Long bookingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody BookingStatusUpdateRequestDto request
    ) {
        BookingResponseDto response = bookingService.updateBookingStatus(bookingId, authenticatedUser, request);
        return ResponseEntity.ok(ApiResponse.success("Booking status updated successfully", response));
    }

    @PatchMapping("/bookings/{bookingId}/cancel")
    @Operation(summary = "Cancel booking", description = "Allows the booking user to cancel a pre-completion booking.")
    public ResponseEntity<ApiResponse<BookingResponseDto>> cancelBooking(
            @PathVariable Long bookingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        BookingResponseDto response = bookingService.cancelBooking(bookingId, authenticatedUser);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }

    @PatchMapping("/bookings/{bookingId}/complete")
    @Operation(summary = "Complete booking", description = "Allows the assigned mechanic to mark the job as completed.")
    public ResponseEntity<ApiResponse<BookingResponseDto>> completeBooking(
            @PathVariable Long bookingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        BookingResponseDto response = bookingService.completeBooking(bookingId, authenticatedUser);
        return ResponseEntity.ok(ApiResponse.success("Booking completed successfully", response));
    }

    @PostMapping("/bookings/{bookingId}/rebook")
    @Operation(summary = "Rebook booking", description = "Creates a new booking by copying a user's previous booking details.")
    public ResponseEntity<ApiResponse<BookingResponseDto>> rebook(
            @PathVariable Long bookingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        BookingResponseDto response = bookingService.rebook(bookingId, authenticatedUser);
        return ResponseEntity.ok(ApiResponse.success("Booking re-created successfully", response));
    }
}
