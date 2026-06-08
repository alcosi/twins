CREATE TABLE IF NOT EXISTS twin_class_uniqueness
(
    id            UUID PRIMARY KEY,
    twin_class_id UUID      NOT NULL REFERENCES twin_class (id) ON DELETE CASCADE,
    key           VARCHAR(255) NULL,
    name          VARCHAR(255) NULL,
    inheritable   BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_twin_class_uniqueness_class_enabled ON twin_class_uniqueness (twin_class_id, key);

CREATE TABLE IF NOT EXISTS twin_class_uniqueness_field
(
    id                       UUID PRIMARY KEY,
    twin_class_uniqueness_id UUID NOT NULL REFERENCES twin_class_uniqueness (id) ON DELETE CASCADE,
    twin_class_field_id      UUID NOT NULL REFERENCES twin_class_field (id) ON DELETE CASCADE,
    UNIQUE (twin_class_uniqueness_id, twin_class_field_id)
);

CREATE INDEX IF NOT EXISTS idx_twin_class_uniqueness_field_uniqueness_id
    ON twin_class_uniqueness_field (twin_class_uniqueness_id);

CREATE INDEX IF NOT EXISTS idx_twin_class_uniqueness_field_field_id
    ON twin_class_uniqueness_field (twin_class_field_id);