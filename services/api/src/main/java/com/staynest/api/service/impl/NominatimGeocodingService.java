package com.staynest.api.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.staynest.api.config.GeocodingProperties;
import com.staynest.api.dto.GeocodeAddress;
import com.staynest.api.dto.GeoCoordinates;
import com.staynest.api.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Free geocoding via OpenStreetMap Nominatim.
 * Usage policy: https://operations.osmfoundation.org/policies/nominatim/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NominatimGeocodingService implements GeocodingService {

    private static final Map<String, String> COUNTRY_CODES = Map.of(
            "india", "in",
            "united states", "us",
            "united kingdom", "gb"
    );

    private final RestClient nominatimRestClient;
    private final GeocodingProperties properties;

    @Override
    public Optional<GeoCoordinates> geocode(GeocodeAddress address) {
        if (!properties.isEnabled() || address == null || !address.isComplete()) {
            return Optional.empty();
        }

        try {
            String countryCode = resolveCountryCode(address.country());
            List<SearchAttempt> attempts = buildSearchAttempts(address, countryCode);

            for (SearchAttempt attempt : attempts) {
                Optional<GeoCoordinates> result = search(attempt.uriBuilder(), attempt.approximate());
                if (result.isPresent()) {
                    return result;
                }
            }

            return Optional.empty();
        } catch (Exception ex) {
            log.warn("Geocoding failed for {}, {}: {}", address.city(), address.country(), ex.getMessage());
            return Optional.empty();
        }
    }

    private List<SearchAttempt> buildSearchAttempts(GeocodeAddress address, String countryCode) {
        List<SearchAttempt> attempts = new ArrayList<>();

        String fullQuery = joinParts(
                address.addressLine1(),
                address.addressLine2(),
                address.city(),
                address.state(),
                address.country(),
                address.postalCode()
        );
        attempts.add(SearchAttempt.freeText(fullQuery, false, countryCode));

        if (address.addressLine1() != null) {
            attempts.add(SearchAttempt.structured(address, false, countryCode));
        }

        String cityQuery = joinParts(address.city(), address.state(), address.country());
        attempts.add(SearchAttempt.freeText(cityQuery, true, countryCode));

        String cityOnlyQuery = joinParts(address.city(), address.country());
        attempts.add(SearchAttempt.freeText(cityOnlyQuery, true, countryCode));

        return attempts;
    }

    private Optional<GeoCoordinates> search(
            Function<UriBuilder, URI> uriFn,
            boolean approximate
    ) {
        List<NominatimResult> results = nominatimRestClient.get()
                .uri(uriFn)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }

        NominatimResult first = results.getFirst();
        return Optional.of(new GeoCoordinates(
                new BigDecimal(first.lat()),
                new BigDecimal(first.lon()),
                first.displayName(),
                approximate
        ));
    }

    private static String joinParts(String... parts) {
        return Stream.of(parts)
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(", "));
    }

    private static String resolveCountryCode(String country) {
        if (country == null || country.isBlank()) {
            return null;
        }
        return COUNTRY_CODES.get(country.trim().toLowerCase(Locale.ROOT));
    }

    private record SearchAttempt(Function<UriBuilder, URI> uriBuilder, boolean approximate) {
        static SearchAttempt freeText(String query, boolean approximate, String countryCode) {
            return new SearchAttempt(
                    builder -> {
                        UriBuilder uriBuilder = builder
                                .path("/search")
                                .queryParam("q", query)
                                .queryParam("format", "json")
                                .queryParam("limit", "1");
                        applyCountryCode(uriBuilder, countryCode);
                        return uriBuilder.build();
                    },
                    approximate
            );
        }

        static SearchAttempt structured(GeocodeAddress address, boolean approximate, String countryCode) {
            return new SearchAttempt(
                    builder -> {
                        UriBuilder uriBuilder = builder
                                .path("/search")
                                .queryParam("street", address.addressLine1())
                                .queryParam("city", address.city())
                                .queryParam("state", address.state())
                                .queryParam("country", address.country())
                                .queryParam("format", "json")
                                .queryParam("limit", "1");
                        if (address.postalCode() != null && !address.postalCode().isBlank()) {
                            uriBuilder.queryParam("postalcode", address.postalCode());
                        }
                        applyCountryCode(uriBuilder, countryCode);
                        return uriBuilder.build();
                    },
                    approximate
            );
        }

        private static void applyCountryCode(UriBuilder uriBuilder, String countryCode) {
            if (countryCode != null) {
                uriBuilder.queryParam("countrycodes", countryCode);
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimResult(
            @JsonProperty("lat") String lat,
            @JsonProperty("lon") String lon,
            @JsonProperty("display_name") String displayName
    ) {
    }
}
