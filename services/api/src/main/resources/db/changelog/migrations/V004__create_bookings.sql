-- liquibase formatted sql

-- changeset staynest:V004-create-bookings
CREATE TABLE bookings (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id         UUID         NOT NULL REFERENCES properties(id) ON DELETE RESTRICT,
    guest_id            UUID         NOT NULL REFERENCES users(id)      ON DELETE RESTRICT,
    host_id             UUID         NOT NULL REFERENCES users(id)      ON DELETE RESTRICT,
    check_in_date       DATE         NOT NULL,
    check_out_date      DATE         NOT NULL,
    num_guests          INT          NOT NULL,
    num_nights          INT          NOT NULL,
    nightly_rate        BIGINT       NOT NULL,
    cleaning_fee        BIGINT       NOT NULL DEFAULT 0,
    platform_fee        BIGINT       NOT NULL DEFAULT 0,
    taxes               BIGINT       NOT NULL DEFAULT 0,
    total_amount        BIGINT       NOT NULL,
    status              VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    cancellation_policy VARCHAR(50)  NOT NULL,
    cancellation_reason TEXT,
    cancelled_by        UUID         REFERENCES users(id),
    cancelled_at        TIMESTAMPTZ,
    special_requests    TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

CREATE INDEX idx_bookings_property_id          ON bookings(property_id);
CREATE INDEX idx_bookings_guest_id             ON bookings(guest_id);
CREATE INDEX idx_bookings_host_id              ON bookings(host_id);
CREATE INDEX idx_bookings_status               ON bookings(status);
CREATE INDEX idx_bookings_dates                ON bookings(property_id, check_in_date, check_out_date);

-- rollback DROP TABLE bookings;
