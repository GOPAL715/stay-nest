package com.staynest.api.dto.response;

import com.staynest.api.enums.AvailabilityBlockReason;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Property availability block")
public class PropertyAvailabilityResponse {

    @Schema(description = "Block unique identifier")
    private UUID id;

    @Schema(description = "Start date of blocked period")
    private LocalDate startDate;

    @Schema(description = "End date of blocked period")
    private LocalDate endDate;

    @Schema(description = "Reason for block")
    private AvailabilityBlockReason reason;
}
