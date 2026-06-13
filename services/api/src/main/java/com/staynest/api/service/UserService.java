package com.staynest.api.service;

import com.staynest.api.dto.request.UpdateProfileRequest;
import com.staynest.api.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getProfile(UUID userId);

    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);
}
