package com.staynest.api.dto;

public record GeocodeAddress(
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode
) {
    public static GeocodeAddress of(
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String country,
            String postalCode
    ) {
        return new GeocodeAddress(
                trimToNull(addressLine1),
                trimToNull(addressLine2),
                trimToNull(city),
                trimToNull(state),
                trimToNull(country),
                trimToNull(postalCode)
        );
    }

    public boolean isComplete() {
        return addressLine1 != null && city != null && state != null && country != null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
