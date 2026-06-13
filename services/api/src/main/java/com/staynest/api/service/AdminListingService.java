package com.staynest.api.service;

import com.staynest.api.dto.request.ModerationActionRequest;
import com.staynest.api.dto.response.PropertyResponse;
import com.staynest.api.dto.response.PropertySummaryResponse;
import com.staynest.api.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminListingService {

    Page<PropertySummaryResponse> getPendingListings(Pageable pageable);

    Page<PropertySummaryResponse> getListingsByStatus(PropertyStatus status, Pageable pageable);

    PropertyResponse approveListing(UUID propertyId, UUID adminId);

    PropertyResponse rejectListing(UUID propertyId, ModerationActionRequest request, UUID adminId);

    PropertyResponse requestChanges(UUID propertyId, ModerationActionRequest request, UUID adminId);

    PropertyResponse suspendListing(UUID propertyId, ModerationActionRequest request, UUID adminId);
}
