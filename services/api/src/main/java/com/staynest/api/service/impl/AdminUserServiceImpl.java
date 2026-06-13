package com.staynest.api.service.impl;

import com.staynest.api.dto.request.CreateAdminRequest;
import com.staynest.api.dto.response.UserResponse;
import com.staynest.api.entity.User;
import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.mapper.UserMapper;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createAdminUser(CreateAdminRequest request, UUID createdByAdminId) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("An account with this email already exists");
        }

        // Only SUPER_ADMIN, PROPERTY_MANAGER, and SUPPORT_AGENT are valid admin roles
        UserRole role = request.getRole();
        if (role == UserRole.GUEST || role == UserRole.HOST) {
            throw new BusinessRuleException("Cannot create a non-admin user through this endpoint");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phone(request.getPhone())
                .role(role)
                .status(UserStatus.ACTIVE)   // admin accounts are pre-activated
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(user);
        log.info("Admin user [{}] created with role [{}] by admin [{}]", user.getEmail(), role, createdByAdminId);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(UUID userId, UserStatus newStatus, UUID adminId) {
        User user = getUserOrThrow(userId);
        if (newStatus == UserStatus.DELETED) {
            throw new BusinessRuleException("Use the delete endpoint to delete a user");
        }
        user.updateStatus(newStatus);
        userRepository.save(user);
        log.info("User [{}] status updated to {} by admin [{}]", userId, newStatus, adminId);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(UUID userId, UserRole newRole, UUID adminId) {
        User user = getUserOrThrow(userId);
        user.updateRole(newRole);
        userRepository.save(user);
        log.info("User [{}] role updated to {} by admin [{}]", userId, newRole, adminId);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void softDeleteUser(UUID userId, UUID adminId) {
        User user = getUserOrThrow(userId);
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessRuleException("Super Admin accounts cannot be deleted");
        }
        user.updateStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("User [{}] soft-deleted by admin [{}]", userId, adminId);
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
