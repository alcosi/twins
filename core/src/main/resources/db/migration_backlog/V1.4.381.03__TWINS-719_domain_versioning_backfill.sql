-- TWINS-719: domain configuration versioning, model v2 (pilot).
-- Step 3/4: backfill existing data. Runs after 02 (functions), before 04 (triggers).

-- ============================================================================
-- 1. Every existing domain gets its V1 window (idempotent)
-- ============================================================================
DO $$
DECLARE
    d record;
    v_id uuid;
BEGIN
    FOR d IN SELECT id FROM domain LOOP
        v_id := domain_version_init(d.id);
        UPDATE domain SET current_domain_version_id = v_id
         WHERE id = d.id AND current_domain_version_id IS NULL;
    END LOOP;
END $$;

-- ============================================================================
-- 2. domain_id of the child tables derived from the parent row (one bulk UPDATE per table)
-- ============================================================================
UPDATE twin_class_field tcf
   SET domain_id = tc.domain_id
  FROM twin_class tc
 WHERE tcf.twin_class_id = tc.id
   AND tcf.domain_id IS NULL;

UPDATE twin_status ts
   SET domain_id = tc.domain_id
  FROM twin_class tc
 WHERE ts.twin_class_id = tc.id
   AND ts.domain_id IS NULL;

UPDATE data_list_option dlo
   SET domain_id = dl.domain_id
  FROM data_list dl
 WHERE dlo.data_list_id = dl.id
   AND dlo.domain_id IS NULL;

UPDATE data_list_subset dls
   SET domain_id = dl.domain_id
  FROM data_list dl
 WHERE dls.data_list_id = dl.id
   AND dls.domain_id IS NULL;

UPDATE data_list_subset_option dlso
   SET domain_id = dls.domain_id
  FROM data_list_subset dls
 WHERE dlso.data_list_subset_id = dls.id
   AND dlso.domain_id IS NULL;

-- ============================================================================
-- 3. Stamp every domain row with the current (V1) version of its domain.
--    The first release of an existing domain becomes a baseline with a full forward delta.
--    System rows (domain_id IS NULL) are not versioned: stamp stays NULL.
-- ============================================================================
UPDATE twin_class t
   SET domain_version_id = d.current_domain_version_id
  FROM domain d
 WHERE t.domain_id = d.id AND t.domain_version_id IS NULL;

UPDATE twin_class_field t
   SET domain_version_id = d.current_domain_version_id
  FROM domain d
 WHERE t.domain_id = d.id AND t.domain_version_id IS NULL;

UPDATE twin_status t
   SET domain_version_id = d.current_domain_version_id
  FROM domain d
 WHERE t.domain_id = d.id AND t.domain_version_id IS NULL;

UPDATE data_list t
   SET domain_version_id = d.current_domain_version_id
  FROM domain d
 WHERE t.domain_id = d.id AND t.domain_version_id IS NULL;

UPDATE data_list_option t
   SET domain_version_id = d.current_domain_version_id
  FROM domain d
 WHERE t.domain_id = d.id AND t.domain_version_id IS NULL;

UPDATE data_list_subset t
   SET domain_version_id = d.current_domain_version_id
  FROM domain d
 WHERE t.domain_id = d.id AND t.domain_version_id IS NULL;

UPDATE data_list_subset_option t
   SET domain_version_id = d.current_domain_version_id
  FROM domain d
 WHERE t.domain_id = d.id AND t.domain_version_id IS NULL;

-- ============================================================================
-- 4. Validate the NOT VALID FKs, tighten the invariants
-- ============================================================================
ALTER TABLE twin_class VALIDATE CONSTRAINT twin_class_domain_version_id_fk;
ALTER TABLE twin_class_field VALIDATE CONSTRAINT twin_class_field_domain_version_id_fk;
ALTER TABLE twin_status VALIDATE CONSTRAINT twin_status_domain_version_id_fk;
ALTER TABLE data_list VALIDATE CONSTRAINT data_list_domain_version_id_fk;
ALTER TABLE data_list_option VALIDATE CONSTRAINT data_list_option_domain_version_id_fk;
ALTER TABLE data_list_subset VALIDATE CONSTRAINT data_list_subset_domain_version_id_fk;
ALTER TABLE data_list_subset_option VALIDATE CONSTRAINT data_list_subset_option_domain_version_id_fk;

-- domain.current_domain_version_id becomes NOT NULL: every existing domain has V1 now,
-- new domains get it from the before-insert trigger (04)
ALTER TABLE domain ALTER COLUMN current_domain_version_id SET NOT NULL;

-- ============================================================================
-- 5. FKs for the child domain_id columns + invariant CHECKs (idempotent helper)
-- ============================================================================
CREATE OR REPLACE FUNCTION add_domain_version_constraint(tbl text, cname text, cdef text) RETURNS void AS
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = cname AND conrelid = tbl::regclass) THEN
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I %s', tbl, cname, cdef);
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT add_domain_version_constraint('twin_class_field', 'twin_class_field_domain_id_fk',
    'FOREIGN KEY (domain_id) REFERENCES domain(id) ON UPDATE CASCADE ON DELETE RESTRICT');
SELECT add_domain_version_constraint('twin_status', 'twin_status_domain_id_fk',
    'FOREIGN KEY (domain_id) REFERENCES domain(id) ON UPDATE CASCADE ON DELETE RESTRICT');
SELECT add_domain_version_constraint('data_list_option', 'data_list_option_domain_id_fk',
    'FOREIGN KEY (domain_id) REFERENCES domain(id) ON UPDATE CASCADE ON DELETE RESTRICT');
SELECT add_domain_version_constraint('data_list_subset', 'data_list_subset_domain_id_fk',
    'FOREIGN KEY (domain_id) REFERENCES domain(id) ON UPDATE CASCADE ON DELETE RESTRICT');
SELECT add_domain_version_constraint('data_list_subset_option', 'data_list_subset_option_domain_id_fk',
    'FOREIGN KEY (domain_id) REFERENCES domain(id) ON UPDATE CASCADE ON DELETE RESTRICT');

-- invariant: a domain row always carries a stamp; a system row never does
SELECT add_domain_version_constraint('twin_class', 'twin_class_domain_version_invariant_ck',
    'CHECK (domain_id IS NULL OR domain_version_id IS NOT NULL)');
SELECT add_domain_version_constraint('twin_class_field', 'twin_class_field_domain_version_invariant_ck',
    'CHECK (domain_id IS NULL OR domain_version_id IS NOT NULL)');
SELECT add_domain_version_constraint('twin_status', 'twin_status_domain_version_invariant_ck',
    'CHECK (domain_id IS NULL OR domain_version_id IS NOT NULL)');
SELECT add_domain_version_constraint('data_list', 'data_list_domain_version_invariant_ck',
    'CHECK (domain_id IS NULL OR domain_version_id IS NOT NULL)');
SELECT add_domain_version_constraint('data_list_option', 'data_list_option_domain_version_invariant_ck',
    'CHECK (domain_id IS NULL OR domain_version_id IS NOT NULL)');
SELECT add_domain_version_constraint('data_list_subset', 'data_list_subset_domain_version_invariant_ck',
    'CHECK (domain_id IS NULL OR domain_version_id IS NOT NULL)');
SELECT add_domain_version_constraint('data_list_subset_option', 'data_list_subset_option_domain_version_invariant_ck',
    'CHECK (domain_id IS NULL OR domain_version_id IS NOT NULL)');

DROP FUNCTION IF EXISTS add_domain_version_constraint(text, text, text);
