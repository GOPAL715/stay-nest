package com.staynest.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "Geocoded coordinates for an address")
public class GeocodeResponse {

    @Schema(description = "Latitude", example = "15.2993000")
    private BigDecimal latitude;

    @Schema(description = "Longitude", example = "74.1240000")
    private BigDecimal longitude;

    @Schema(description = "Human-readable matched address from the geocoder")
    private String displayName;

    @Schema(description = "True when only city/region could be matched; drag the map pin for exact location")
    private boolean approximate;
}
