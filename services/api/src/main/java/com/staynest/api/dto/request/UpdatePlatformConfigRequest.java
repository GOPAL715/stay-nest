package com.staynest.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Update platform configuration value")
public class UpdatePlatformConfigRequest {

    @NotBlank(message = "Config value is required")
    @Size(max = 500)
    @Schema(description = "New configuration value", example = "12")
    private String configValue;
}
