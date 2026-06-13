package com.staynest.api.entity;

import com.staynest.api.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "properties")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Property extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false, length = 50)
    private PropertyType propertyType;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "max_guests", nullable = false)
    private int maxGuests;

    @Column(name = "bedrooms", nullable = false)
    private int bedrooms;

    @Column(name = "bathrooms", nullable = false, precision = 3, scale = 1)
    private BigDecimal bathrooms;

    @Column(name = "beds", nullable = false)
    private int beds;

    // All monetary values stored in paise (1/100 of INR)
    @Column(name = "base_price_per_night", nullable = false)
    private long basePricePerNight;

    @Column(name = "cleaning_fee", nullable = false)
    private long cleaningFee;

    @Column(name = "service_fee_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal serviceFeePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_mode", nullable = false, length = 50)
    private BookingMode bookingMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", nullable = false, length = 50)
    private CancellationPolicy cancellationPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PropertyStatus status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<PropertyPhoto> photos = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "property_amenities",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PropertyAvailability> availabilityBlocks = new ArrayList<>();

    // --- Mutators ---

    public void updateDetails(String title, String description, PropertyType propertyType,
                               String addressLine1, String addressLine2, String city,
                               String state, String country, String postalCode,
                               BigDecimal latitude, BigDecimal longitude,
                               int maxGuests, int bedrooms, BigDecimal bathrooms, int beds,
                               long basePricePerNight, long cleaningFee,
                               BookingMode bookingMode, CancellationPolicy cancellationPolicy) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (propertyType != null) this.propertyType = propertyType;
        if (addressLine1 != null) this.addressLine1 = addressLine1;
        if (addressLine2 != null) this.addressLine2 = addressLine2;
        if (city != null) this.city = city;
        if (state != null) this.state = state;
        if (country != null) this.country = country;
        if (postalCode != null) this.postalCode = postalCode;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        if (maxGuests > 0) this.maxGuests = maxGuests;
        if (bedrooms > 0) this.bedrooms = bedrooms;
        if (bathrooms != null) this.bathrooms = bathrooms;
        if (beds > 0) this.beds = beds;
        if (basePricePerNight > 0) this.basePricePerNight = basePricePerNight;
        if (cleaningFee >= 0) this.cleaningFee = cleaningFee;
        if (bookingMode != null) this.bookingMode = bookingMode;
        if (cancellationPolicy != null) this.cancellationPolicy = cancellationPolicy;
    }

    public void setCoordinates(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void submitForReview() {
        this.status = PropertyStatus.PENDING;
    }

    public void approve() {
        this.status = PropertyStatus.ACTIVE;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        this.status = PropertyStatus.DRAFT;
        this.rejectionReason = reason;
    }

    public void suspend(String reason) {
        this.status = PropertyStatus.SUSPENDED;
        this.rejectionReason = reason;
    }

    public void setAmenities(Set<Amenity> amenities) {
        this.amenities = amenities;
    }
}
