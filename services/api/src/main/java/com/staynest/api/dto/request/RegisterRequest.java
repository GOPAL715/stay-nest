package com.staynest.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "User registration request")
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Password (min 8 characters)", example = "SecurePass@123")
    private String password;

    @Size(max = 20)
    @Schema(description = "Phone number", example = "+919876543210")
    private String phone;
}
