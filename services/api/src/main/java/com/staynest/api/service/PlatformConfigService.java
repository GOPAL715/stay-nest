package com.staynest.api.service;

import com.staynest.api.dto.request.UpdatePlatformConfigRequest;
import com.staynest.api.dto.response.PlatformConfigResponse;

import java.util.List;
import java.util.UUID;

public interface PlatformConfigService {

    List<PlatformConfigResponse> getAllConfig();

    PlatformConfigResponse updateConfig(String configKey, UpdatePlatformConfigRequest request, UUID adminId);

    int getIntValue(String configKey, int defaultValue);
}
