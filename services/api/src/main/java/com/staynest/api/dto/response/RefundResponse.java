package com.staynest.api.dto.response;

import com.staynest.api.enums.RefundStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class RefundResponse {
    private UUID id;
    private UUID bookingId;
    private UUID paymentId;
    private String razorpayRefundId;
    private long amount;
    private String reason;
    private RefundStatus status;
    private Instant createdAt;
}
