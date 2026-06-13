package com.staynest.api.service.impl;

import com.staynest.api.dto.request.UpdateProfileRequest;
import com.staynest.api.dto.response.UserResponse;
import com.staynest.api.entity.User;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.mapper.UserMapper;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.updateProfile(
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getProfilePictureUrl()
        );
        userRepository.save(user);
        log.info("Profile updated for user: {}", userId);
        return userMapper.toUserResponse(user);
    }
}
