package com.staynest.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Moderation action request with optional reason/notes")
public class ModerationActionRequest {

    @NotBlank(message = "Reason is required")
    @Schema(description = "Reason for the moderation action", example = "Photos do not match description")
    private String reason;

    @Schema(description = "Internal notes for moderators")
    private String notes;
}
