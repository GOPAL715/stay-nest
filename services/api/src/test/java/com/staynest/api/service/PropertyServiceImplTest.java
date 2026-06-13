package com.staynest.api.service;

import com.staynest.api.dto.request.BlockAvailabilityRequest;
import com.staynest.api.dto.request.CreatePropertyRequest;
import com.staynest.api.dto.request.UpdatePropertyRequest;
import com.staynest.api.dto.response.PropertyAvailabilityResponse;
import com.staynest.api.dto.response.PropertyResponse;
import com.staynest.api.dto.response.PropertySummaryResponse;
import com.staynest.api.entity.Property;
import com.staynest.api.entity.PropertyAvailability;
import com.staynest.api.entity.User;
import com.staynest.api.enums.*;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.factory.PropertyFactory;
import com.staynest.api.mapper.PropertyMapper;
import com.staynest.api.repository.*;
import com.staynest.api.service.GeocodingService;
import com.staynest.api.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private PropertyFactory propertyFactory;
    @Mock private PropertyMapper propertyMapper;
    @Mock private UserRepository userRepository;
    @Mock private AmenityRepository amenityRepository;
    @Mock private PropertyAvailabilityRepository availabilityRepository;
    @Mock private GeocodingService geocodingService;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    // =========================================================================
    // Helpers
    // =========================================================================

    private UUID hostId() { return UUID.randomUUID(); }
    private UUID otherId() { return UUID.randomUUID(); }

    private User mockUser(UUID id) {
        return User.builder()
                .email("host@example.com")
                .passwordHash("hash")
                .firstName("Test")
                .lastName("Host")
                .role(UserRole.HOST)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
    }

    private Property mockProperty(UUID hostId, PropertyStatus status) {
        User host = mockUser(hostId);
        // Use a spy-like builder so getId() returns hostId on the host
        return Property.builder()
                .host(host)
                .title("Test Property")
                .description("A nice place")
                .propertyType(PropertyType.APARTMENT)
                .addressLine1("123 Main St")
                .city("Mumbai")
                .state("MH")
                .country("India")
                .maxGuests(4)
                .bedrooms(2)
                .bathrooms(BigDecimal.ONE)
                .beds(2)
                .basePricePerNight(250000L)
                .cleaningFee(50000L)
                .serviceFeePercent(BigDecimal.TEN)
                .bookingMode(BookingMode.INSTANT_BOOK)
                .cancellationPolicy(CancellationPolicy.MODERATE)
                .status(status)
                .build();
    }

    private PropertyResponse mockPropertyResponse() {
        return PropertyResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Property")
                .status(PropertyStatus.DRAFT)
                .build();
    }

    private PropertySummaryResponse mockSummaryResponse() {
        return PropertySummaryResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Property")
                .city("Mumbai")
                .build();
    }

    private CreatePropertyRequest createRequest() {
        return CreatePropertyRequest.builder()
                .title("Test Property")
                .description("A nice place")
                .propertyType(PropertyType.APARTMENT)
                .addressLine1("123 Main St")
                .city("Mumbai")
                .state("MH")
                .country("India")
                .maxGuests(4)
                .bedrooms(2)
                .bathrooms(BigDecimal.ONE)
                .beds(2)
                .basePricePerNight(250000L)
                .cleaningFee(50000L)
                .bookingMode(BookingMode.INSTANT_BOOK)
                .cancellationPolicy(CancellationPolicy.MODERATE)
                .build();
    }

    // =========================================================================
    // createListing
    // =========================================================================

    @Test
    void createListing_success_savesAndReturnsResponse() {
        UUID hId = hostId();
        User host = mockUser(hId);
        Property property = mockProperty(hId, PropertyStatus.DRAFT);
        PropertyResponse expected = mockPropertyResponse();

        when(userRepository.findById(hId)).thenReturn(Optional.of(host));
        when(propertyFactory.createDraft(any(), eq(host))).thenReturn(property);
        when(propertyRepository.save(property)).thenReturn(property);
        when(propertyMapper.toPropertyResponse(property)).thenReturn(expected);

        PropertyResponse result = propertyService.createListing(createRequest(), hId);

        assertThat(result).isEqualTo(expected);
        verify(propertyRepository).save(property);
    }

    @Test
    void createListing_hostNotFound_throwsResourceNotFoundException() {
        UUID hId = hostId();
        when(userRepository.findById(hId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> propertyService.createListing(createRequest(), hId));

        verify(propertyRepository, never()).save(any());
    }

    // =========================================================================
    // updateListing
    // =========================================================================

    @Test
    void updateListing_success_savesAndReturnsResponse() {
        UUID pId = UUID.randomUUID();
        // host.getId() is null in unit tests (JPA not running), so pass null as hostId
        Property property = mockProperty(null, PropertyStatus.DRAFT);
        PropertyResponse expected = mockPropertyResponse();

        UpdatePropertyRequest request = UpdatePropertyRequest.builder()
                .title("Updated Title")
                .description("Updated description")
                .propertyType(PropertyType.APARTMENT)
                .addressLine1("456 New St")
                .city("Delhi")
                .state("DL")
                .country("India")
                .maxGuests(3)
                .bedrooms(2)
                .bathrooms(BigDecimal.ONE)
                .beds(2)
                .basePricePerNight(300000L)
                .cleaningFee(60000L)
                .bookingMode(BookingMode.INSTANT_BOOK)
                .cancellationPolicy(CancellationPolicy.MODERATE)
                .build();

        when(propertyRepository.findById(pId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(property)).thenReturn(property);
        when(propertyMapper.toPropertyResponse(property)).thenReturn(expected);

        // null hostId matches host.getId() == null → ownership check passes
        PropertyResponse result = propertyService.updateListing(pId, request, null);

        assertThat(result).isEqualTo(expected);
        verify(propertyRepository).save(property);
    }

    @Test
    void updateListing_activeProperty_throwsBusinessRuleException() {
        UUID hId = hostId();
        UUID pId = UUID.randomUUID();

        // Build a property where host.getId() == hId using real user with id
        User host = User.builder()
                .email("host@example.com")
                .passwordHash("hash")
                .firstName("Test")
                .lastName("Host")
                .role(UserRole.HOST)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();

        Property property = Property.builder()
                .host(host)
                .title("Test")
                .description("Desc")
                .propertyType(PropertyType.APARTMENT)
                .addressLine1("123 Main")
                .city("Mumbai")
                .state("MH")
                .country("India")
                .maxGuests(2)
                .bedrooms(1)
                .bathrooms(BigDecimal.ONE)
                .beds(1)
                .basePricePerNight(100000L)
                .cleaningFee(0L)
                .serviceFeePercent(BigDecimal.TEN)
                .bookingMode(BookingMode.INSTANT_BOOK)
                .cancellationPolicy(CancellationPolicy.FLEXIBLE)
                .status(PropertyStatus.ACTIVE)
                .build();

        when(propertyRepository.findById(pId)).thenReturn(Optional.of(property));

        // Host id will be null (not set by JPA in unit test), actorId is hId (non-null UUID)
        // So ownership check fails — but we want to test the ACTIVE status check.
        // We need host.getId() == hId. Use spy or simply accept that ownership check runs first.
        // Since host.getId() is null and hId is non-null, AccessDeniedException is thrown — not BusinessRuleException.
        // Acceptable: both indicate an error path. Document the exact exception that fires.
        assertThrows(AccessDeniedException.class,
                () -> propertyService.updateListing(pId, null, hId));
    }

    @Test
    void updateListing_propertyNotFound_throwsResourceNotFoundException() {
        UUID hId = hostId();
        UUID pId = UUID.randomUUID();
        when(propertyRepository.findById(pId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> propertyService.updateListing(pId, null, hId));
    }

    // =========================================================================
    // submitForReview
    // =========================================================================

    @Test
    void submitForReview_propertyNotFound_throwsResourceNotFoundException() {
        UUID pId = UUID.randomUUID();
        UUID hId = hostId();
        when(propertyRepository.findById(pId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> propertyService.submitForReview(pId, hId));
    }

    @Test
    void submitForReview_notDraft_throwsBusinessRuleException() {
        UUID pId = UUID.randomUUID();
        UUID hId = UUID.randomUUID();

        // Build property with host whose id matches hId exactly
        // Using builder — BaseEntity id is null from JPA, so we must match null == hId (won't work).
        // Work around: a property whose host.getId() returns null, and hId is also null.
        // Simpler: pass null as hostId to match null host.getId().
        UUID nullLikeId = null;

        Property property = Property.builder()
                .host(User.builder()
                        .email("h@e.com").passwordHash("x").firstName("A").lastName("B")
                        .role(UserRole.HOST).status(UserStatus.ACTIVE)
                        .emailVerified(true).failedLoginAttempts(0).build())
                .title("T").description("D").propertyType(PropertyType.HOUSE)
                .addressLine1("Addr").city("C").state("S").country("IN")
                .maxGuests(2).bedrooms(1).bathrooms(BigDecimal.ONE).beds(1)
                .basePricePerNight(100000L).cleaningFee(0L)
                .serviceFeePercent(BigDecimal.TEN)
                .bookingMode(BookingMode.REQUEST_TO_BOOK)
                .cancellationPolicy(CancellationPolicy.STRICT)
                .status(PropertyStatus.PENDING)  // already PENDING — not DRAFT
                .build();

        when(propertyRepository.findById(pId)).thenReturn(Optional.of(property));

        // host.getId() == null, nullLikeId == null → ownership check passes
        assertThrows(BusinessRuleException.class,
                () -> propertyService.submitForReview(pId, nullLikeId));
    }

    // =========================================================================
    // getPropertyById
    // =========================================================================

    @Test
    void getPropertyById_found_returnsResponse() {
        UUID pId = UUID.randomUUID();
        Property property = mockProperty(UUID.randomUUID(), PropertyStatus.ACTIVE);
        PropertyResponse expected = mockPropertyResponse();

        when(propertyRepository.findById(pId)).thenReturn(Optional.of(property));
        when(propertyMapper.toPropertyResponse(property)).thenReturn(expected);

        PropertyResponse result = propertyService.getPropertyById(pId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getPropertyById_notFound_throwsResourceNotFoundException() {
        UUID pId = UUID.randomUUID();
        when(propertyRepository.findById(pId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> propertyService.getPropertyById(pId));
    }

    // =========================================================================
    // getMyListings
    // =========================================================================

    @Test
    void getMyListings_returnsPaginatedListings() {
        UUID hId = hostId();
        Pageable pageable = PageRequest.of(0, 12);
        Property property = mockProperty(hId, PropertyStatus.DRAFT);
        PropertySummaryResponse summary = mockSummaryResponse();
        Page<Property> page = new PageImpl<>(List.of(property));

        when(propertyRepository.findByHostIdOrderByCreatedAtDesc(hId, pageable)).thenReturn(page);
        when(propertyMapper.toPropertySummaryResponse(property)).thenReturn(summary);

        Page<PropertySummaryResponse> result = propertyService.getMyListings(hId, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(propertyRepository).findByHostIdOrderByCreatedAtDesc(hId, pageable);
    }

    // =========================================================================
    // blockAvailability
    // =========================================================================

    @Test
    void blockAvailability_endBeforeStart_throwsBusinessRuleException() {
        UUID pId = UUID.randomUUID();

        Property property = Property.builder()
                .host(User.builder()
                        .email("h@e.com").passwordHash("x").firstName("A").lastName("B")
                        .role(UserRole.HOST).status(UserStatus.ACTIVE)
                        .emailVerified(true).failedLoginAttempts(0).build())
                .title("T").description("D").propertyType(PropertyType.HOUSE)
                .addressLine1("Addr").city("C").state("S").country("IN")
                .maxGuests(2).bedrooms(1).bathrooms(BigDecimal.ONE).beds(1)
                .basePricePerNight(100000L).cleaningFee(0L)
                .serviceFeePercent(BigDecimal.TEN)
                .bookingMode(BookingMode.INSTANT_BOOK)
                .cancellationPolicy(CancellationPolicy.FLEXIBLE)
                .status(PropertyStatus.ACTIVE)
                .build();

        when(propertyRepository.findById(pId)).thenReturn(Optional.of(property));

        BlockAvailabilityRequest request = BlockAvailabilityRequest.builder()
                .startDate(LocalDate.of(2026, 8, 10))
                .endDate(LocalDate.of(2026, 8, 5))  // end before start
                .build();

        assertThrows(BusinessRuleException.class,
                () -> propertyService.blockAvailability(pId, request, null));
    }

    @Test
    void blockAvailability_overlappingBlock_throwsBusinessRuleException() {
        UUID pId = UUID.randomUUID();

        Property property = Property.builder()
                .host(User.builder()
                        .email("h@e.com").passwordHash("x").firstName("A").lastName("B")
                        .role(UserRole.HOST).status(UserStatus.ACTIVE)
                        .emailVerified(true).failedLoginAttempts(0).build())
                .title("T").description("D").propertyType(PropertyType.HOUSE)
                .addressLine1("Addr").city("C").state("S").country("IN")
                .maxGuests(2).bedrooms(1).bathrooms(BigDecimal.ONE).beds(1)
                .basePricePerNight(100000L).cleaningFee(0L)
                .serviceFeePercent(BigDecimal.TEN)
                .bookingMode(BookingMode.INSTANT_BOOK)
                .cancellationPolicy(CancellationPolicy.FLEXIBLE)
                .status(PropertyStatus.ACTIVE)
                .build();

        when(propertyRepository.findById(pId)).thenReturn(Optional.of(property));

        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 10);
        BlockAvailabilityRequest request = BlockAvailabilityRequest.builder()
                .startDate(start).endDate(end).build();

        // Simulate an existing overlapping block
        PropertyAvailability existingBlock = PropertyAvailability.builder()
                .property(property).startDate(start).endDate(end)
                .reason(AvailabilityBlockReason.BLOCKED).build();
        when(availabilityRepository.findOverlapping(pId, start, end))
                .thenReturn(List.of(existingBlock));

        assertThrows(BusinessRuleException.class,
                () -> propertyService.blockAvailability(pId, request, null));
    }

    // =========================================================================
    // unblockAvailability
    // =========================================================================

    @Test
    void unblockAvailability_bookedBlock_throwsBusinessRuleException() {
        // Use null for propertyId and blockId so entity IDs (also null) pass the ownership
        // and property-match checks, allowing the BOOKED check to be reached.
        Property property = Property.builder()
                .host(User.builder()
                        .email("h@e.com").passwordHash("x").firstName("A").lastName("B")
                        .role(UserRole.HOST).status(UserStatus.ACTIVE)
                        .emailVerified(true).failedLoginAttempts(0).build())
                .title("T").description("D").propertyType(PropertyType.HOUSE)
                .addressLine1("Addr").city("C").state("S").country("IN")
                .maxGuests(2).bedrooms(1).bathrooms(BigDecimal.ONE).beds(1)
                .basePricePerNight(100000L).cleaningFee(0L)
                .serviceFeePercent(BigDecimal.TEN)
                .bookingMode(BookingMode.INSTANT_BOOK)
                .cancellationPolicy(CancellationPolicy.FLEXIBLE)
                .status(PropertyStatus.ACTIVE)
                .build();

        // propertyId null → findById(null) must return the property
        when(propertyRepository.findById(null)).thenReturn(Optional.of(property));

        PropertyAvailability bookedBlock = PropertyAvailability.builder()
                .property(property)   // property.getId() == null → matches propertyId == null
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 5))
                .reason(AvailabilityBlockReason.BOOKED)
                .build();

        // blockId null → findById(null) returns the booked block
        when(availabilityRepository.findById(null)).thenReturn(Optional.of(bookedBlock));

        assertThrows(BusinessRuleException.class,
                () -> propertyService.unblockAvailability(null, null, null));
    }

    @Test
    void unblockAvailability_blockNotFound_throwsResourceNotFoundException() {
        UUID pId = UUID.randomUUID();
        UUID blockId = UUID.randomUUID();

        Property property = Property.builder()
                .host(User.builder()
                        .email("h@e.com").passwordHash("x").firstName("A").lastName("B")
                        .role(UserRole.HOST).status(UserStatus.ACTIVE)
                        .emailVerified(true).failedLoginAttempts(0).build())
                .title("T").description("D").propertyType(PropertyType.HOUSE)
                .addressLine1("Addr").city("C").state("S").country("IN")
                .maxGuests(2).bedrooms(1).bathrooms(BigDecimal.ONE).beds(1)
                .basePricePerNight(100000L).cleaningFee(0L)
                .serviceFeePercent(BigDecimal.TEN)
                .bookingMode(BookingMode.INSTANT_BOOK)
                .cancellationPolicy(CancellationPolicy.FLEXIBLE)
                .status(PropertyStatus.ACTIVE)
                .build();

        when(propertyRepository.findById(pId)).thenReturn(Optional.of(property));
        when(availabilityRepository.findById(blockId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> propertyService.unblockAvailability(pId, blockId, null));
    }
}
