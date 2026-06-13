package com.staynest.api.controller;

import com.staynest.api.dto.request.CreatePaymentOrderRequest;
import com.staynest.api.dto.request.VerifyPaymentRequest;
import com.staynest.api.dto.response.PaymentResponse;
import com.staynest.api.dto.response.RefundResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.PaymentService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment integration — demo mode by default (no Razorpay keys required)")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    // -----------------------------------------------------------------------
    // 1. Create Razorpay order
    // -----------------------------------------------------------------------

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('GUEST', 'HOST')")
    @Operation(summary = "Create a Razorpay payment order for a booking")
    public ResponseEntity<ApiResponse<PaymentResponse>> createOrder(
            @Valid @RequestBody CreatePaymentOrderRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        PaymentResponse response = paymentService.createOrder(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment order created", httpRequest.getRequestURI()));
    }

    // -----------------------------------------------------------------------
    // 2. Verify payment signature after Razorpay checkout
    // -----------------------------------------------------------------------

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('GUEST', 'HOST')")
    @Operation(summary = "Verify Razorpay payment signature")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        PaymentResponse response = paymentService.verifyPayment(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Payment verified", httpRequest.getRequestURI()));
    }

    // -----------------------------------------------------------------------
    // 3. Razorpay webhook — PUBLIC, no auth, must always return 200
    // -----------------------------------------------------------------------

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay webhook endpoint (public — no auth required)")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String razorpaySignature) {
        paymentService.handleWebhook(payload, razorpaySignature);
        return ResponseEntity.ok().build();
    }

    // -----------------------------------------------------------------------
    // 4. Initiate refund
    // -----------------------------------------------------------------------

    @PostMapping("/bookings/{bookingId}/refund")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPPORT_AGENT')")
    @Operation(summary = "Issue a refund for a booking (SUPER_ADMIN or SUPPORT_AGENT only)")
    public ResponseEntity<ApiResponse<RefundResponse>> initiateRefund(
            @PathVariable UUID bookingId,
            @RequestParam long amount,
            @RequestParam String reason,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        RefundResponse response = paymentService.initiateRefund(bookingId, amount, reason, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Refund initiated", httpRequest.getRequestURI()));
    }

    // -----------------------------------------------------------------------
    // 5. Get payment for a booking
    // -----------------------------------------------------------------------

    @GetMapping("/bookings/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment details for a booking (returns empty data if unpaid)")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBooking(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        Optional<PaymentResponse> payment = paymentService.getPaymentByBooking(bookingId, currentUser.getId());
        String message = payment.isPresent() ? "Payment retrieved" : "No payment for this booking yet";
        return ResponseEntity.ok(ApiResponse.success(payment.orElse(null), message, httpRequest.getRequestURI()));
    }
}
