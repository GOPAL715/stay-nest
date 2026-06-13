package com.staynest.api.config;

import com.staynest.api.entity.User;
import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import com.staynest.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures a default SUPER_ADMIN account exists on every startup.
 *
 * Why this exists: The Liquibase seed (V012__seed_data.sql) contains a
 * hard-coded BCrypt hash that may not match the intended plain-text password.
 * This runner uses the application's real PasswordEncoder to encode the
 * password correctly, so login always works regardless of the SQL hash.
 *
 * Behaviour:
 *  - If 'admin@staynest.com' does NOT exist → create it fresh.
 *  - If it exists but the password hash is wrong → re-encode and update it.
 *  - If it exists and the hash is correct → do nothing (idempotent).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_EMAIL    = "admin@staynest.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123!";
    private static final String DEFAULT_FIRST_NAME     = "Super";
    private static final String DEFAULT_LAST_NAME      = "Admin";

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        userRepository.findByEmail(DEFAULT_ADMIN_EMAIL).ifPresentOrElse(
            existingAdmin -> {
                // User exists — verify the stored hash still matches the default password.
                // If the hash is corrupted (as it was in V012 seed), fix it in-place.
                if (!passwordEncoder.matches(DEFAULT_ADMIN_PASSWORD, existingAdmin.getPassword())) {
                    log.warn(
                        "Default SUPER_ADMIN password hash is invalid — re-encoding and updating. " +
                        "This usually means the seed SQL contained a wrong hash."
                    );
                    existingAdmin.updatePassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
                    userRepository.save(existingAdmin);
                    log.info("Default SUPER_ADMIN password hash corrected for: {}", DEFAULT_ADMIN_EMAIL);
                } else {
                    log.debug("Default SUPER_ADMIN account OK: {}", DEFAULT_ADMIN_EMAIL);
                }
            },
            () -> {
                // User doesn't exist at all — create from scratch.
                log.info("No SUPER_ADMIN found — creating default admin account: {}", DEFAULT_ADMIN_EMAIL);
                User admin = User.builder()
                        .email(DEFAULT_ADMIN_EMAIL)
                        .passwordHash(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                        .firstName(DEFAULT_FIRST_NAME)
                        .lastName(DEFAULT_LAST_NAME)
                        .role(UserRole.SUPER_ADMIN)
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .failedLoginAttempts(0)
                        .build();
                userRepository.save(admin);
                log.info("Default SUPER_ADMIN created: {} (password: {})",
                         DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
            }
        );
    }
}
