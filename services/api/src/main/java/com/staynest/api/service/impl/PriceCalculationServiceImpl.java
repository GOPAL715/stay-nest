package com.staynest.api.service.impl;

import com.staynest.api.dto.response.PriceCalculationResult;
import com.staynest.api.entity.Property;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.service.PlatformConfigService;
import com.staynest.api.service.PriceCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PriceCalculationServiceImpl implements PriceCalculationService {

    private static final int DEFAULT_PLATFORM_FEE_PERCENT = 10;
    private static final int DEFAULT_TAX_RATE_PERCENT = 18;

    private final PlatformConfigService platformConfigService;

    @Override
    public PriceCalculationResult calculate(Property property, LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BusinessRuleException("Check-out date must be after check-in date");
        }

        int platformFeePercent = platformConfigService.getIntValue("service_fee_percent", DEFAULT_PLATFORM_FEE_PERCENT);
        int taxRatePercent = platformConfigService.getIntValue("tax_rate_default", DEFAULT_TAX_RATE_PERCENT);

        long numNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        long nightlyRate = property.getBasePricePerNight();
        long subtotal = nightlyRate * numNights;
        long cleaningFee = property.getCleaningFee();
        long platformFee = (long) (subtotal * platformFeePercent / 100.0);
        long taxableAmount = subtotal + cleaningFee + platformFee;
        long taxes = (long) (taxableAmount * taxRatePercent / 100.0);
        long totalAmount = taxableAmount + taxes;

        return PriceCalculationResult.builder()
                .numNights((int) numNights)
                .nightlyRate(nightlyRate)
                .subtotal(subtotal)
                .cleaningFee(cleaningFee)
                .platformFee(platformFee)
                .taxes(taxes)
                .totalAmount(totalAmount)
                .platformFeePercent(platformFeePercent)
                .taxRatePercent(taxRatePercent)
                .build();
    }
}
