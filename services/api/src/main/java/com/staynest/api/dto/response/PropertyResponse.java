package com.staynest.api.dto.response;

import com.staynest.api.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Full property detail response")
public class PropertyResponse {

    private UUID id;
    private String title;
    private String description;
    private PropertyType propertyType;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int maxGuests;
    private int bedrooms;
    private BigDecimal bathrooms;
    private int beds;

    @Schema(description = "Base price per night in paise")
    private long basePricePerNight;

    @Schema(description = "Cleaning fee in paise")
    private long cleaningFee;

    private BigDecimal serviceFeePercent;
    private BookingMode bookingMode;
    private CancellationPolicy cancellationPolicy;
    private PropertyStatus status;
    private String rejectionReason;

    @Schema(description = "Host summary information")
    private HostSummary host;

    private List<PropertyPhotoResponse> photos;
    private Set<AmenityResponse> amenities;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Builder
    @Schema(description = "Host brief info embedded in property response")
    public static class HostSummary {
        private UUID id;
        private String firstName;
        private String lastName;
        private String profilePictureUrl;
        private Instant createdAt;
    }
}
