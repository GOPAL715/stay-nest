package com.staynest.api.service;

import com.staynest.api.dto.request.ModerationActionRequest;
import com.staynest.api.dto.response.PropertyResponse;
import com.staynest.api.entity.Property;
import com.staynest.api.entity.User;
import com.staynest.api.enums.*;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.mapper.PropertyMapper;
import com.staynest.api.repository.BookingRepository;
import com.staynest.api.repository.PropertyRepository;
import com.staynest.api.service.impl.AdminListingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.contains;

@ExtendWith(MockitoExtension.class)
class AdminListingServiceImplTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PropertyMapper propertyMapper;
    @Mock private EmailService emailService;

    @InjectMocks
    private AdminListingServiceImpl adminListingService;

    private User host() {
        return User.builder()
                .email("host@example.com")
                .passwordHash("hash")
                .firstName("Bob")
                .lastName("Host")
                .role(UserRole.HOST)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
    }

    private Property pendingProperty(User host) {
        return Property.builder()
                .host(host)
                .title("Beach Villa")
                .description("Nice place")
                .propertyType(PropertyType.VILLA)
                .addressLine1("1 Beach Rd")
                .city("Goa")
                .state("GA")
                .country("India")
                .maxGuests(4)
                .bedrooms(2)
                .bathrooms(BigDecimal.ONE)
                .beds(2)
                .basePricePerNight(500000L)
                .cleaningFee(100000L)
                .serviceFeePercent(BigDecimal.TEN)
                .bookingMode(BookingMode.INSTANT_BOOK)
                .cancellationPolicy(CancellationPolicy.FLEXIBLE)
                .status(PropertyStatus.PENDING)
                .build();
    }

    @Test
    void getPendingListings_returnsMappedPage() {
        User host = host();
        Property property = pendingProperty(host);
        Page<Property> page = new PageImpl<>(List.of(property));
        given(propertyRepository.findByStatus(eq(PropertyStatus.PENDING), any()))
                .willReturn(page);
        given(propertyMapper.toPropertySummaryResponse(property)).willReturn(
                com.staynest.api.dto.response.PropertySummaryResponse.builder()
                        .id(UUID.randomUUID())
                        .title("Beach Villa")
                        .status(PropertyStatus.PENDING)
                        .build());

        Page<?> result = adminListingService.getPendingListings(PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getListingsByStatus_active_returnsMappedPage() {
        User host = host();
        Property property = pendingProperty(host);
        property.approve();
        Page<Property> page = new PageImpl<>(List.of(property));
        given(propertyRepository.findByStatus(eq(PropertyStatus.ACTIVE), any()))
                .willReturn(page);
        given(propertyMapper.toPropertySummaryResponse(property)).willReturn(
                com.staynest.api.dto.response.PropertySummaryResponse.builder()
                        .id(UUID.randomUUID())
                        .title("Beach Villa")
                        .status(PropertyStatus.ACTIVE)
                        .build());

        Page<?> result = adminListingService.getListingsByStatus(PropertyStatus.ACTIVE, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void approveListing_pendingToActive_sendsEmail() {
        UUID propertyId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User host = host();
        Property property = pendingProperty(host);
        PropertyResponse response = PropertyResponse.builder().title("Beach Villa").build();

        given(propertyRepository.findById(propertyId)).willReturn(Optional.of(property));
        given(propertyRepository.save(property)).willReturn(property);
        given(propertyMapper.toPropertyResponse(property)).willReturn(response);

        PropertyResponse result = adminListingService.approveListing(propertyId, adminId);

        assertThat(property.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
        assertThat(result).isEqualTo(response);
        verify(emailService).sendGenericEmail(eq(host.getEmail()), anyString(), anyString());
    }

    @Test
    void approveListing_notPending_throwsBusinessRuleException() {
        UUID propertyId = UUID.randomUUID();
        Property property = pendingProperty(host());
        property.approve();

        given(propertyRepository.findById(propertyId)).willReturn(Optional.of(property));

        assertThrows(BusinessRuleException.class,
                () -> adminListingService.approveListing(propertyId, UUID.randomUUID()));
    }

    @Test
    void rejectListing_pendingToDraft_sendsEmailWithReason() {
        UUID propertyId = UUID.randomUUID();
        User host = host();
        Property property = pendingProperty(host);
        ModerationActionRequest request = ModerationActionRequest.builder()
                .reason("Photos unclear")
                .build();

        given(propertyRepository.findById(propertyId)).willReturn(Optional.of(property));
        given(propertyRepository.save(property)).willReturn(property);
        given(propertyMapper.toPropertyResponse(property)).willReturn(
                PropertyResponse.builder().title("Beach Villa").build());

        adminListingService.rejectListing(propertyId, request, UUID.randomUUID());

        assertThat(property.getStatus()).isEqualTo(PropertyStatus.DRAFT);
        assertThat(property.getRejectionReason()).isEqualTo("Photos unclear");
        verify(emailService).sendGenericEmail(eq(host.getEmail()), anyString(), contains("Photos unclear"));
    }

    @Test
    void suspendListing_cancelsPendingBookings() {
        UUID propertyId = UUID.randomUUID();
        User host = host();
        Property property = pendingProperty(host);
        property.approve();
        ModerationActionRequest request = ModerationActionRequest.builder().reason("Policy violation").build();

        given(propertyRepository.findById(propertyId)).willReturn(Optional.of(property));
        given(bookingRepository.findByPropertyIdAndStatus(propertyId, BookingStatus.PENDING))
                .willReturn(List.of());
        given(propertyRepository.save(property)).willReturn(property);
        given(propertyMapper.toPropertyResponse(property)).willReturn(
                PropertyResponse.builder().title("Beach Villa").build());

        adminListingService.suspendListing(propertyId, request, UUID.randomUUID());

        assertThat(property.getStatus()).isEqualTo(PropertyStatus.SUSPENDED);
    }

    @Test
    void getProperty_notFound_throwsResourceNotFoundException() {
        UUID propertyId = UUID.randomUUID();
        given(propertyRepository.findById(propertyId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminListingService.approveListing(propertyId, UUID.randomUUID()));
    }
}
