package com.staynest.api.service.impl;

import com.staynest.api.dto.request.CreateAmenityRequest;
import com.staynest.api.dto.response.AmenityResponse;
import com.staynest.api.entity.Amenity;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.mapper.PropertyMapper;
import com.staynest.api.repository.AmenityRepository;
import com.staynest.api.service.AmenityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;
    private final PropertyMapper propertyMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(propertyMapper::toAmenityResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AmenityResponse addAmenity(CreateAmenityRequest request, UUID adminId) {
        if (amenityRepository.existsByName(request.getName().trim())) {
            throw new BusinessRuleException("An amenity with this name already exists");
        }
        Amenity amenity = Amenity.builder()
                .name(request.getName().trim())
                .icon(request.getIcon())
                .category(request.getCategory())
                .build();
        amenityRepository.save(amenity);
        log.info("Amenity created: {} by admin {}", amenity.getName(), adminId);
        return propertyMapper.toAmenityResponse(amenity);
    }
}
