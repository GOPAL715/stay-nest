package com.staynest.api.controller;

import com.staynest.api.dto.request.CreateAdminRequest;
import com.staynest.api.dto.request.UpdateUserRoleRequest;
import com.staynest.api.dto.request.UpdateUserStatusRequest;
import com.staynest.api.dto.response.UserResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.AdminUserService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "Admin user management operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new admin user (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> createAdminUser(
            @Valid @RequestBody CreateAdminRequest createRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        UserResponse user = adminUserService.createAdminUser(createRequest, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "Admin user created successfully", request.getRequestURI()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = adminUserService.listUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved", request.getRequestURI()));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID userId,
            HttpServletRequest request) {
        UserResponse user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved", request.getRequestURI()));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PROPERTY_MANAGER')")
    @Operation(summary = "Update user status (activate/deactivate)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest statusRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        UserResponse user = adminUserService.updateUserStatus(userId, statusRequest.getStatus(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(user, "User status updated", request.getRequestURI()));
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Change user role (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest roleRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        UserResponse user = adminUserService.updateUserRole(userId, roleRequest.getRole(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(user, "User role updated", request.getRequestURI()));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Soft-delete a user (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        adminUserService.softDeleteUser(userId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User deleted", request.getRequestURI()));
    }
}
