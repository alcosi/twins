-- TWINS-875: audit / security columns on twin_pointer.
--   domain_id          — multi-tenant isolation (security only, never exposed to the client)
--   created_by_user_id — audit; FK to "user"
--   created_at         — audit timestamp
-- All nullable so existing rows survive backfill. New rows are populated from the api-user
-- context in TwinPointerService.createTwinPointers (mirrors FactoryConditionSetService).
-- Style mirrors V1.3.144.01 (twin_factory_condition_set domain_id) and the notification_schema
-- created_by_user_id migration.

ALTER TABLE twin_pointer ADD COLUMN IF NOT EXISTS created_at timestamp;
ALTER TABLE twin_pointer ADD COLUMN IF NOT EXISTS created_by_user_id uuid;
ALTER TABLE twin_pointer ADD COLUMN IF NOT EXISTS domain_id uuid;

-- created_by_user_id FK -> "user"(id)
ALTER TABLE twin_pointer DROP CONSTRAINT IF EXISTS twin_pointer_created_by_user_id_fk;
ALTER TABLE twin_pointer
    ADD CONSTRAINT twin_pointer_created_by_user_id_fk
        FOREIGN KEY (created_by_user_id) REFERENCES "user" (id)
        ON UPDATE CASCADE ON DELETE SET NULL;

-- domain_id FK -> domain(id)
ALTER TABLE twin_pointer DROP CONSTRAINT IF EXISTS twin_pointer_domain_id_fk;
ALTER TABLE twin_pointer
    ADD CONSTRAINT twin_pointer_domain_id_fk
        FOREIGN KEY (domain_id) REFERENCES domain (id)
        ON UPDATE CASCADE;

-- Indexes for FK columns and the sortable audit timestamp (convention: index every FK column)
CREATE INDEX IF NOT EXISTS idx_twin_pointer_created_by_user_id ON twin_pointer (created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_twin_pointer_domain_id ON twin_pointer (domain_id);
CREATE INDEX IF NOT EXISTS idx_twin_pointer_created_at ON twin_pointer (created_at);
