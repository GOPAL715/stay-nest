package com.staynest.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "User login request")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "Registered email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "SecurePass@123")
    private String password;
}
