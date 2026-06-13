package com.staynest.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendMessageRequest {

    @NotBlank(message = "Message content is required")
    @Size(max = 5000, message = "Message must not exceed 5000 characters")
    private String content;
}
