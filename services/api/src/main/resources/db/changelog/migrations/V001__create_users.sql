-- liquibase formatted sql

-- changeset staynest:V001-create-users
CREATE TABLE users (
    id                       UUID                     NOT NULL DEFAULT gen_random_uuid(),
    email                    VARCHAR(255)             NOT NULL,
    password_hash            VARCHAR(255)             NOT NULL,
    first_name               VARCHAR(100)             NOT NULL,
    last_name                VARCHAR(100)             NOT NULL,
    phone                    VARCHAR(20),
    profile_picture_url      VARCHAR(500),
    role                     VARCHAR(50)              NOT NULL,
    status                   VARCHAR(50)              NOT NULL,
    email_verified           BOOLEAN                  NOT NULL DEFAULT false,
    verification_token       VARCHAR(500),
    verification_token_expiry TIMESTAMP WITH TIME ZONE,
    reset_token              VARCHAR(500),
    reset_token_expiry       TIMESTAMP WITH TIME ZONE,
    failed_login_attempts    INT                      NOT NULL DEFAULT 0,
    lockout_until            TIMESTAMP WITH TIME ZONE,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_by               VARCHAR(255),
    updated_by               VARCHAR(255),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role   CHECK (role   IN ('SUPER_ADMIN', 'PROPERTY_MANAGER', 'HOST', 'GUEST', 'SUPPORT_AGENT')),
    CONSTRAINT chk_users_status CHECK (status IN ('UNVERIFIED', 'ACTIVE', 'INACTIVE', 'DELETED'))
);

CREATE INDEX idx_users_email               ON users (email);
CREATE INDEX idx_users_verification_token  ON users (verification_token);
CREATE INDEX idx_users_reset_token         ON users (reset_token);
-- rollback DROP TABLE users;

-- changeset staynest:V001-create-refresh-tokens
CREATE TABLE refresh_tokens (
    id          UUID                     NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID                     NOT NULL,
    token       VARCHAR(500)             NOT NULL,
    expiry      TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked     BOOLEAN                  NOT NULL DEFAULT false,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),

    CONSTRAINT pk_refresh_tokens       PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
-- rollback DROP TABLE refresh_tokens;
