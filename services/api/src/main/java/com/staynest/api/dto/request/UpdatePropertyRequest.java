package com.staynest.api.dto.request;

import com.staynest.api.enums.BookingMode;
import com.staynest.api.enums.CancellationPolicy;
import com.staynest.api.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "Request to update a property listing — all fields optional")
public class UpdatePropertyRequest {

    @Size(max = 255)
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

    @Min(1)
    private Integer maxGuests;

    @Min(0)
    private Integer bedrooms;

    @DecimalMin("0.5")
    private BigDecimal bathrooms;

    @Min(1)
    private Integer beds;

    @Min(100)
    @Schema(description = "Price per night in paise")
    private Long basePricePerNight;

    @Min(0)
    private Long cleaningFee;

    private BookingMode bookingMode;
    private CancellationPolicy cancellationPolicy;
}
