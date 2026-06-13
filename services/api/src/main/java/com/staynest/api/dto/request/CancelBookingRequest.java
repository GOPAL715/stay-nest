package com.staynest.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Cancellation request with optional reason")
public class CancelBookingRequest {

    @NotBlank(message = "Cancellation reason is required")
    @Schema(description = "Reason for cancellation", example = "Change of plans")
    private String reason;
}
