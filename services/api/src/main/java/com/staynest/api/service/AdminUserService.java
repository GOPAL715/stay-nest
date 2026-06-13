package com.staynest.api.service;

import com.staynest.api.dto.request.CreateAdminRequest;
import com.staynest.api.dto.response.UserResponse;
import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {

    UserResponse createAdminUser(CreateAdminRequest request, UUID createdByAdminId);

    Page<UserResponse> listUsers(Pageable pageable);

    UserResponse getUserById(UUID userId);

    UserResponse updateUserStatus(UUID userId, UserStatus newStatus, UUID adminId);

    UserResponse updateUserRole(UUID userId, UserRole newRole, UUID adminId);

    void softDeleteUser(UUID userId, UUID adminId);
}
