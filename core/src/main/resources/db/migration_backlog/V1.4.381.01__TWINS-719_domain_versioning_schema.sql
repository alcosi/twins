-- TWINS-719: domain configuration versioning, model v2 (pilot).
-- Design: ai/plans/domain-versioning-v2.md, implementation plan: ai/plans/domain-versioning-slice-a.md
-- Step 1/4: schema only — no functions, no backfill, no triggers.

-- ============================================================================
-- 1. domain_version — one row per version window / release / import
-- ============================================================================
CREATE TABLE IF NOT EXISTS domain_version
(
    id                         uuid PRIMARY KEY,          -- uuid_generate_v7_custom()
    domain_id                  uuid NOT NULL
        CONSTRAINT domain_version_domain_id_fk
            REFERENCES domain
            ON UPDATE CASCADE ON DELETE CASCADE
            DEFERRABLE INITIALLY DEFERRED,               -- domain birth: version inserted before domain row
    version                    varchar NOT NULL,          -- OPEN window: 'draft'; release: final label; import: '(import) 1.2'
    name                       varchar,
    description                varchar,
    status                     varchar NOT NULL DEFAULT 'OPEN'
        CONSTRAINT domain_version_status_ck CHECK (status IN ('OPEN', 'RELEASED')),
    "order"                    bigint,                    -- monotonic per domain counter, assigned at release/import;
                                                           -- NULL for the open window; used for sorting/chain walk
    json_file                  jsonb,                     -- full snapshot of the configuration state
    hash                       varchar,                   -- SHA-256 of the full snapshot (always set for RELEASED)
    delta_file                 jsonb,                     -- forward delta: prev release -> this release (cross-env import)
    delta_hash                 varchar,
    rollback_delta_file        jsonb,                     -- reverse delta: this release -> prev release (local restore)
    rollback_delta_hash        varchar,
    previous_domain_version_id uuid
        CONSTRAINT domain_version_prev_version_fk
            REFERENCES domain_version
            ON UPDATE CASCADE ON DELETE SET NULL,
    started_at                 timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_duration             bigint,
    released_at                timestamp,
    release_duration           bigint
);

-- label occupancy per domain (checked on UPDATE as well, see release_end)
CREATE UNIQUE INDEX IF NOT EXISTS domain_version_unique
    ON domain_version (domain_id, version);

-- at most one open draft window per domain
CREATE UNIQUE INDEX IF NOT EXISTS domain_version_one_open_per_domain
    ON domain_version (domain_id)
    WHERE status = 'OPEN';

CREATE INDEX IF NOT EXISTS domain_version_prev_idx
    ON domain_version (previous_domain_version_id);

CREATE INDEX IF NOT EXISTS domain_version_order_idx
    ON domain_version (domain_id, "order" DESC);

-- latest release lookups (import base chain walk)
CREATE INDEX IF NOT EXISTS domain_version_released_idx
    ON domain_version (domain_id, released_at DESC);

-- ============================================================================
-- 2. domain_version_audit — full change history, written by all paths
-- ============================================================================
CREATE TABLE IF NOT EXISTS domain_version_audit
(
    id                      uuid PRIMARY KEY,
    domain_id               uuid NOT NULL
        CONSTRAINT domain_version_audit_domain_id_fk
            REFERENCES domain
            ON UPDATE CASCADE ON DELETE CASCADE,
    domain_version_id       uuid NOT NULL,           -- window stamp; intentionally no FK: windows are collapsed/deleted
    seq                     bigserial NOT NULL,      -- strict insertion order: uuid v7 has only
    -- millisecond precision, records within one
    -- millisecond (bulk writes!) order randomly
    table_name              varchar NOT NULL,
    row_id                  uuid NOT NULL,           -- PK of the changed row (every versioned table has a uuid id)
    operation               varchar NOT NULL
        CONSTRAINT domain_version_audit_operation_ck
            CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE', 'RESTORE')),
    domain_operation_status varchar NOT NULL,        -- IDLE / IMPORTING / RELEASING / ROLLBACK: stage of the write
    before                  jsonb,                   -- to_jsonb(OLD); NULL only for INSERT
    after                   jsonb,                   -- to_jsonb(NEW); NULL for a physical DELETE
    changed_at              timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_user_id           uuid
        CONSTRAINT domain_version_audit_actor_user_id_fk
            REFERENCES "user"
            ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS domain_version_audit_window_idx
    ON domain_version_audit (domain_id, domain_version_id);

CREATE INDEX IF NOT EXISTS domain_version_audit_row_idx
    ON domain_version_audit (table_name, row_id);

CREATE INDEX IF NOT EXISTS domain_version_audit_actor_idx
    ON domain_version_audit (actor_user_id);

-- ============================================================================
-- 3. domain — operation state, lock, pointer to the current version
-- ============================================================================
ALTER TABLE domain ADD COLUMN IF NOT EXISTS current_domain_version_id uuid
    CONSTRAINT domain_current_domain_version_fk
        REFERENCES domain_version (id)
        ON UPDATE CASCADE ON DELETE RESTRICT;
-- NOT NULL is set by 03_backfill after every existing domain gets its V1; new domains
-- get it from the before-insert trigger (02_functions + 04_triggers).

ALTER TABLE domain ADD COLUMN IF NOT EXISTS operation_status varchar NOT NULL DEFAULT 'IDLE'
    CONSTRAINT domain_operation_status_ck CHECK (operation_status IN ('IDLE', 'IMPORTING', 'RELEASING', 'ROLLBACK'));

ALTER TABLE domain ADD COLUMN IF NOT EXISTS operation_started_at timestamp; -- stuck operation diagnostics

ALTER TABLE domain ADD COLUMN IF NOT EXISTS locked boolean NOT NULL DEFAULT false;

ALTER TABLE domain ADD COLUMN IF NOT EXISTS locked_at timestamp;

ALTER TABLE domain ADD COLUMN IF NOT EXISTS locked_by uuid
    CONSTRAINT domain_locked_by_fk
        REFERENCES "user"
        ON UPDATE CASCADE ON DELETE SET NULL;

-- ============================================================================
-- 4. Versioning columns on the pilot configuration tables
-- ============================================================================
-- domain_id for child tables first: the composite index below needs it
-- (backfilled from the parent row by 03_backfill, FK added there too)
ALTER TABLE twin_class_field ADD COLUMN IF NOT EXISTS domain_id uuid;
ALTER TABLE twin_status ADD COLUMN IF NOT EXISTS domain_id uuid;       -- parent column: twin_class_id
ALTER TABLE data_list_option ADD COLUMN IF NOT EXISTS domain_id uuid;
ALTER TABLE data_list_subset ADD COLUMN IF NOT EXISTS domain_id uuid;
ALTER TABLE data_list_subset_option ADD COLUMN IF NOT EXISTS domain_id uuid;

-- versioning contract: every versioned table carries a uuid PK. data_list_subset_option is the
-- only pilot table with a composite business key and no id -> add a surrogate PK, the composite
-- unique stays as the business key (rebuilt partial below)
ALTER TABLE data_list_subset_option ADD COLUMN IF NOT EXISTS id uuid;
-- deterministic id (uuid v5 of the business key pair): independently migrated environments
-- must converge to the SAME id for the same setting, otherwise snapshots/deltas referenced
-- by id become mutually incompatible between environments
UPDATE data_list_subset_option
   SET id = uuid_generate_v5('71900000-0000-0000-0000-000000000001'::uuid,
                             data_list_subset_id::text || ':' || data_list_option_id::text)
 WHERE id IS NULL;
ALTER TABLE data_list_subset_option ALTER COLUMN id SET NOT NULL;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint
                    WHERE conrelid = 'data_list_subset_option'::regclass AND contype = 'p') THEN
        ALTER TABLE data_list_subset_option ADD CONSTRAINT data_list_subset_option_pk PRIMARY KEY (id);
    END IF;
END $$;

CREATE OR REPLACE FUNCTION add_domain_version_columns(tbl text) RETURNS void AS
$$
BEGIN
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS domain_version_id uuid', tbl);
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS row_status varchar NOT NULL DEFAULT ''ACTIVE''
                        CONSTRAINT %I CHECK (row_status IN (''ACTIVE'', ''DISABLED'', ''DELETED''))',
                   tbl, tbl || '_row_status_ck');
    EXECUTE format('CREATE INDEX IF NOT EXISTS %I ON %I (domain_id, domain_version_id)',
                   tbl || '_domain_version_idx', tbl);
    -- FK is NOT VALID here, VALIDATEd by 03_backfill after stamping; existence check keeps the
    -- script re-runnable (the IT re-applies it per test)
    IF NOT EXISTS (SELECT 1 FROM pg_constraint
                    WHERE conrelid = tbl::regclass AND conname = tbl || '_domain_version_id_fk') THEN
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (domain_version_id)
                            REFERENCES domain_version (id)
                            ON UPDATE CASCADE ON DELETE RESTRICT NOT VALID',
                       tbl, tbl || '_domain_version_id_fk');
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT add_domain_version_columns('twin_class');
SELECT add_domain_version_columns('twin_class_field');
SELECT add_domain_version_columns('twin_status');
SELECT add_domain_version_columns('data_list');
SELECT add_domain_version_columns('data_list_option');
SELECT add_domain_version_columns('data_list_subset');
SELECT add_domain_version_columns('data_list_subset_option');

DROP FUNCTION IF EXISTS add_domain_version_columns(text);

-- ============================================================================
-- 5. Business unique indexes -> partial (a DELETED row must not block a re-create)
--    Inventory of the pilot tables: twin_class, twin_class_field, data_list,
--    data_list_subset, data_list_subset_option have unique business keys;
--    twin_status (key is not unique per class), data_list_option (no unique key) — nothing to do.
-- ============================================================================
DROP INDEX IF EXISTS twin_class_domain_id_key_uindex;
CREATE UNIQUE INDEX twin_class_domain_id_key_uindex
    ON twin_class (domain_id, key)
    WHERE row_status <> 'DELETED';

DROP INDEX IF EXISTS twin_class_field_twin_class_id_key_uindex;
CREATE UNIQUE INDEX twin_class_field_twin_class_id_key_uindex
    ON twin_class_field (twin_class_id, key)
    WHERE row_status <> 'DELETED';

DROP INDEX IF EXISTS data_list_domain_id_key_uindex;
CREATE UNIQUE INDEX data_list_domain_id_key_uindex
    ON data_list (domain_id, key)
    WHERE row_status <> 'DELETED';

DROP INDEX IF EXISTS ux_data_list_subset_data_list_id_key;
CREATE UNIQUE INDEX ux_data_list_subset_data_list_id_key
    ON data_list_subset (data_list_id, key)
    WHERE row_status <> 'DELETED';

ALTER TABLE data_list_subset_option DROP CONSTRAINT IF EXISTS data_list_subset_option_uq;
CREATE UNIQUE INDEX IF NOT EXISTS data_list_subset_option_uq_idx
    ON data_list_subset_option (data_list_subset_id, data_list_option_id)
    WHERE row_status <> 'DELETED';
