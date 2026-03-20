package com.nilesh.cym.payment.controller;

import com.nilesh.cym.common.dto.ApiResponse;
import com.nilesh.cym.config.OpenApiConfig;
import com.nilesh.cym.payment.dto.PaymentCreateRequestDto;
import com.nilesh.cym.payment.dto.PaymentResponseDto;
import com.nilesh.cym.payment.dto.PaymentVerifyRequestDto;
import com.nilesh.cym.payment.service.PaymentService;
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
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Protected placeholder payment endpoints.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create payment")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> createPayment(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody PaymentCreateRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Payment created successfully", paymentService.createPayment(authenticatedUser, request)));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify payment")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> verifyPayment(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody PaymentVerifyRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", paymentService.verifyPayment(authenticatedUser, request)));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get payment by booking")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPayment(
            @PathVariable Long bookingId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Payment fetched successfully", paymentService.getPaymentForBooking(bookingId, authenticatedUser)));
    }
}
