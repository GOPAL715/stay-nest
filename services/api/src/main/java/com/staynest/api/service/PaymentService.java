package com.staynest.api.service;

import com.staynest.api.dto.request.CreatePaymentOrderRequest;
import com.staynest.api.dto.request.VerifyPaymentRequest;
import com.staynest.api.dto.response.PaymentResponse;
import com.staynest.api.dto.response.RefundResponse;

import java.util.Optional;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createOrder(CreatePaymentOrderRequest request, UUID guestId);

    PaymentResponse verifyPayment(VerifyPaymentRequest request, UUID guestId);

    void handleWebhook(String payload, String razorpaySignature);

    RefundResponse initiateRefund(UUID bookingId, long amount, String reason, UUID adminId);

    Optional<PaymentResponse> getPaymentByBooking(UUID bookingId, UUID actorId);
}
