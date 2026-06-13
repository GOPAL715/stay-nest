-- liquibase formatted sql

-- changeset staynest:V003-create-amenities
CREATE TABLE amenities (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL UNIQUE,
    icon       VARCHAR(100),
    category   VARCHAR(100),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- changeset staynest:V003-create-property-amenities
CREATE TABLE property_amenities (
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    amenity_id  UUID NOT NULL REFERENCES amenities(id)  ON DELETE CASCADE,
    PRIMARY KEY (property_id, amenity_id)
);

CREATE INDEX idx_property_amenities_property_id ON property_amenities(property_id);
CREATE INDEX idx_property_amenities_amenity_id  ON property_amenities(amenity_id);

-- rollback DROP TABLE property_amenities; DROP TABLE amenities;
