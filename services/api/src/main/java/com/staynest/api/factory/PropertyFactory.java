package com.staynest.api.factory;

import com.staynest.api.dto.request.CreatePropertyRequest;
import com.staynest.api.entity.Property;
import com.staynest.api.entity.User;
import com.staynest.api.enums.PropertyStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PropertyFactory {

    public Property createDraft(CreatePropertyRequest request, User host) {
        return Property.builder()
                .host(host)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .propertyType(request.getPropertyType())
                .addressLine1(request.getAddressLine1().trim())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .country(request.getCountry().trim())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .maxGuests(request.getMaxGuests())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .beds(request.getBeds())
                .basePricePerNight(request.getBasePricePerNight())
                .cleaningFee(request.getCleaningFee() != null ? request.getCleaningFee() : 0L)
                .serviceFeePercent(BigDecimal.TEN) // default from platform config
                .bookingMode(request.getBookingMode())
                .cancellationPolicy(request.getCancellationPolicy())
                .status(PropertyStatus.DRAFT)
                .build();
    }
}
