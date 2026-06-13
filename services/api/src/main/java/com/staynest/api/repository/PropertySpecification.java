package com.staynest.api.repository;

import com.staynest.api.entity.Property;
import com.staynest.api.entity.PropertyAvailability;
import com.staynest.api.enums.BookingStatus;
import com.staynest.api.enums.PropertyStatus;
import com.staynest.api.enums.PropertyType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PropertySpecification {

    private PropertySpecification() {}

    public static Specification<Property> isActive() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), PropertyStatus.ACTIVE);
    }

    public static Specification<Property> inCity(String city) {
        if (city == null || city.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase().trim() + "%");
    }

    public static Specification<Property> hasMinGuests(Integer numGuests) {
        if (numGuests == null) return null;
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("maxGuests"), numGuests);
    }

    public static Specification<Property> priceBetween(Long min, Long max) {
        if (min == null && max == null) return null;
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (min != null) predicates.add(cb.greaterThanOrEqualTo(root.get("basePricePerNight"), min));
            if (max != null) predicates.add(cb.lessThanOrEqualTo(root.get("basePricePerNight"), max));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Property> hasPropertyType(PropertyType type) {
        if (type == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("propertyType"), type);
    }

    public static Specification<Property> hasAmenities(Set<UUID> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) return null;
        return (root, query, cb) -> {
            // For each amenity ID, property must have it in its amenities set
            Join<Object, Object> amenitiesJoin = root.join("amenities");
            return amenitiesJoin.get("id").in(amenityIds);
        };
    }

    public static Specification<Property> isAvailableForDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return null;
        return (root, query, cb) -> {
            // Property must NOT have a conflicting availability block (BLOCKED or BOOKED)
            Subquery<Long> blockedSubquery = query.subquery(Long.class);
            Root<PropertyAvailability> availRoot = blockedSubquery.from(PropertyAvailability.class);
            blockedSubquery.select(cb.count(availRoot))
                    .where(
                            cb.equal(availRoot.get("property"), root),
                            cb.lessThanOrEqualTo(availRoot.get("startDate"), checkOut),
                            cb.greaterThanOrEqualTo(availRoot.get("endDate"), checkIn)
                    );
            return cb.equal(blockedSubquery, 0L);
        };
    }

    /**
     * Combine all non-null specs with AND.
     */
    public static Specification<Property> buildSearchSpec(
            String city, Integer numGuests, Long minPrice, Long maxPrice,
            PropertyType type, Set<UUID> amenityIds, LocalDate checkIn, LocalDate checkOut) {

        Specification<Property> spec = Specification.where(isActive());
        spec = addIfNotNull(spec, inCity(city));
        spec = addIfNotNull(spec, hasMinGuests(numGuests));
        spec = addIfNotNull(spec, priceBetween(minPrice, maxPrice));
        spec = addIfNotNull(spec, hasPropertyType(type));
        spec = addIfNotNull(spec, hasAmenities(amenityIds));
        spec = addIfNotNull(spec, isAvailableForDates(checkIn, checkOut));
        return spec;
    }

    private static Specification<Property> addIfNotNull(Specification<Property> base, Specification<Property> toAdd) {
        return toAdd != null ? base.and(toAdd) : base;
    }
}
