package com.staynest.api.service;

import com.staynest.api.dto.response.PriceCalculationResult;
import com.staynest.api.entity.Property;

import java.time.LocalDate;

public interface PriceCalculationService {

    PriceCalculationResult calculate(Property property, LocalDate checkIn, LocalDate checkOut);
}
