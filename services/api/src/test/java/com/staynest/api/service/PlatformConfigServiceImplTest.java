package com.staynest.api.service;

import com.staynest.api.dto.request.UpdatePlatformConfigRequest;
import com.staynest.api.dto.response.PlatformConfigResponse;
import com.staynest.api.entity.PlatformConfig;
import com.staynest.api.entity.User;
import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.mapper.PlatformConfigMapper;
import com.staynest.api.repository.PlatformConfigRepository;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.impl.PlatformConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlatformConfigServiceImplTest {

    @Mock private PlatformConfigRepository platformConfigRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlatformConfigMapper platformConfigMapper;

    @InjectMocks
    private PlatformConfigServiceImpl platformConfigService;

    @Test
    void getIntValue_returnsParsedInteger() {
        PlatformConfig config = PlatformConfig.builder()
                .configKey("service_fee_percent")
                .configValue("12")
                .build();
        given(platformConfigRepository.findByConfigKey("service_fee_percent"))
                .willReturn(Optional.of(config));

        assertThat(platformConfigService.getIntValue("service_fee_percent", 10)).isEqualTo(12);
    }

    @Test
    void getIntValue_missingKey_returnsDefault() {
        given(platformConfigRepository.findByConfigKey("unknown")).willReturn(Optional.empty());
        assertThat(platformConfigService.getIntValue("unknown", 10)).isEqualTo(10);
    }

    @Test
    void getIntValue_invalidValue_returnsDefault() {
        PlatformConfig config = PlatformConfig.builder()
                .configKey("bad")
                .configValue("not-a-number")
                .build();
        given(platformConfigRepository.findByConfigKey("bad")).willReturn(Optional.of(config));

        assertThat(platformConfigService.getIntValue("bad", 10)).isEqualTo(10);
    }

    @Test
    void updateConfig_updatesValue() {
        UUID adminId = UUID.randomUUID();
        PlatformConfig config = PlatformConfig.builder()
                .configKey("service_fee_percent")
                .configValue("10")
                .build();
        User admin = User.builder()
                .email("admin@staynest.com")
                .passwordHash("h")
                .firstName("A")
                .lastName("B")
                .role(UserRole.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
        UpdatePlatformConfigRequest request = UpdatePlatformConfigRequest.builder()
                .configValue("12")
                .build();
        PlatformConfigResponse response = PlatformConfigResponse.builder()
                .configKey("service_fee_percent")
                .configValue("12")
                .build();

        given(platformConfigRepository.findByConfigKey("service_fee_percent")).willReturn(Optional.of(config));
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(platformConfigRepository.save(config)).willReturn(config);
        given(platformConfigMapper.toResponse(config)).willReturn(response);

        PlatformConfigResponse result = platformConfigService.updateConfig("service_fee_percent", request, adminId);

        assertThat(config.getConfigValue()).isEqualTo("12");
        assertThat(result.getConfigValue()).isEqualTo("12");
    }

    @Test
    void getAllConfig_returnsMappedList() {
        PlatformConfig config = PlatformConfig.builder().configKey("k").configValue("v").build();
        given(platformConfigRepository.findAll()).willReturn(List.of(config));
        given(platformConfigMapper.toResponseList(List.of(config))).willReturn(
                List.of(PlatformConfigResponse.builder().configKey("k").configValue("v").build()));

        assertThat(platformConfigService.getAllConfig()).hasSize(1);
    }

    @Test
    void updateConfig_keyNotFound_throwsResourceNotFoundException() {
        given(platformConfigRepository.findByConfigKey("missing")).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                platformConfigService.updateConfig("missing",
                        UpdatePlatformConfigRequest.builder().configValue("1").build(),
                        UUID.randomUUID()));
    }
}
