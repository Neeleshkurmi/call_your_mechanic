package com.nilesh.cym.booking.controller;

import com.nilesh.cym.booking.dto.BookingResponseDto;
import com.nilesh.cym.booking.dto.CreateBookingRequestDto;
import com.nilesh.cym.booking.service.BookingService;
import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.token.AuthenticatedUser;
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
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponseDto>> createBooking(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateBookingRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Booking created successfully", bookingService.createBooking(authenticatedUser, request)));
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDto>> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Booking fetched successfully", bookingService.getBooking(bookingId)));
    }

    @GetMapping("/users/me/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getUserBookings(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(ApiResponse.success("User bookings fetched successfully", bookingService.getUserBookings(authenticatedUser)));
    }

    @GetMapping("/mechanics/me/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getMechanicBookings(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Mechanic bookings fetched successfully", bookingService.getMechanicBookings(authenticatedUser)));
    }

    @PatchMapping("/bookings/{bookingId}/accept")
    public ResponseEntity<ApiResponse<BookingResponseDto>> acceptBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Booking accepted successfully", bookingService.acceptBooking(bookingId, authenticatedUser)));
    }

    @PatchMapping("/bookings/{bookingId}/reject")
    public ResponseEntity<ApiResponse<BookingResponseDto>> rejectBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Booking rejected successfully", bookingService.rejectBooking(bookingId, authenticatedUser)));
    }
}
