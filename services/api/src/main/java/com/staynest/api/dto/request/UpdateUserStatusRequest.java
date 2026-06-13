package com.staynest.api.dto.request;

import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New user status")
    private UserStatus status;
}
