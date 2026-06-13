package com.staynest.api.dto.request;

import com.staynest.api.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Property search/filter parameters")
public class PropertySearchRequest {

    @Schema(description = "City to search in", example = "Mumbai")
    private String city;

    @Schema(description = "Check-in date", example = "2026-07-01")
    private LocalDate checkIn;

    @Schema(description = "Check-out date", example = "2026-07-05")
    private LocalDate checkOut;

    @Schema(description = "Number of guests", example = "2")
    private Integer numGuests;

    @Schema(description = "Minimum price per night in paise", example = "100000")
    private Long minPrice;

    @Schema(description = "Maximum price per night in paise", example = "500000")
    private Long maxPrice;

    @Schema(description = "Filter by amenity IDs")
    private Set<UUID> amenityIds;

    @Schema(description = "Filter by property type")
    private PropertyType propertyType;

    @Schema(description = "Sort field: newest, price_asc, price_desc, rating", example = "newest")
    @Builder.Default
    private String sortBy = "newest";
}
