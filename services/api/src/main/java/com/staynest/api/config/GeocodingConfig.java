package com.staynest.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeocodingConfig {

    @Bean
    RestClient nominatimRestClient(GeocodingProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getNominatim().getBaseUrl())
                .defaultHeader("User-Agent", buildUserAgent(properties))
                .defaultHeader("Accept", "application/json")
                .build();
    }

    private static String buildUserAgent(GeocodingProperties properties) {
        GeocodingProperties.Nominatim nominatim = properties.getNominatim();
        return nominatim.getUserAgent() + " (contact: " + nominatim.getEmail() + ")";
    }
}
