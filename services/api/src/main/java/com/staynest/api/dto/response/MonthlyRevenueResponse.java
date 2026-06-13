package com.staynest.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Monthly revenue data point")
public class MonthlyRevenueResponse {

    private int year;
    private int month;
    private String monthLabel;
    private long platformFeePaise;
    private String platformFeeInr;
    private long bookingCount;
}
