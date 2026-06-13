package com.staynest.api.service;

public interface LoginAttemptService {

    void recordFailure(String email);

    boolean isLocked(String email);

    void reset(String email);
}
