package com.staynest.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Request to add a new amenity to the master list")
public class CreateAmenityRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    @Schema(description = "Amenity name", example = "Rooftop Terrace")
    private String name;

    @Size(max = 100)
    @Schema(description = "Icon identifier", example = "rooftop")
    private String icon;

    @Size(max = 100)
    @Schema(description = "Category", example = "Recreation")
    private String category;
}
