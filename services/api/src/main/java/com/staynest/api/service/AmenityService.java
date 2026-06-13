package com.staynest.api.service;

import com.staynest.api.dto.request.CreateAmenityRequest;
import com.staynest.api.dto.response.AmenityResponse;

import java.util.List;
import java.util.UUID;

public interface AmenityService {

    List<AmenityResponse> getAllAmenities();

    AmenityResponse addAmenity(CreateAmenityRequest request, UUID adminId);
}
