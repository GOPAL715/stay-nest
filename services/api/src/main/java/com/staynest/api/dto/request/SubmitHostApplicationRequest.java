package com.staynest.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Host application submission")
public class SubmitHostApplicationRequest {

    @NotBlank(message = "Motivation is required")
    @Schema(description = "Why you want to become a host", example = "I have a spare room and want to share it with travellers.")
    private String motivation;
}
