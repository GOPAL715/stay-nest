-- liquibase formatted sql

-- changeset staynest:V011-create-audit-logs
CREATE TABLE audit_logs (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id     UUID         REFERENCES users(id) ON DELETE SET NULL,
    action       VARCHAR(100) NOT NULL,
    entity_type  VARCHAR(100) NOT NULL,
    entity_id    UUID,
    before_state JSONB,
    after_state  JSONB,
    ip_address   VARCHAR(50),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_actor_id    ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_entity      ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at  ON audit_logs(created_at DESC);

-- rollback DROP TABLE audit_logs;
