package com.staynest.api.dto.response;

import com.staynest.api.enums.BookingMode;
import com.staynest.api.enums.PropertyStatus;
import com.staynest.api.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Property summary for search results / listing cards")
public class PropertySummaryResponse {

    private UUID id;
    private String title;
    private PropertyType propertyType;
    private String city;
    private String state;
    private String country;
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

    private BookingMode bookingMode;
    private PropertyStatus status;

    @Schema(description = "Cover photo URL")
    private String coverPhotoUrl;

    @Schema(description = "Host first name")
    private String hostFirstName;

    @Schema(description = "Host last name")
    private String hostLastName;
}
