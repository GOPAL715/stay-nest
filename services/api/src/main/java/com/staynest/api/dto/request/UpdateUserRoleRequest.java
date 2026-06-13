package com.staynest.api.dto.request;

import com.staynest.api.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(description = "New user role")
    private UserRole role;
}
