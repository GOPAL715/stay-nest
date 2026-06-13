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
@Schema(description = "Request to create a new property listing")
public class CreatePropertyRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Schema(description = "Property title", example = "Cozy Studio in Bandra")
    private String title;

    @NotBlank(message = "Description is required")
    @Schema(description = "Detailed property description")
    private String description;

    @NotNull(message = "Property type is required")
    @Schema(description = "Type of property")
    private PropertyType propertyType;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255)
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    private String postalCode;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @NotNull(message = "Max guests is required")
    @Min(value = 1, message = "Must accommodate at least 1 guest")
    private Integer maxGuests;

    @NotNull(message = "Bedrooms is required")
    @Min(value = 0)
    private Integer bedrooms;

    @NotNull(message = "Bathrooms is required")
    @DecimalMin(value = "0.5")
    private BigDecimal bathrooms;

    @NotNull(message = "Beds is required")
    @Min(value = 1)
    private Integer beds;

    @NotNull(message = "Base price per night is required")
    @Min(value = 100, message = "Base price must be at least ₹1 (100 paise)")
    @Schema(description = "Price per night in paise (₹1 = 100 paise)", example = "250000")
    private Long basePricePerNight;

    @Min(value = 0)
    @Schema(description = "Cleaning fee in paise", example = "50000")
    private Long cleaningFee;

    @NotNull(message = "Booking mode is required")
    private BookingMode bookingMode;

    @NotNull(message = "Cancellation policy is required")
    private CancellationPolicy cancellationPolicy;
}
