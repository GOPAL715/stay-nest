-- liquibase formatted sql

-- changeset staynest:V008-create-payments
CREATE TABLE payments (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id          UUID         NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE RESTRICT,
    razorpay_order_id   VARCHAR(100),
    razorpay_payment_id VARCHAR(100),
    razorpay_signature  VARCHAR(500),
    amount              BIGINT       NOT NULL,
    currency            VARCHAR(10)  NOT NULL DEFAULT 'INR',
    status              VARCHAR(50)  NOT NULL DEFAULT 'CREATED',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

CREATE INDEX idx_payments_booking_id        ON payments(booking_id);
CREATE INDEX idx_payments_razorpay_order_id ON payments(razorpay_order_id);

-- changeset staynest:V008-create-refunds
CREATE TABLE refunds (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id        UUID         NOT NULL REFERENCES payments(id)  ON DELETE RESTRICT,
    booking_id        UUID         NOT NULL REFERENCES bookings(id)  ON DELETE RESTRICT,
    razorpay_refund_id VARCHAR(100),
    amount            BIGINT       NOT NULL,
    reason            TEXT,
    status            VARCHAR(50)  NOT NULL DEFAULT 'INITIATED',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255)
);

CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_booking_id ON refunds(booking_id);

-- rollback DROP TABLE refunds; DROP TABLE payments;
