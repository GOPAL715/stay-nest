package com.staynest.api.entity;

import com.staynest.api.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Refund extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "razorpay_refund_id", length = 100)
    private String razorpayRefundId;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private RefundStatus status;

    // --- Mutators ---

    public void markProcessed(String razorpayRefundId) {
        this.razorpayRefundId = razorpayRefundId;
        this.status = RefundStatus.PROCESSED;
    }

    public void markFailed() {
        this.status = RefundStatus.FAILED;
    }
}
