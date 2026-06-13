package com.staynest.api.factory;

import com.staynest.api.dto.request.CreateBookingRequest;
import com.staynest.api.dto.response.PriceCalculationResult;
import com.staynest.api.entity.Booking;
import com.staynest.api.entity.Property;
import com.staynest.api.entity.User;
import com.staynest.api.enums.BookingStatus;
import org.springframework.stereotype.Component;

@Component
public class BookingFactory {

    public Booking createBooking(CreateBookingRequest request, Property property,
                                  User guest, User host,
                                  PriceCalculationResult pricing) {

        return Booking.builder()
                .property(property)
                .guest(guest)
                .host(host)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numGuests(request.getNumGuests())
                .numNights(pricing.getNumNights())
                .nightlyRate(pricing.getNightlyRate())
                .cleaningFee(pricing.getCleaningFee())
                .platformFee(pricing.getPlatformFee())
                .taxes(pricing.getTaxes())
                .totalAmount(pricing.getTotalAmount())
                .status(BookingStatus.PENDING)
                .cancellationPolicy(property.getCancellationPolicy())
                .specialRequests(request.getSpecialRequests())
                .build();
    }
}
