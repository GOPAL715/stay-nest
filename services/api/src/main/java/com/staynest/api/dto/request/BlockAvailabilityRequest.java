package com.staynest.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "Request to block a date range on a property")
public class BlockAvailabilityRequest {

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    @Schema(description = "First blocked date (inclusive)", example = "2026-07-01")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(description = "Last blocked date (inclusive)", example = "2026-07-05")
    private LocalDate endDate;
}
