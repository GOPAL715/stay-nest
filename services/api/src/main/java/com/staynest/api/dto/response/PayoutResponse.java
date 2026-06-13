package com.staynest.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Host payout summary")
public class PayoutResponse {

    private UUID bookingId;
    private UUID hostId;
    private String hostName;
    private String propertyTitle;
    private long hostPayoutPaise;
    private String hostPayoutInr;
    private Instant checkOutDate;
    private String status;
}
