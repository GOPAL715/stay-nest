-- liquibase formatted sql

-- changeset staynest:V010-create-host-applications
CREATE TABLE host_applications (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    applicant_id  UUID         NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    status        VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    motivation    TEXT,
    reviewed_by   UUID         REFERENCES users(id),
    review_notes  TEXT,
    reviewed_at   TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255)
);

CREATE INDEX idx_host_applications_applicant_id ON host_applications(applicant_id);
CREATE INDEX idx_host_applications_status       ON host_applications(status);

-- rollback DROP TABLE host_applications;
