-- TWINS-830: Materialized relationship table domain_business_account_user
-- Stores a denormalized link between User, Domain, and BusinessAccount,
-- previously computed dynamically via a 3-table JOIN:
--   domain_user (user_id, domain_id)
--   + domain_business_account (business_account_id, domain_id)
--   + business_account_user (user_id, business_account_id)
--
-- A row exists only when ALL three relationships are present simultaneously:
--   1. user is registered in domain (domain_user)
--   2. business_account is registered in domain (domain_business_account)
--   3. user belongs to business_account (business_account_user)
--
-- PK is composite (user_id, domain_id, business_account_id) — the natural business key.
-- Direct INSERT/UPDATE is forbidden; data is managed exclusively by triggers.
-- Deletion is handled automatically via FK ON DELETE CASCADE on:
--   domain_user_id, domain_business_account_id, business_account_user_id

-- ============================================================
-- 1. Add last_activity_at column to user-related tables
-- ============================================================

ALTER TABLE domain_user
    ADD COLUMN IF NOT EXISTS last_activity_at timestamp without time zone;

ALTER TABLE business_account_user
    ADD COLUMN IF NOT EXISTS last_activity_at timestamp without time zone;

-- ============================================================
-- 2. Create domain_business_account_user table
-- ============================================================

CREATE TABLE IF NOT EXISTS domain_business_account_user (
    domain_user_id uuid NOT NULL,
    domain_business_account_id uuid NOT NULL,
    business_account_user_id uuid NOT NULL,
    user_id uuid NOT NULL,
    domain_id uuid NOT NULL,
    business_account_id uuid NOT NULL,
    last_activity_at timestamp without time zone,
    created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT domain_business_account_user_pk
        PRIMARY KEY (user_id, domain_id, business_account_id),

    CONSTRAINT domain_business_account_user_domain_user_id_fk
        FOREIGN KEY (domain_user_id) REFERENCES domain_user(id)
        ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT domain_business_account_user_domain_business_account_id_fk
        FOREIGN KEY (domain_business_account_id) REFERENCES domain_business_account(id)
        ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT domain_business_account_user_business_account_user_id_fk
        FOREIGN KEY (business_account_user_id) REFERENCES business_account_user(id)
        ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT domain_business_account_user_user_id_fk
        FOREIGN KEY (user_id) REFERENCES "user"(id)
        ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT domain_business_account_user_domain_id_fk
        FOREIGN KEY (domain_id) REFERENCES domain(id)
        ON UPDATE CASCADE,

    CONSTRAINT domain_business_account_user_business_account_id_fk
        FOREIGN KEY (business_account_id) REFERENCES business_account(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_domain_business_account_user_domain_user_id
    ON domain_business_account_user(domain_user_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_user_domain_business_account_id
    ON domain_business_account_user(domain_business_account_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_user_business_account_user_id
    ON domain_business_account_user(business_account_user_id);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_user_domain_id_created_at
    ON domain_business_account_user(domain_id, created_at);
CREATE INDEX IF NOT EXISTS idx_domain_business_account_user_domain_id_last_activity_at
    ON domain_business_account_user(domain_id, last_activity_at);

-- ============================================================
-- 3. Protection: forbid direct INSERT/UPDATE on materialization table
-- ============================================================

CREATE OR REPLACE FUNCTION domain_business_account_user_before_insert_wrapper()
    RETURNS trigger AS $$
BEGIN
    IF current_setting('app.domain_business_account_user_insert', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Direct insert into domain_business_account_user is forbidden. Data is managed by triggers.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_business_account_user_before_update_wrapper()
    RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Direct update of domain_business_account_user is forbidden. Data is managed by triggers.';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS domain_business_account_user_before_insert_wrapper_trigger ON domain_business_account_user;
CREATE TRIGGER domain_business_account_user_before_insert_wrapper_trigger
    BEFORE INSERT ON domain_business_account_user
    FOR EACH ROW
    EXECUTE FUNCTION domain_business_account_user_before_insert_wrapper();

DROP TRIGGER IF EXISTS domain_business_account_user_before_update_wrapper_trigger ON domain_business_account_user;
CREATE TRIGGER domain_business_account_user_before_update_wrapper_trigger
    BEFORE UPDATE ON domain_business_account_user
    FOR EACH ROW
    EXECUTE FUNCTION domain_business_account_user_before_update_wrapper();

-- ============================================================
-- 4. Initial data population from existing relationships
-- ============================================================

PERFORM set_config('app.domain_business_account_user_insert', 'on', true);

INSERT INTO domain_business_account_user (domain_user_id, domain_business_account_id, business_account_user_id,
                                          user_id, domain_id, business_account_id, created_at)
SELECT
    du.id,
    dba.id,
    bau.id,
    du.user_id,
    du.domain_id,
    bau.business_account_id,
    GREATEST(du.created_at, dba.created_at, bau.created_at)
FROM domain_user du
    INNER JOIN domain_business_account dba
        ON dba.domain_id = du.domain_id
    INNER JOIN business_account_user bau
        ON bau.user_id = du.user_id
        AND bau.business_account_id = dba.business_account_id;

PERFORM set_config('app.domain_business_account_user_insert', 'off', true);

-- ============================================================
-- 5. Stored procedures for materialization sync logic
--    (delete not needed — handled by FK ON DELETE CASCADE)
-- ============================================================

-- --- Triggered by domain_user changes --------------------------------------

CREATE OR REPLACE FUNCTION domain_business_account_user_insert_by_domain_user(
    p_domain_user_id uuid,
    p_user_id uuid,
    p_domain_id uuid
) RETURNS void AS $$
BEGIN
    PERFORM set_config('app.domain_business_account_user_insert', 'on', true);
    INSERT INTO domain_business_account_user
        (domain_user_id, domain_business_account_id, business_account_user_id,
         user_id, domain_id, business_account_id, created_at)
    SELECT
        p_domain_user_id,
        dba.id,
        bau.id,
        p_user_id,
        p_domain_id,
        bau.business_account_id,
        CURRENT_TIMESTAMP
    FROM domain_business_account dba
        INNER JOIN business_account_user bau
            ON bau.business_account_id = dba.business_account_id
            AND bau.user_id = p_user_id
    WHERE dba.domain_id = p_domain_id
    ON CONFLICT DO NOTHING;
END;
$$ LANGUAGE plpgsql;

-- --- Triggered by domain_business_account changes --------------------------

CREATE OR REPLACE FUNCTION domain_business_account_user_insert_by_domain_business_account(
    p_domain_business_account_id uuid,
    p_business_account_id uuid,
    p_domain_id uuid
) RETURNS void AS $$
BEGIN
    PERFORM set_config('app.domain_business_account_user_insert', 'on', true);
    INSERT INTO domain_business_account_user
        (domain_user_id, domain_business_account_id, business_account_user_id,
         user_id, domain_id, business_account_id, created_at)
    SELECT
        du.id,
        p_domain_business_account_id,
        bau.id,
        du.user_id,
        p_domain_id,
        p_business_account_id,
        CURRENT_TIMESTAMP
    FROM domain_user du
        INNER JOIN business_account_user bau
            ON bau.user_id = du.user_id
            AND bau.business_account_id = p_business_account_id
    WHERE du.domain_id = p_domain_id
    ON CONFLICT DO NOTHING;
END;
$$ LANGUAGE plpgsql;

-- --- Triggered by business_account_user changes ----------------------------

CREATE OR REPLACE FUNCTION domain_business_account_user_insert_by_business_account_user(
    p_business_account_user_id uuid,
    p_user_id uuid,
    p_business_account_id uuid
) RETURNS void AS $$
BEGIN
    PERFORM set_config('app.domain_business_account_user_insert', 'on', true);
    INSERT INTO domain_business_account_user
        (domain_user_id, domain_business_account_id, business_account_user_id,
         user_id, domain_id, business_account_id, created_at)
    SELECT
        du.id,
        dba.id,
        p_business_account_user_id,
        p_user_id,
        du.domain_id,
        p_business_account_id,
        CURRENT_TIMESTAMP
    FROM domain_user du
        INNER JOIN domain_business_account dba
            ON dba.domain_id = du.domain_id
            AND dba.business_account_id = p_business_account_id
    WHERE du.user_id = p_user_id
    ON CONFLICT DO NOTHING;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- 6. Wrapper functions
-- ============================================================

-- --- domain_user: forbid changing key fields on UPDATE ---------------------

CREATE OR REPLACE FUNCTION domain_user_before_update_wrapper()
    RETURNS trigger AS $$
BEGIN
    IF OLD.user_id IS DISTINCT FROM NEW.user_id
       OR OLD.domain_id IS DISTINCT FROM NEW.domain_id THEN
        RAISE EXCEPTION 'Its forbidden to change domain_user.user_id or domain_user.domain_id';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_user_after_insert_wrapper()
    RETURNS trigger AS $$
BEGIN
    PERFORM domain_business_account_user_insert_by_domain_user(NEW.id, NEW.user_id, NEW.domain_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- --- domain_business_account: after-insert + existing after-update ----------

CREATE OR REPLACE FUNCTION domain_business_account_after_insert_wrapper()
    RETURNS trigger AS $$
BEGIN
    PERFORM domain_business_account_user_insert_by_domain_business_account(NEW.id, NEW.business_account_id, NEW.domain_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- --- business_account_user: forbid changing key fields on UPDATE -----------

CREATE OR REPLACE FUNCTION business_account_user_before_update_wrapper()
    RETURNS trigger AS $$
BEGIN
    IF OLD.user_id IS DISTINCT FROM NEW.user_id
       OR OLD.business_account_id IS DISTINCT FROM NEW.business_account_id THEN
        RAISE EXCEPTION 'Its forbidden to change business_account_user.user_id or business_account_user.business_account_id';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION business_account_user_after_insert_wrapper()
    RETURNS trigger AS $$
BEGIN
    PERFORM domain_business_account_user_insert_by_business_account_user(NEW.id, NEW.user_id, NEW.business_account_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- 7. Triggers
-- ============================================================

-- --- domain_user ---
DROP TRIGGER IF EXISTS domain_user_before_update_wrapper_trigger ON domain_user;
CREATE TRIGGER domain_user_before_update_wrapper_trigger
    BEFORE UPDATE ON domain_user
    FOR EACH ROW
    EXECUTE FUNCTION domain_user_before_update_wrapper();

DROP TRIGGER IF EXISTS domain_user_after_insert_wrapper_trigger ON domain_user;
CREATE TRIGGER domain_user_after_insert_wrapper_trigger
    AFTER INSERT ON domain_user
    FOR EACH ROW
    EXECUTE FUNCTION domain_user_after_insert_wrapper();

-- --- domain_business_account ---
DROP TRIGGER IF EXISTS domain_business_account_after_insert_wrapper_trigger ON domain_business_account;
CREATE TRIGGER domain_business_account_after_insert_wrapper_trigger
    AFTER INSERT ON domain_business_account
    FOR EACH ROW
    EXECUTE FUNCTION domain_business_account_after_insert_wrapper();

-- --- business_account_user ---
DROP TRIGGER IF EXISTS business_account_user_before_update_wrapper_trigger ON business_account_user;
CREATE TRIGGER business_account_user_before_update_wrapper_trigger
    BEFORE UPDATE ON business_account_user
    FOR EACH ROW
    EXECUTE FUNCTION business_account_user_before_update_wrapper();

DROP TRIGGER IF EXISTS business_account_user_after_insert_wrapper_trigger ON business_account_user;
CREATE TRIGGER business_account_user_after_insert_wrapper_trigger
    AFTER INSERT ON business_account_user
    FOR EACH ROW
    EXECUTE FUNCTION business_account_user_after_insert_wrapper();
