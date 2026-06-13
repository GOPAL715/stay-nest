package com.staynest.api.service.impl;

import com.staynest.api.service.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LoginAttemptServiceImpl implements LoginAttemptService {

    @Value("${app.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.login.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    // In-memory cache: email -> {failureCount, lockedUntil}
    private final ConcurrentHashMap<String, AttemptRecord> attemptCache = new ConcurrentHashMap<>();

    @Override
    public void recordFailure(String email) {
        attemptCache.compute(email.toLowerCase(), (key, record) -> {
            if (record == null) {
                record = new AttemptRecord(0, null);
            }
            // Clear lockout if expired
            if (record.lockedUntil() != null && record.lockedUntil().isBefore(Instant.now())) {
                record = new AttemptRecord(0, null);
            }
            int newCount = record.failureCount() + 1;
            Instant lockedUntil = record.lockedUntil();
            if (newCount >= maxAttempts) {
                lockedUntil = Instant.now().plusSeconds((long) lockoutDurationMinutes * 60);
                log.warn("Account locked for email: {} until {}", key, lockedUntil);
            }
            return new AttemptRecord(newCount, lockedUntil);
        });
    }

    @Override
    public boolean isLocked(String email) {
        AttemptRecord record = attemptCache.get(email.toLowerCase());
        if (record == null) return false;
        if (record.lockedUntil() == null) return false;
        if (record.lockedUntil().isBefore(Instant.now())) {
            attemptCache.remove(email.toLowerCase());
            return false;
        }
        return true;
    }

    @Override
    public void reset(String email) {
        attemptCache.remove(email.toLowerCase());
    }

    private record AttemptRecord(int failureCount, Instant lockedUntil) {}
}
