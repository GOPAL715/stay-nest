package com.staynest.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Platform configuration entry")
public class PlatformConfigResponse {

    private UUID id;
    private String configKey;
    private String configValue;
    private String description;
    private Instant updatedAt;
}
