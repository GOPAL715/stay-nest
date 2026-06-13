package com.staynest.api.service;

import com.staynest.api.dto.request.*;
import com.staynest.api.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PropertyService {

    PropertyResponse createListing(CreatePropertyRequest request, UUID hostId);

    PropertyResponse updateListing(UUID propertyId, UpdatePropertyRequest request, UUID hostId);

    PropertyResponse submitForReview(UUID propertyId, UUID hostId);

    PropertyResponse getPropertyById(UUID propertyId);

    Page<PropertySummaryResponse> getMyListings(UUID hostId, Pageable pageable);

    Page<PropertySummaryResponse> searchProperties(PropertySearchRequest request, Pageable pageable);

    PropertyResponse addPhotos(UUID propertyId, List<String> photoUrls, UUID hostId);

    PropertyResponse updateAmenities(UUID propertyId, Set<UUID> amenityIds, UUID hostId);

    void deleteProperty(UUID propertyId, UUID actorId, boolean isAdmin);

    // Availability
    PropertyAvailabilityResponse blockAvailability(UUID propertyId, BlockAvailabilityRequest request, UUID hostId);

    void unblockAvailability(UUID propertyId, UUID blockId, UUID hostId);

    List<PropertyAvailabilityResponse> getAvailability(UUID propertyId);
}
