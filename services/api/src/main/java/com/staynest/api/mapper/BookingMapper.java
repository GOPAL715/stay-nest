package com.staynest.api.mapper;

import com.staynest.api.dto.response.BookingResponse;
import com.staynest.api.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "propertyTitle", source = "property.title")
    @Mapping(target = "propertyCity", source = "property.city")
    @Mapping(target = "coverPhotoUrl", expression = "java(extractCoverPhoto(booking))")
    @Mapping(target = "hostId", source = "host.id")
    @Mapping(target = "hostFirstName", source = "host.firstName")
    @Mapping(target = "hostLastName", source = "host.lastName")
    @Mapping(target = "guestId", source = "guest.id")
    @Mapping(target = "guestFirstName", source = "guest.firstName")
    @Mapping(target = "guestLastName", source = "guest.lastName")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "priceBreakdown", expression = "java(buildPriceBreakdown(booking))")
    BookingResponse toBookingResponse(Booking booking);

    default String extractCoverPhoto(Booking booking) {
        if (booking.getProperty().getPhotos() == null || booking.getProperty().getPhotos().isEmpty()) {
            return null;
        }
        return booking.getProperty().getPhotos().stream()
                .filter(p -> p.isCover())
                .findFirst()
                .map(p -> p.getUrl())
                .orElse(booking.getProperty().getPhotos().get(0).getUrl());
    }

    default BookingResponse.PriceBreakdownResponse buildPriceBreakdown(Booking booking) {
        long subtotal = booking.getNightlyRate() * booking.getNumNights();
        return BookingResponse.PriceBreakdownResponse.builder()
                .nightlyRate(booking.getNightlyRate())
                .numNights(booking.getNumNights())
                .subtotal(subtotal)
                .cleaningFee(booking.getCleaningFee())
                .platformFee(booking.getPlatformFee())
                .taxes(booking.getTaxes())
                .totalAmount(booking.getTotalAmount())
                .nightlyRateInr(paisoToInr(booking.getNightlyRate()))
                .totalAmountInr(paisoToInr(booking.getTotalAmount()))
                .build();
    }

    default String paisoToInr(long paise) {
        return BigDecimal.valueOf(paise)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .toPlainString();
    }
}
