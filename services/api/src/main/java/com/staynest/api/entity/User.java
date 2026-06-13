package com.staynest.api.entity;

import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private UserStatus status;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private Instant verificationTokenExpiry;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private Instant resetTokenExpiry;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "lockout_until")
    private Instant lockoutUntil;

    // --- Mutators (no @Setter on entity — explicit methods only) ---

    public void markEmailVerified() {
        this.emailVerified = true;
        this.verificationToken = null;
        this.verificationTokenExpiry = null;
    }

    public void setVerificationToken(String token, Instant expiry) {
        this.verificationToken = token;
        this.verificationTokenExpiry = expiry;
    }

    public void setResetToken(String token, Instant expiry) {
        this.resetToken = token;
        this.resetTokenExpiry = expiry;
    }

    public void clearResetToken() {
        this.resetToken = null;
        this.resetTokenExpiry = null;
    }

    public void updatePassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
    }

    public void updateStatus(UserStatus newStatus) {
        this.status = newStatus;
    }

    public void updateRole(UserRole newRole) {
        this.role = newRole;
    }

    public void updateProfile(String firstName, String lastName, String phone, String profilePictureUrl) {
        if (firstName != null) this.firstName = firstName;
        if (lastName != null) this.lastName = lastName;
        if (phone != null) this.phone = phone;
        if (profilePictureUrl != null) this.profilePictureUrl = profilePictureUrl;
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void lockUntil(Instant lockoutUntil) {
        this.lockoutUntil = lockoutUntil;
    }

    // --- UserDetails implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockoutUntil == null || lockoutUntil.isBefore(Instant.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
