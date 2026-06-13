package com.staynest.api.service.impl;

import com.staynest.api.dto.GeocodeAddress;
import com.staynest.api.dto.GeoCoordinates;
import com.staynest.api.dto.request.*;
import com.staynest.api.dto.response.*;
import com.staynest.api.entity.*;
import com.staynest.api.enums.AvailabilityBlockReason;
import com.staynest.api.enums.PropertyStatus;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.factory.PropertyFactory;
import com.staynest.api.mapper.PropertyMapper;
import com.staynest.api.repository.*;
import com.staynest.api.service.GeocodingService;
import com.staynest.api.service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyFactory propertyFactory;
    private final PropertyMapper propertyMapper;
    private final UserRepository userRepository;
    private final AmenityRepository amenityRepository;
    private final PropertyAvailabilityRepository availabilityRepository;
    private final GeocodingService geocodingService;

    @Override
    @Transactional
    public PropertyResponse createListing(CreatePropertyRequest request, UUID hostId) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Host not found"));

        Property property = propertyFactory.createDraft(request, host);
        resolveCoordinates(property, request.getLatitude(), request.getLongitude());
        propertyRepository.save(property);
        log.info("Property created [{}] by host [{}]", property.getId(), hostId);
        return propertyMapper.toPropertyResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse updateListing(UUID propertyId, UpdatePropertyRequest request, UUID hostId) {
        Property property = getPropertyOwnedByHost(propertyId, hostId);

        if (property.getStatus() == PropertyStatus.ACTIVE || property.getStatus() == PropertyStatus.SUSPENDED) {
            throw new BusinessRuleException("Cannot update an active or suspended listing. Contact admin.");
        }

        property.updateDetails(
                request.getTitle(), request.getDescription(), request.getPropertyType(),
                request.getAddressLine1(), request.getAddressLine2(),
                request.getCity(), request.getState(), request.getCountry(), request.getPostalCode(),
                request.getLatitude(), request.getLongitude(),
                request.getMaxGuests() != null ? request.getMaxGuests() : 0,
                request.getBedrooms() != null ? request.getBedrooms() : 0,
                request.getBathrooms(),
                request.getBeds() != null ? request.getBeds() : 0,
                request.getBasePricePerNight() != null ? request.getBasePricePerNight() : 0L,
                request.getCleaningFee() != null ? request.getCleaningFee() : -1L,
                request.getBookingMode(), request.getCancellationPolicy()
        );

        resolveCoordinatesAfterUpdate(property, request);
        propertyRepository.save(property);
        return propertyMapper.toPropertyResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse submitForReview(UUID propertyId, UUID hostId) {
        Property property = getPropertyOwnedByHost(propertyId, hostId);

        if (property.getStatus() != PropertyStatus.DRAFT) {
            throw new BusinessRuleException("Only DRAFT listings can be submitted for review. Current status: " + property.getStatus());
        }

        property.submitForReview();
        propertyRepository.save(property);
        log.info("Property [{}] submitted for review by host [{}]", propertyId, hostId);
        return propertyMapper.toPropertyResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        return propertyMapper.toPropertyResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertySummaryResponse> getMyListings(UUID hostId, Pageable pageable) {
        return propertyRepository
                .findByHostIdOrderByCreatedAtDesc(hostId, pageable)
                .map(propertyMapper::toPropertySummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertySummaryResponse> searchProperties(PropertySearchRequest request, Pageable pageable) {
        Specification<Property> spec = PropertySpecification.buildSearchSpec(
                request.getCity(),
                request.getNumGuests(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getPropertyType(),
                request.getAmenityIds(),
                request.getCheckIn(),
                request.getCheckOut()
        );

        // Apply sorting
        Pageable sortedPageable = applySorting(request.getSortBy(), pageable);

        return propertyRepository.findAll(spec, sortedPageable)
                .map(propertyMapper::toPropertySummaryResponse);
    }

    @Override
    @Transactional
    public PropertyResponse addPhotos(UUID propertyId, List<String> photoUrls, UUID hostId) {
        Property property = getPropertyOwnedByHost(propertyId, hostId);

        int startOrder = property.getPhotos().size();
        List<PropertyPhoto> newPhotos = new ArrayList<>();
        for (int i = 0; i < photoUrls.size(); i++) {
            boolean isCover = (startOrder == 0 && i == 0); // first photo is cover if no photos yet
            newPhotos.add(PropertyPhoto.builder()
                    .property(property)
                    .url(photoUrls.get(i))
                    .displayOrder(startOrder + i)
                    .isCover(isCover)
                    .build());
        }
        property.getPhotos().addAll(newPhotos);
        propertyRepository.save(property);
        return propertyMapper.toPropertyResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse updateAmenities(UUID propertyId, Set<UUID> amenityIds, UUID hostId) {
        Property property = getPropertyOwnedByHost(propertyId, hostId);
        Set<Amenity> amenities = amenityRepository.findByIdIn(amenityIds);
        property.setAmenities(amenities);
        propertyRepository.save(property);
        return propertyMapper.toPropertyResponse(property);
    }

    @Override
    @Transactional
    public void deleteProperty(UUID propertyId, UUID actorId, boolean isAdmin) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        if (!isAdmin && !Objects.equals(property.getHost().getId(), actorId)) {
            throw new AccessDeniedException("You do not own this property");
        }

        // Soft delete: mark as SUSPENDED (or you could add a deletedAt field)
        property.suspend("Deleted by " + (isAdmin ? "admin" : "host"));
        propertyRepository.save(property);
        log.info("Property [{}] deleted by [{}] (admin={})", propertyId, actorId, isAdmin);
    }

    @Override
    @Transactional
    public PropertyAvailabilityResponse blockAvailability(UUID propertyId, BlockAvailabilityRequest request, UUID hostId) {
        Property property = getPropertyOwnedByHost(propertyId, hostId);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessRuleException("End date must be after start date");
        }

        // Check for existing blocks
        List<PropertyAvailability> overlapping = availabilityRepository.findOverlapping(
                propertyId, request.getStartDate(), request.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new BusinessRuleException("Date range overlaps with existing availability block");
        }

        PropertyAvailability block = PropertyAvailability.builder()
                .property(property)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(AvailabilityBlockReason.BLOCKED)
                .build();

        availabilityRepository.save(block);
        return propertyMapper.toPropertyAvailabilityResponse(block);
    }

    @Override
    @Transactional
    public void unblockAvailability(UUID propertyId, UUID blockId, UUID hostId) {
        // Ensure host owns the property
        getPropertyOwnedByHost(propertyId, hostId);

        PropertyAvailability block = availabilityRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability block not found"));

        if (!Objects.equals(block.getProperty().getId(), propertyId)) {
            throw new AccessDeniedException("This block does not belong to this property");
        }

        if (block.getReason() == AvailabilityBlockReason.BOOKED) {
            throw new BusinessRuleException("Cannot manually remove a BOOKED block. Cancel the booking instead.");
        }

        availabilityRepository.delete(block);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyAvailabilityResponse> getAvailability(UUID propertyId) {
        return availabilityRepository.findByPropertyIdOrderByStartDateAsc(propertyId).stream()
                .map(propertyMapper::toPropertyAvailabilityResponse)
                .collect(Collectors.toList());
    }

    // --- Private helpers ---

    private Property getPropertyOwnedByHost(UUID propertyId, UUID hostId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        if (!Objects.equals(property.getHost().getId(), hostId)) {
            throw new AccessDeniedException("You do not own this property");
        }

        return property;
    }

    private Pageable applySorting(String sortBy, Pageable pageable) {
        Sort sort = switch (sortBy != null ? sortBy : "newest") {
            case "price_asc"  -> Sort.by("basePricePerNight").ascending();
            case "price_desc" -> Sort.by("basePricePerNight").descending();
            default           -> Sort.by("createdAt").descending(); // "newest"
        };
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private void resolveCoordinates(Property property, BigDecimal requestLat, BigDecimal requestLng) {
        if (requestLat != null && requestLng != null) {
            return;
        }
        geocodePropertyAddress(property).ifPresent(coords ->
                property.setCoordinates(coords.latitude(), coords.longitude()));
    }

    private void resolveCoordinatesAfterUpdate(Property property, UpdatePropertyRequest request) {
        if (request.getLatitude() != null && request.getLongitude() != null) {
            return;
        }
        if (property.getLatitude() != null && property.getLongitude() != null && !addressFieldsChanged(request)) {
            return;
        }
        geocodePropertyAddress(property).ifPresent(coords ->
                property.setCoordinates(coords.latitude(), coords.longitude()));
    }

    private Optional<GeoCoordinates> geocodePropertyAddress(Property property) {
        GeocodeAddress address = GeocodeAddress.of(
                property.getAddressLine1(),
                property.getAddressLine2(),
                property.getCity(),
                property.getState(),
                property.getCountry(),
                property.getPostalCode()
        );
        return geocodingService.geocode(address);
    }

    private boolean addressFieldsChanged(UpdatePropertyRequest request) {
        return request.getAddressLine1() != null
                || request.getAddressLine2() != null
                || request.getCity() != null
                || request.getState() != null
                || request.getCountry() != null
                || request.getPostalCode() != null;
    }
}
