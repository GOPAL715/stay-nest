-- liquibase formatted sql

-- changeset staynest:V009-create-platform-config
CREATE TABLE platform_config (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key          VARCHAR(100) NOT NULL UNIQUE,
    config_value        VARCHAR(500) NOT NULL,
    description         TEXT,
    updated_by_admin_id UUID         REFERENCES users(id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

CREATE INDEX idx_platform_config_key ON platform_config(config_key);

-- rollback DROP TABLE platform_config;
