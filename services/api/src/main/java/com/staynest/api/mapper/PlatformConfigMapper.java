package com.staynest.api.mapper;

import com.staynest.api.dto.response.PlatformConfigResponse;
import com.staynest.api.entity.PlatformConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlatformConfigMapper {

    PlatformConfigResponse toResponse(PlatformConfig config);

    List<PlatformConfigResponse> toResponseList(List<PlatformConfig> configs);
}
