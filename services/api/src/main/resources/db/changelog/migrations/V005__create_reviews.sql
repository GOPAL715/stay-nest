-- liquibase formatted sql

-- changeset staynest:V005-create-reviews
CREATE TABLE reviews (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id            UUID         NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE RESTRICT,
    property_id           UUID         NOT NULL REFERENCES properties(id)      ON DELETE RESTRICT,
    reviewer_id           UUID         NOT NULL REFERENCES users(id)           ON DELETE RESTRICT,
    reviewee_id           UUID         NOT NULL REFERENCES users(id)           ON DELETE RESTRICT,
    reviewer_type         VARCHAR(10)  NOT NULL,
    overall_rating        INT          NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    cleanliness_rating    INT          CHECK (cleanliness_rating BETWEEN 1 AND 5),
    accuracy_rating       INT          CHECK (accuracy_rating BETWEEN 1 AND 5),
    checkin_rating        INT          CHECK (checkin_rating BETWEEN 1 AND 5),
    communication_rating  INT          CHECK (communication_rating BETWEEN 1 AND 5),
    location_rating       INT          CHECK (location_rating BETWEEN 1 AND 5),
    value_rating          INT          CHECK (value_rating BETWEEN 1 AND 5),
    comment               TEXT,
    host_response         TEXT,
    host_response_at      TIMESTAMPTZ,
    is_published          BOOLEAN      NOT NULL DEFAULT FALSE,
    submitted_at          TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255)
);

CREATE INDEX idx_reviews_property_id  ON reviews(property_id);
CREATE INDEX idx_reviews_reviewer_id  ON reviews(reviewer_id);
CREATE INDEX idx_reviews_booking_id   ON reviews(booking_id);

-- rollback DROP TABLE reviews;
