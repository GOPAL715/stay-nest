package com.staynest.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "Property photo")
public class PropertyPhotoResponse {

    @Schema(description = "Photo unique identifier")
    private UUID id;

    @Schema(description = "Photo URL")
    private String url;

    @Schema(description = "Caption")
    private String caption;

    @Schema(description = "Display order (0 = first)")
    private int displayOrder;

    @Schema(description = "Whether this is the cover photo")
    private boolean isCover;
}
