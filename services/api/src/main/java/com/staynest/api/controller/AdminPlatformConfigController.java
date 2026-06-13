package com.staynest.api.controller;

import com.staynest.api.dto.request.UpdatePlatformConfigRequest;
import com.staynest.api.dto.response.PlatformConfigResponse;
import com.staynest.api.entity.User;
import com.staynest.api.service.PlatformConfigService;
import com.staynest.api.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
@Tag(name = "Admin - Platform Config", description = "Super Admin platform configuration")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminPlatformConfigController {

    private final PlatformConfigService platformConfigService;

    @GetMapping
    @Operation(summary = "List all platform configuration entries")
    public ResponseEntity<ApiResponse<List<PlatformConfigResponse>>> getAllConfig(HttpServletRequest request) {
        List<PlatformConfigResponse> configs = platformConfigService.getAllConfig();
        return ResponseEntity.ok(ApiResponse.success(configs, "Platform config retrieved", request.getRequestURI()));
    }

    @PutMapping("/{configKey}")
    @Operation(summary = "Update a platform configuration value")
    public ResponseEntity<ApiResponse<PlatformConfigResponse>> updateConfig(
            @PathVariable String configKey,
            @Valid @RequestBody UpdatePlatformConfigRequest updateRequest,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        PlatformConfigResponse config = platformConfigService.updateConfig(configKey, updateRequest, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(config, "Config updated", request.getRequestURI()));
    }
}
