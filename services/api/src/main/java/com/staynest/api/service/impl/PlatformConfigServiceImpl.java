package com.staynest.api.service.impl;

import com.staynest.api.dto.request.UpdatePlatformConfigRequest;
import com.staynest.api.dto.response.PlatformConfigResponse;
import com.staynest.api.entity.PlatformConfig;
import com.staynest.api.entity.User;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.mapper.PlatformConfigMapper;
import com.staynest.api.repository.PlatformConfigRepository;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformConfigServiceImpl implements PlatformConfigService {

    private final PlatformConfigRepository platformConfigRepository;
    private final UserRepository userRepository;
    private final PlatformConfigMapper platformConfigMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PlatformConfigResponse> getAllConfig() {
        return platformConfigMapper.toResponseList(platformConfigRepository.findAll());
    }

    @Override
    @Transactional
    public PlatformConfigResponse updateConfig(String configKey, UpdatePlatformConfigRequest request, UUID adminId) {
        PlatformConfig config = platformConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Config key not found: " + configKey));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        config.updateValue(request.getConfigValue(), admin);
        PlatformConfig saved = platformConfigRepository.save(config);
        log.info("Platform config [{}] updated to [{}] by admin [{}]", configKey, request.getConfigValue(), adminId);
        return platformConfigMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public int getIntValue(String configKey, int defaultValue) {
        return platformConfigRepository.findByConfigKey(configKey)
                .map(PlatformConfig::getConfigValue)
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        log.warn("Invalid integer config value for key [{}]: {}", configKey, value);
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }
}
