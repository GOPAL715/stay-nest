-- liquibase formatted sql

-- changeset staynest:V006-create-messages
CREATE TABLE messages (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id  UUID        NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    sender_id   UUID        NOT NULL REFERENCES users(id)    ON DELETE RESTRICT,
    content     TEXT        NOT NULL,
    is_read     BOOLEAN     NOT NULL DEFAULT FALSE,
    read_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE INDEX idx_messages_booking_id ON messages(booking_id);
CREATE INDEX idx_messages_sender_id  ON messages(sender_id);

-- rollback DROP TABLE messages;
