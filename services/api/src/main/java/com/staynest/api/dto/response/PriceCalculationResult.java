package com.staynest.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Calculated booking price breakdown")
public class PriceCalculationResult {

    private int numNights;
    private long nightlyRate;
    private long subtotal;
    private long cleaningFee;
    private long platformFee;
    private long taxes;
    private long totalAmount;
    private int platformFeePercent;
    private int taxRatePercent;
}
