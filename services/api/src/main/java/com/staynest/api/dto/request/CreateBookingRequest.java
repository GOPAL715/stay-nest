package com.staynest.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Request to create a new booking")
public class CreateBookingRequest {

    @NotNull(message = "Property ID is required")
    @Schema(description = "Property to book")
    private UUID propertyId;

    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    @Schema(description = "Check-in date", example = "2026-07-01")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Schema(description = "Check-out date", example = "2026-07-05")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "Must have at least 1 guest")
    @Schema(description = "Number of guests", example = "2")
    private Integer numGuests;

    @Schema(description = "Special requests for the host")
    private String specialRequests;
}
