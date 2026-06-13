package com.staynest.api.dto.response;

import com.staynest.api.enums.BookingStatus;
import com.staynest.api.enums.CancellationPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Booking detail response")
public class BookingResponse {

    private UUID id;
    private UUID propertyId;
    private String propertyTitle;
    private String propertyCity;
    private String coverPhotoUrl;
    private UUID hostId;
    private String hostFirstName;
    private String hostLastName;
    private UUID guestId;
    private String guestFirstName;
    private String guestLastName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numGuests;
    private int numNights;
    private BookingStatus status;
    private CancellationPolicy cancellationPolicy;
    private String cancellationReason;
    private String specialRequests;
    private Instant cancelledAt;
    private Instant createdAt;

    @Schema(description = "Itemised price breakdown")
    private PriceBreakdownResponse priceBreakdown;

    @Getter
    @Builder
    @Schema(description = "Itemised price breakdown in paise and INR")
    public static class PriceBreakdownResponse {

        @Schema(description = "Nightly rate in paise")
        private long nightlyRate;

        @Schema(description = "Number of nights")
        private int numNights;

        @Schema(description = "Nightly rate × nights in paise")
        private long subtotal;

        @Schema(description = "Cleaning fee in paise")
        private long cleaningFee;

        @Schema(description = "Platform service fee in paise")
        private long platformFee;

        @Schema(description = "Taxes (GST) in paise")
        private long taxes;

        @Schema(description = "Grand total in paise")
        private long totalAmount;

        // INR formatted display values
        @Schema(description = "Nightly rate in INR (₹)", example = "2500.00")
        private String nightlyRateInr;

        @Schema(description = "Total amount in INR (₹)", example = "14500.00")
        private String totalAmountInr;
    }
}
