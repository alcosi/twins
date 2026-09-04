-- relation twin: a shadow twin that carries a twin_link's extra relation attributes
-- (twin_link itself has no attribute columns).
--
-- 1) link.relation_twin_class_id: opt-in class pointer (admin configures per relation type).
--    When set, creating a twin_link of this link auto-creates a relation twin of that class
--    with id == twin_link.id (mirrors the job_twin_class_id pattern in twin_trigger).
ALTER TABLE link
ADD COLUMN IF NOT EXISTS relation_twin_class_id UUID REFERENCES twin_class(id);
CREATE INDEX IF NOT EXISTS idx_link_relation_twin_class_id ON link (relation_twin_class_id);

-- 2) twin_link.relation_twin_id: explicit pointer to the relation twin instance.
--    Redundant (== twin_link.id) but hosts the FK. ON DELETE CASCADE: deleting the relation twin
--    directly removes the twin_link (reverse direction).
ALTER TABLE twin_link
ADD COLUMN IF NOT EXISTS relation_twin_id UUID REFERENCES twin(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_twin_link_relation_twin_id ON twin_link (relation_twin_id);

-- Defensive: enforce the redundancy invariant (relation_twin_id, when set, equals the twin_link's own id).
ALTER TABLE twin_link
ADD CONSTRAINT twin_link_relation_twin_id_eq_id CHECK (relation_twin_id IS NULL OR relation_twin_id = id);

-- 3) AFTER DELETE trigger: deleting a twin_link removes its relation twin (forward direction).
--    The FK above only cascades relation-twin -> twin_link; this trigger covers twin_link -> relation twin.
--    AFTER (not BEFORE) + the fact that a relation twin is never an endpoint of any twin_link
--    makes this recursion-free in both directions.
CREATE OR REPLACE FUNCTION twin_link_delete_relation_twin() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.relation_twin_id IS NOT NULL THEN
        DELETE FROM twin WHERE id = OLD.relation_twin_id;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS twin_link_delete_relation_twin ON twin_link;
CREATE TRIGGER twin_link_delete_relation_twin
AFTER DELETE ON twin_link
FOR EACH ROW
EXECUTE FUNCTION twin_link_delete_relation_twin();
