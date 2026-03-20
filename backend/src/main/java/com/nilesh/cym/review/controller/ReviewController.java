package com.nilesh.cym.review.controller;

import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiConfig;
import com.nilesh.cym.review.dto.MechanicReviewsResponseDto;
import com.nilesh.cym.review.dto.ReviewRequestDto;
import com.nilesh.cym.review.dto.ReviewResponseDto;
import com.nilesh.cym.review.service.ReviewService;
import com.nilesh.cym.token.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reviews", description = "Protected review endpoints for mechanics and completed bookings.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/bookings/{id}/review")
    @Operation(summary = "Create booking review")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(
            @PathVariable Long id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ReviewRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review submitted successfully",
                reviewService.createReview(id, authenticatedUser, request)
        ));
    }

    @GetMapping("/mechanics/{id}/reviews")
    @Operation(summary = "Get mechanic reviews")
    public ResponseEntity<ApiResponse<MechanicReviewsResponseDto>> getMechanicReviews(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Mechanic reviews fetched successfully",
                reviewService.getMechanicReviews(id)
        ));
    }
}
