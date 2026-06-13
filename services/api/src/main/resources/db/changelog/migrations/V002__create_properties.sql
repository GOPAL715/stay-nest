-- liquibase formatted sql

-- changeset staynest:V002-create-properties
CREATE TABLE properties (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    host_id               UUID         NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    title                 VARCHAR(255) NOT NULL,
    description           TEXT         NOT NULL,
    property_type         VARCHAR(50)  NOT NULL,
    address_line1         VARCHAR(255) NOT NULL,
    address_line2         VARCHAR(255),
    city                  VARCHAR(100) NOT NULL,
    state                 VARCHAR(100) NOT NULL,
    country               VARCHAR(100) NOT NULL,
    postal_code           VARCHAR(20),
    latitude              DECIMAL(10, 7),
    longitude             DECIMAL(10, 7),
    max_guests            INT          NOT NULL,
    bedrooms              INT          NOT NULL,
    bathrooms             DECIMAL(3,1) NOT NULL,
    beds                  INT          NOT NULL,
    base_price_per_night  BIGINT       NOT NULL,
    cleaning_fee          BIGINT       NOT NULL DEFAULT 0,
    service_fee_percent   DECIMAL(5,2) NOT NULL DEFAULT 10.00,
    booking_mode          VARCHAR(50)  NOT NULL DEFAULT 'INSTANT_BOOK',
    cancellation_policy   VARCHAR(50)  NOT NULL DEFAULT 'MODERATE',
    status                VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    rejection_reason      TEXT,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255)
);

CREATE INDEX idx_properties_host_id ON properties(host_id);
CREATE INDEX idx_properties_status  ON properties(status);
CREATE INDEX idx_properties_city    ON properties(city);
CREATE INDEX idx_properties_type    ON properties(property_type);

-- changeset staynest:V002-create-property-photos
CREATE TABLE property_photos (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id   UUID         NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    url           TEXT         NOT NULL,
    caption       VARCHAR(255),
    display_order INT          NOT NULL DEFAULT 0,
    is_cover      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255)
);

CREATE INDEX idx_property_photos_property_id ON property_photos(property_id);

-- changeset staynest:V002-create-property-availability
CREATE TABLE property_availability (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID        NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    reason      VARCHAR(50) NOT NULL DEFAULT 'BLOCKED',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE INDEX idx_property_availability_property_id ON property_availability(property_id);
CREATE INDEX idx_property_availability_dates        ON property_availability(property_id, start_date, end_date);

-- rollback DROP TABLE property_availability; DROP TABLE property_photos; DROP TABLE properties;
