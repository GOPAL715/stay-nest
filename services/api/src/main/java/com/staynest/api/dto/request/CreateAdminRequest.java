package com.staynest.api.dto.request;

import com.staynest.api.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Request to create a new admin user (SUPER_ADMIN only)")
public class CreateAdminRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Schema(example = "Jane")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Schema(example = "Smith")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(example = "jane.smith@staynest.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(example = "SecurePass@123")
    private String password;

    @Size(max = 20)
    @Schema(example = "+919876543210")
    private String phone;

    @NotNull(message = "Role is required")
    @Schema(description = "Admin role to assign", allowableValues = {"SUPER_ADMIN", "PROPERTY_MANAGER", "SUPPORT_AGENT"})
    private UserRole role;
}
