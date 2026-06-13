-- liquibase formatted sql

-- changeset staynest:V007-create-notifications
CREATE TABLE notifications (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title          VARCHAR(255) NOT NULL,
    body           TEXT         NOT NULL,
    type           VARCHAR(100) NOT NULL,
    reference_id   UUID,
    reference_type VARCHAR(100),
    is_read        BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at        TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255)
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(user_id, is_read);

-- rollback DROP TABLE notifications;
