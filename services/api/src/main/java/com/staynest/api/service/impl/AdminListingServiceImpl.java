package com.staynest.api.service.impl;

import com.staynest.api.dto.request.ModerationActionRequest;
import com.staynest.api.dto.response.PropertyResponse;
import com.staynest.api.dto.response.PropertySummaryResponse;
import com.staynest.api.entity.Property;
import com.staynest.api.enums.BookingStatus;
import com.staynest.api.enums.PropertyStatus;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.mapper.PropertyMapper;
import com.staynest.api.repository.BookingRepository;
import com.staynest.api.repository.PropertyRepository;
import com.staynest.api.service.AdminListingService;
import com.staynest.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminListingServiceImpl implements AdminListingService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final PropertyMapper propertyMapper;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public Page<PropertySummaryResponse> getPendingListings(Pageable pageable) {
        return getListingsByStatus(PropertyStatus.PENDING, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertySummaryResponse> getListingsByStatus(PropertyStatus status, Pageable pageable) {
        return propertyRepository.findByStatus(status, pageable)
                .map(propertyMapper::toPropertySummaryResponse);
    }

    @Override
    @Transactional
    public PropertyResponse approveListing(UUID propertyId, UUID adminId) {
        Property property = getPropertyOrThrow(propertyId);

        if (property.getStatus() != PropertyStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING listings can be approved. Current: " + property.getStatus());
        }

        property.approve();
        propertyRepository.save(property);

        // Notify host
        emailService.sendGenericEmail(
                property.getHost().getEmail(),
                "Your StayNest listing has been approved! 🎉",
                "Great news! Your listing \"" + property.getTitle() + "\" has been approved and is now visible to guests."
        );

        log.info("Listing [{}] approved by admin [{}]", propertyId, adminId);
        return propertyMapper.toPropertyResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse rejectListing(UUID propertyId, ModerationActionRequest request, UUID adminId) {
        Property property = getPropertyOrThrow(propertyId);

        if (property.getStatus() != PropertyStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING listings can be rejected. Current: " + property.getStatus());
        }

        property.reject(request.getReason());
        propertyRepository.save(property);

        emailService.sendGenericEmail(
                property.getHost().getEmail(),
                "Update on your StayNest listing review",
                "Your listing \"" + property.getTitle() + "\" was not approved.\n\nReason: " + request.getReason()
                        + "\n\nPlease update your listing and resubmit for review."
        );

        log.info("Listing [{}] rejected by admin [{}]: {}", propertyId, adminId, request.getReason());
        return propertyMapper.toPropertyResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse requestChanges(UUID propertyId, ModerationActionRequest request, UUID adminId) {
        Property property = getPropertyOrThrow(propertyId);

        property.reject(request.getReason()); // Back to DRAFT
        propertyRepository.save(property);

        emailService.sendGenericEmail(
                property.getHost().getEmail(),
                "Changes requested for your StayNest listing",
                "Our team has reviewed \"" + property.getTitle() + "\" and requests the following changes:\n\n"
                        + request.getReason() + "\n\nPlease update your listing and resubmit."
        );

        log.info("Changes requested for listing [{}] by admin [{}]", propertyId, adminId);
        return propertyMapper.toPropertyResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse suspendListing(UUID propertyId, ModerationActionRequest request, UUID adminId) {
        Property property = getPropertyOrThrow(propertyId);

        if (property.getStatus() != PropertyStatus.ACTIVE) {
            throw new BusinessRuleException("Only ACTIVE listings can be suspended. Current: " + property.getStatus());
        }

        property.suspend(request.getReason());
        propertyRepository.save(property);

        // Cancel all PENDING bookings for this property
        bookingRepository.findByPropertyIdAndStatus(propertyId, BookingStatus.PENDING)
                .forEach(booking -> {
                    booking.cancel(null, "Property suspended by admin: " + request.getReason());
                    bookingRepository.save(booking);
                });

        emailService.sendGenericEmail(
                property.getHost().getEmail(),
                "Your StayNest listing has been suspended",
                "Your listing \"" + property.getTitle() + "\" has been suspended.\n\nReason: " + request.getReason()
                        + "\n\nPlease contact support for further information."
        );

        log.info("Listing [{}] suspended by admin [{}]: {}", propertyId, adminId, request.getReason());
        return propertyMapper.toPropertyResponse(property);
    }

    private Property getPropertyOrThrow(UUID propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
    }
}
