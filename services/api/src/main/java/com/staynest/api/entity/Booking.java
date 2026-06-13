package com.staynest.api.entity;

import com.staynest.api.enums.BookingStatus;
import com.staynest.api.enums.CancellationPolicy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "num_guests", nullable = false)
    private int numGuests;

    @Column(name = "num_nights", nullable = false)
    private int numNights;

    // All monetary values in paise
    @Column(name = "nightly_rate", nullable = false)
    private long nightlyRate;

    @Column(name = "cleaning_fee", nullable = false)
    private long cleaningFee;

    @Column(name = "platform_fee", nullable = false)
    private long platformFee;

    @Column(name = "taxes", nullable = false)
    private long taxes;

    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", nullable = false, length = 50)
    private CancellationPolicy cancellationPolicy;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    // --- Mutators ---

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel(User cancelledBy, String reason) {
        this.status = BookingStatus.CANCELLED;
        this.cancelledBy = cancelledBy;
        this.cancellationReason = reason;
        this.cancelledAt = Instant.now();
    }

    public void complete() {
        this.status = BookingStatus.COMPLETED;
    }
}
