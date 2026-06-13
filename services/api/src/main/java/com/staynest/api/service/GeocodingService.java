package com.staynest.api.service;

import com.staynest.api.dto.GeocodeAddress;
import com.staynest.api.dto.GeoCoordinates;

import java.util.Optional;

public interface GeocodingService {

    Optional<GeoCoordinates> geocode(GeocodeAddress address);
}
