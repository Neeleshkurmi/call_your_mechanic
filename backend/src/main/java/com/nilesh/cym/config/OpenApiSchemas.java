package com.nilesh.cym.config;

import com.nilesh.cym.auth.dto.AuthDtos;
import com.nilesh.cym.auth.dto.AuthTokenResponseDto;
import com.nilesh.cym.auth.dto.ProfileUpdateResponseDto;
import com.nilesh.cym.booking.dto.BookingResponseDto;
import com.nilesh.cym.catalog.dto.ServiceResponseDto;
import com.nilesh.cym.location.dto.LocationResponseDto;
import com.nilesh.cym.mechanic.dto.MechanicProfileResponseDto;
import com.nilesh.cym.mechanic.dto.NearbyMechanicResponseDto;
import com.nilesh.cym.review.dto.MechanicReviewsResponseDto;
import com.nilesh.cym.review.dto.ReviewResponseDto;
import com.nilesh.cym.userlocation.dto.SavedLocationResponseDto;
import com.nilesh.cym.vehicle.dto.VehicleResponseDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public final class OpenApiSchemas {

    private OpenApiSchemas() {
    }

    @Schema(name = "VoidApiResponse", description = "Standard success response without a data payload.")
    public record VoidApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "OTP sent successfully") String message,
            @Schema(nullable = true) Object data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "AuthTokenApiResponse", description = "Standard success response carrying OTP authentication tokens and user details.")
    public record AuthTokenApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "OTP verified successfully") String message,
            AuthTokenResponseDto data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "RefreshTokenApiResponse", description = "Standard success response carrying refreshed access and refresh tokens.")
    public record RefreshTokenApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Token refreshed successfully") String message,
            AuthDtos.TokenResponse data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "ProfileUpdateApiResponse", description = "Standard success response carrying updated user details and fresh tokens.")
    public record ProfileUpdateApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "User profile updated successfully") String message,
            ProfileUpdateResponseDto data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "BookingApiResponse", description = "Standard success response carrying a single booking.")
    public record BookingApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Booking fetched successfully") String message,
            BookingResponseDto data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "BookingListApiResponse", description = "Standard success response carrying a list of bookings.")
    public record BookingListApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "User bookings fetched successfully") String message,
            List<BookingResponseDto> data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }


    @Schema(name = "LocationApiResponse", description = "Standard success response carrying actor location details.")
    public record LocationApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Location updated successfully") String message,
            LocationResponseDto data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "ServiceApiResponse", description = "Standard success response carrying a single service catalog item.")
    public record ServiceApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Service fetched successfully") String message,
            ServiceResponseDto data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "ServiceListApiResponse", description = "Standard success response carrying a list of service catalog items.")
    public record ServiceListApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Services fetched successfully") String message,
            List<ServiceResponseDto> data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "VehicleApiResponse", description = "Standard success response carrying a single vehicle.")
    public record VehicleApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Vehicle added successfully") String message,
            VehicleResponseDto data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "VehicleListApiResponse", description = "Standard success response carrying a list of vehicles.")
    public record VehicleListApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Vehicles fetched successfully") String message,
            List<VehicleResponseDto> data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "SavedLocationApiResponse", description = "Standard success response carrying a single saved location.")
    public record SavedLocationApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Saved location added successfully") String message,
            SavedLocationResponseDto data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "SavedLocationListApiResponse", description = "Standard success response carrying saved locations.")
    public record SavedLocationListApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Saved locations fetched successfully") String message,
            List<SavedLocationResponseDto> data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "MechanicProfileApiResponse", description = "Standard success response carrying mechanic profile details.")
    public record MechanicProfileApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Mechanic profile fetched successfully") String message,
            MechanicProfileResponseDto data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "NearbyMechanicListApiResponse", description = "Standard success response carrying nearby mechanics.")
    public record NearbyMechanicListApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Nearby mechanics fetched successfully") String message,
            List<NearbyMechanicResponseDto> data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "ReviewApiResponse", description = "Standard success response carrying a submitted review.")
    public record ReviewApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Review submitted successfully") String message,
            ReviewResponseDto data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "MechanicReviewsApiResponse", description = "Standard success response carrying mechanic review summary.")
    public record MechanicReviewsApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "true") boolean success,
            @Schema(example = "Mechanic reviews fetched successfully") String message,
            MechanicReviewsResponseDto data,
            @ArraySchema(schema = @Schema(example = "[]")) List<String> errors
    ) {
    }

    @Schema(name = "ErrorApiResponse", description = "Standard error response envelope returned by validation, auth, and server errors.")
    public record ErrorApiResponse(
            @Schema(example = "2026-03-15T12:34:56Z") Instant timestamp,
            @Schema(example = "false") boolean success,
            @Schema(example = "Validation failed") String message,
            @Schema(nullable = true) Object data,
            @ArraySchema(arraySchema = @Schema(description = "Error details."), schema = @Schema(example = "mobile: Mobile must be in E.164 or digits format"))
            List<String> errors
    ) {
    }
}
