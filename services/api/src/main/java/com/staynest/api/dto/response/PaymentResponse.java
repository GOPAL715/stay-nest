package com.staynest.api.dto.response;

import com.staynest.api.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID bookingId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private long amount;
    private String currency;
    private PaymentStatus status;
    private Instant createdAt;
}
