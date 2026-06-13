package com.staynest.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Reset password request using token from email")
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    @Schema(description = "Token received in the reset password email")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "New password", example = "NewSecurePass@456")
    private String newPassword;
}
