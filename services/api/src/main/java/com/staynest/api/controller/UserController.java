package com.staynest.api.controller;

import com.staynest.api.dto.request.UpdateProfileRequest;
import com.staynest.api.dto.response.UserResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.UserService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user's own profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        UserResponse profile = userService.getProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved", request.getRequestURI()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user's own profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        UserResponse profile = userService.updateProfile(currentUser.getId(), updateRequest);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile updated", request.getRequestURI()));
    }
}
