package com.staynest.api.dto;

import java.math.BigDecimal;

public record GeoCoordinates(
        BigDecimal latitude,
        BigDecimal longitude,
        String displayName,
        boolean approximate
) {
}
