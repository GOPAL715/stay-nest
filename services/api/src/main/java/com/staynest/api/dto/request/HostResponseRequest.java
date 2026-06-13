package com.staynest.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HostResponseRequest {

    @NotBlank(message = "Response text is required")
    @Size(max = 2000, message = "Response must not exceed 2000 characters")
    private String response;
}
