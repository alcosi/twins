CREATE TABLE IF NOT EXISTS domain_config_audit
(
    id               uuid PRIMARY KEY,
    domain_id        uuid REFERENCES domain ON UPDATE CASCADE ON DELETE CASCADE,
    "table"          varchar         NOT NULL,
    row_id           uuid            NOT NULL,
    operation        varchar         NOT NULL,
    snapshot         jsonb           NOT NULL,
    changed_at       timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_user_id    uuid     REFERENCES "user" ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS domain_config_audit_domain_id_index
    ON domain_config_audit (domain_id);

CREATE INDEX IF NOT EXISTS domain_config_audit_actor_user_id_index
    ON domain_config_audit (actor_user_id);
