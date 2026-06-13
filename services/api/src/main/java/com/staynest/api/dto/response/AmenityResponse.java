package com.staynest.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "Amenity details")
public class AmenityResponse {

    @Schema(description = "Amenity unique identifier")
    private UUID id;

    @Schema(description = "Amenity name", example = "WiFi")
    private String name;

    @Schema(description = "Icon identifier", example = "wifi")
    private String icon;

    @Schema(description = "Category", example = "Connectivity")
    private String category;
}
