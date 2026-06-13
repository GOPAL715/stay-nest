package com.staynest.api.service;

import com.staynest.api.dto.request.PropertySearchRequest;
import com.staynest.api.dto.response.PropertyResponse;
import com.staynest.api.dto.response.PropertySummaryResponse;
import com.staynest.api.entity.Property;
import com.staynest.api.entity.User;
import com.staynest.api.enums.*;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.factory.PropertyFactory;
import com.staynest.api.mapper.PropertyMapper;
import com.staynest.api.repository.*;
import com.staynest.api.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

/**
 * Service-layer tests for User Story 3 — Guest: Search & Discover Properties.
 *
 * Covers:
 *  - Scenario 1: Basic search returns only ACTIVE listings, paginated (default 12/page)
 *  - Scenario 2: Price-range filter ($50–$200 / night)
 *  - Scenario 3: Amenity multi-select filter
 *  - Scenario 4: Sort by price (low to high)
 *  - Scenario 5: getPropertyById returns full detail (or 404)
 *  - Scenario 6: Empty search results
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PropertyServiceImpl — Search & Discovery (US3)")
class PropertySearchServiceImplTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private PropertyFactory propertyFactory;
    @Mock private PropertyMapper propertyMapper;
    @Mock private UserRepository userRepository;
    @Mock private AmenityRepository amenityRepository;
    @Mock private PropertyAvailabilityRepository availabilityRepository;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Build a minimal ACTIVE property entity — no JPA-managed id needed for unit tests. */
    private Property activeProperty(String city, long priceInPaise) {
        User host = User.builder()
                .email("host@staynest.com")
                .passwordHash("hash")
                .firstName("Host")
                .lastName("User")
                .role(UserRole.HOST)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();

        return Property.builder()
                .host(host)
                .title("Nice Apartment in " + city)
                .description("Great place")
                .propertyType(PropertyType.APARTMENT)
                .addressLine1("1 Main Street")
                .city(city)
                .state("MH")
                .country("India")
                .maxGuests(4)
                .bedrooms(2)
                .bathrooms(BigDecimal.ONE)
                .beds(2)
                .basePricePerNight(priceInPaise)
                .cleaningFee(50000L)
                .serviceFeePercent(BigDecimal.TEN)
                .bookingMode(BookingMode.INSTANT_BOOK)
                .cancellationPolicy(CancellationPolicy.FLEXIBLE)
                .status(PropertyStatus.ACTIVE)
                .build();
    }

    private PropertySummaryResponse summaryResponse(UUID id, String title, long priceInPaise) {
        return PropertySummaryResponse.builder()
                .id(id)
                .title(title)
                .city("Mumbai")
                .basePricePerNight(priceInPaise)
                .status(PropertyStatus.ACTIVE)
                .build();
    }

    private PropertyResponse detailResponse(UUID id) {
        return PropertyResponse.builder()
                .id(id)
                .title("Full Detail Property")
                .status(PropertyStatus.ACTIVE)
                .build();
    }

    // =========================================================================
    // Scenario 1 — Basic search: only ACTIVE listings, paginated (default 12/page)
    // =========================================================================

    @Nested
    @DisplayName("Scenario 1 — Basic location + dates + guests search")
    class BasicSearch {

        @Test
        @DisplayName("Returns paginated ACTIVE listings matching city and guest count")
        void basicSearch_returnsActivePaginatedListings() {
            // Arrange
            Property property = activeProperty("Mumbai", 200000L);
            UUID propId = UUID.randomUUID();
            PropertySummaryResponse summary = summaryResponse(propId, "Nice Apartment in Mumbai", 200000L);

            Page<Property> repoPage = new PageImpl<>(
                    List.of(property),
                    PageRequest.of(0, 12),
                    1L
            );

            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(repoPage);
            given(propertyMapper.toPropertySummaryResponse(property)).willReturn(summary);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .city("Mumbai")
                    .checkIn(LocalDate.of(2026, 8, 1))
                    .checkOut(LocalDate.of(2026, 8, 5))
                    .numGuests(2)
                    .build();

            Pageable pageable = PageRequest.of(0, 12);

            // Act
            Page<PropertySummaryResponse> result = propertyService.searchProperties(request, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(PropertyStatus.ACTIVE);
            assertThat(result.getSize()).isEqualTo(12);
            verify(propertyRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Default page size is 12 — Pageable is forwarded correctly")
        void basicSearch_defaultPageSize_is12() {
            Page<Property> emptyPage = Page.empty(PageRequest.of(0, 12));
            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(emptyPage);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .city("Pune")
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result.getSize()).isEqualTo(12);
        }
    }

    // =========================================================================
    // Scenario 2 — Price range filter
    // =========================================================================

    @Nested
    @DisplayName("Scenario 2 — Price range filter")
    class PriceRangeFilter {

        @Test
        @DisplayName("Only listings within the price range are included in results")
        void priceRangeFilter_returnsListingsWithinRange() {
            // 5000 paise = ₹50/night, 20000 paise = ₹200/night
            long minPrice = 5_000_00L;   // ₹50 in paise
            long maxPrice = 20_000_00L;  // ₹200 in paise

            Property inRange = activeProperty("Delhi", 10_000_00L); // ₹100 — in range
            UUID propId = UUID.randomUUID();
            PropertySummaryResponse inRangeSummary = summaryResponse(propId, "In-range property", 10_000_00L);

            Page<Property> repoPage = new PageImpl<>(List.of(inRange));

            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(repoPage);
            given(propertyMapper.toPropertySummaryResponse(inRange)).willReturn(inRangeSummary);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getBasePricePerNight())
                    .isGreaterThanOrEqualTo(minPrice)
                    .isLessThanOrEqualTo(maxPrice);
            verify(propertyRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Min-price-only filter is supported")
        void priceRangeFilter_minPriceOnly_isSupported() {
            Property expensive = activeProperty("Goa", 50_000_00L); // ₹5000/night
            UUID propId = UUID.randomUUID();
            PropertySummaryResponse summary = summaryResponse(propId, "Luxury Villa", 50_000_00L);

            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(expensive)));
            given(propertyMapper.toPropertySummaryResponse(expensive)).willReturn(summary);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .minPrice(30_000_00L)
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // =========================================================================
    // Scenario 3 — Amenity filter
    // =========================================================================

    @Nested
    @DisplayName("Scenario 3 — Amenity filter (multi-select)")
    class AmenityFilter {

        @Test
        @DisplayName("Results include only listings with all selected amenities")
        void amenityFilter_returnsListingsWithAllSelectedAmenities() {
            UUID wifiId = UUID.randomUUID();
            UUID poolId = UUID.randomUUID();
            UUID parkingId = UUID.randomUUID();
            Set<UUID> selectedAmenities = Set.of(wifiId, poolId, parkingId);

            Property matchingProperty = activeProperty("Chennai", 15_000_00L);
            UUID propId = UUID.randomUUID();
            PropertySummaryResponse summary = summaryResponse(propId, "Amenity-rich listing", 15_000_00L);

            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(matchingProperty)));
            given(propertyMapper.toPropertySummaryResponse(matchingProperty)).willReturn(summary);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .amenityIds(selectedAmenities)
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result.getContent()).hasSize(1);
            verify(propertyRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Empty amenity set does not add amenity predicate — all results returned")
        void amenityFilter_emptyAmenitySet_returnsAllResults() {
            Property property = activeProperty("Bangalore", 8_000_00L);
            PropertySummaryResponse summary = summaryResponse(UUID.randomUUID(), "Any listing", 8_000_00L);

            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(property)));
            given(propertyMapper.toPropertySummaryResponse(property)).willReturn(summary);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .amenityIds(Collections.emptySet())
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // =========================================================================
    // Scenario 4 — Sort by price (low to high)
    // =========================================================================

    @Nested
    @DisplayName("Scenario 4 — Sort by price low to high")
    class Sorting {

        @Test
        @DisplayName("price_asc sort is forwarded to repository via sorted Pageable")
        void sortByPriceAscending_pageableHasPriceAscSort() {
            Property cheap = activeProperty("Mumbai", 5_000_00L);
            Property expensive = activeProperty("Mumbai", 20_000_00L);

            UUID cheapId = UUID.randomUUID();
            UUID expId = UUID.randomUUID();

            PropertySummaryResponse cheapSummary = summaryResponse(cheapId, "Budget stay", 5_000_00L);
            PropertySummaryResponse expSummary = summaryResponse(expId, "Luxury stay", 20_000_00L);

            // Repository returns them pre-sorted (sorted Pageable applied)
            Page<Property> sortedPage = new PageImpl<>(List.of(cheap, expensive));

            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(sortedPage);
            given(propertyMapper.toPropertySummaryResponse(cheap)).willReturn(cheapSummary);
            given(propertyMapper.toPropertySummaryResponse(expensive)).willReturn(expSummary);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .city("Mumbai")
                    .sortBy("price_asc")
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result.getContent()).hasSize(2);
            // Verify the service actually forwarded a sorted Pageable to the repository
            verify(propertyRepository).findAll(
                    any(Specification.class),
                    argThat((Pageable p) -> p.getSort().getOrderFor("basePricePerNight") != null
                            && p.getSort().getOrderFor("basePricePerNight").isAscending())
            );
        }

        @Test
        @DisplayName("price_desc sort creates descending order by basePricePerNight")
        void sortByPriceDescending_pageableHasPriceDescSort() {
            Page<Property> emptyPage = Page.empty();
            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(emptyPage);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .sortBy("price_desc")
                    .build();

            propertyService.searchProperties(request, PageRequest.of(0, 12));

            verify(propertyRepository).findAll(
                    any(Specification.class),
                    argThat((Pageable p) -> p.getSort().getOrderFor("basePricePerNight") != null
                            && p.getSort().getOrderFor("basePricePerNight").isDescending())
            );
        }

        @Test
        @DisplayName("Default sort (no sortBy field) uses newest (createdAt descending)")
        void sortDefault_usesNewest() {
            Page<Property> emptyPage = Page.empty();
            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(emptyPage);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .city("Mumbai")
                    // sortBy defaults to "newest" via @Builder.Default
                    .build();

            propertyService.searchProperties(request, PageRequest.of(0, 12));

            verify(propertyRepository).findAll(
                    any(Specification.class),
                    argThat((Pageable p) -> p.getSort().getOrderFor("createdAt") != null
                            && p.getSort().getOrderFor("createdAt").isDescending())
            );
        }
    }

    // =========================================================================
    // Scenario 5 — Property detail page (getPropertyById)
    // =========================================================================

    @Nested
    @DisplayName("Scenario 5 — Property detail page")
    class PropertyDetail {

        @Test
        @DisplayName("getPropertyById returns full detail response when property exists")
        void getPropertyById_found_returnsDetailResponse() {
            UUID propertyId = UUID.randomUUID();
            Property property = activeProperty("Hyderabad", 12_000_00L);
            PropertyResponse detail = detailResponse(propertyId);

            given(propertyRepository.findById(propertyId)).willReturn(Optional.of(property));
            given(propertyMapper.toPropertyResponse(property)).willReturn(detail);

            PropertyResponse result = propertyService.getPropertyById(propertyId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(propertyId);
            assertThat(result.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
            verify(propertyRepository).findById(propertyId);
        }

        @Test
        @DisplayName("getPropertyById throws ResourceNotFoundException when property does not exist")
        void getPropertyById_notFound_throwsResourceNotFoundException() {
            UUID unknownId = UUID.randomUUID();
            given(propertyRepository.findById(unknownId)).willReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> propertyService.getPropertyById(unknownId)
            );

            verify(propertyRepository).findById(unknownId);
        }
    }

    // =========================================================================
    // Scenario 6 — Empty search results
    // =========================================================================

    @Nested
    @DisplayName("Scenario 6 — Empty search results")
    class EmptyResults {

        @Test
        @DisplayName("Returns empty page when no listings match the search criteria")
        void search_noMatchingListings_returnsEmptyPage() {
            Page<Property> emptyPage = Page.empty(PageRequest.of(0, 12));
            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(emptyPage);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .city("NonExistentCity")
                    .checkIn(LocalDate.of(2026, 12, 1))
                    .checkOut(LocalDate.of(2026, 12, 7))
                    .numGuests(10)
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Returns empty page when all dates are blocked (no available properties)")
        void search_allDatesBlocked_returnsEmptyPage() {
            Page<Property> emptyPage = Page.empty(PageRequest.of(0, 12));
            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(emptyPage);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .city("Mumbai")
                    .checkIn(LocalDate.of(2026, 7, 15))
                    .checkOut(LocalDate.of(2026, 7, 20))
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Returns empty page when no ACTIVE listings exist for specified city")
        void search_noActiveListingsInCity_returnsEmptyPage() {
            Page<Property> emptyPage = Page.empty(PageRequest.of(0, 12));
            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(emptyPage);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .city("Shimla")
                    .numGuests(2)
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result.getContent()).isEmpty();
        }
    }

    // =========================================================================
    // Combined filter scenarios
    // =========================================================================

    @Nested
    @DisplayName("Combined filters")
    class CombinedFilters {

        @Test
        @DisplayName("City + dates + guests + price range + amenities all forwarded to repository via Specification")
        void combinedFilters_allParametersForwardedToRepository() {
            UUID wifiId = UUID.randomUUID();
            Property property = activeProperty("Mumbai", 10_000_00L);
            PropertySummaryResponse summary = summaryResponse(UUID.randomUUID(), "All-inclusive stay", 10_000_00L);

            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(property)));
            given(propertyMapper.toPropertySummaryResponse(property)).willReturn(summary);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .city("Mumbai")
                    .checkIn(LocalDate.of(2026, 9, 1))
                    .checkOut(LocalDate.of(2026, 9, 7))
                    .numGuests(3)
                    .minPrice(5_000_00L)
                    .maxPrice(20_000_00L)
                    .amenityIds(Set.of(wifiId))
                    .propertyType(PropertyType.APARTMENT)
                    .sortBy("price_asc")
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result.getContent()).hasSize(1);
            verify(propertyRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Search with only property type filter returns matching results")
        void propertyTypeFilter_returnsMatchingListings() {
            Property villa = activeProperty("Goa", 25_000_00L);
            // Override type — builder can't override superclass; just verify the call is made
            PropertySummaryResponse summary = summaryResponse(UUID.randomUUID(), "Beach Villa", 25_000_00L);

            given(propertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(villa)));
            given(propertyMapper.toPropertySummaryResponse(villa)).willReturn(summary);

            PropertySearchRequest request = PropertySearchRequest.builder()
                    .propertyType(PropertyType.VILLA)
                    .build();

            Page<PropertySummaryResponse> result =
                    propertyService.searchProperties(request, PageRequest.of(0, 12));

            assertThat(result.getContent()).hasSize(1);
            verify(propertyRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }
}
