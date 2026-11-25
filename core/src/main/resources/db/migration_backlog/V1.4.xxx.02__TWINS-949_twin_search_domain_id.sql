-- TWINS-949: multi-tenant isolation column on twin_search (security only, never exposed to the client).
-- Nullable: existing/system searches stay global (NULL = cross-domain); backfill of real domain ids
-- is owned by domain-level migrations, not by the engine. ON DELETE CASCADE mirrors
-- twin_search_alias.domain_id: a search dies together with its domain.

ALTER TABLE twin_search ADD COLUMN IF NOT EXISTS domain_id uuid;

-- domain_id FK -> domain(id)
ALTER TABLE twin_search DROP CONSTRAINT IF EXISTS twin_search_domain_id_fk;
ALTER TABLE twin_search
    ADD CONSTRAINT twin_search_domain_id_fk
        FOREIGN KEY (domain_id) REFERENCES domain (id)
        ON UPDATE CASCADE ON DELETE CASCADE;

-- Convention: index every FK column
CREATE INDEX IF NOT EXISTS idx_twin_search_domain_id ON twin_search (domain_id);
