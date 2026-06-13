package com.staynest.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.geocoding")
public class GeocodingProperties {

    private boolean enabled = true;
    private Nominatim nominatim = new Nominatim();

    @Getter
    @Setter
    public static class Nominatim {
        private String baseUrl = "https://nominatim.openstreetmap.org";
        private String userAgent = "StayNest/1.0";
        private String email = "dev@staynest.local";
    }
}
