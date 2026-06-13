package com.staynest.api.factory;

import com.staynest.api.dto.request.RegisterRequest;
import com.staynest.api.entity.User;
import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserFactory {

    @Value("${app.auth.email-verification-required:true}")
    private boolean emailVerificationRequired;

    public User createNewUser(RegisterRequest request, String encodedPassword, String verificationToken) {
        // In dev (email-verification-required=false) accounts are auto-activated
        // so users can log in immediately without a mail server running.
        boolean requiresVerification = emailVerificationRequired;

        return User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(encodedPassword)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phone(request.getPhone())
                .role(UserRole.GUEST)
                .status(requiresVerification ? UserStatus.UNVERIFIED : UserStatus.ACTIVE)
                .emailVerified(!requiresVerification)
                .verificationToken(requiresVerification ? verificationToken : null)
                .verificationTokenExpiry(requiresVerification ? Instant.now().plusSeconds(10 * 60) : null) // 10 min OTP
                .failedLoginAttempts(0)
                .build();
    }
}
