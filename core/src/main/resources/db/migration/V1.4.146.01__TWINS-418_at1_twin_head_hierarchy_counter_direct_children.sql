-- TWINS-418: twin head_hierarchy_counter_direct_children column and triggers

ALTER TABLE twin
    ADD COLUMN IF NOT EXISTS head_hierarchy_counter_direct_children int NOT NULL DEFAULT 0;

-- Update direct children counter for one parent twin (by head)
CREATE OR REPLACE FUNCTION update_twin_head_direct_children_counter(p_parent_id UUID)
    RETURNS VOID AS
$$
DECLARE
    v_direct_children_count INT;
BEGIN
    IF p_parent_id IS NULL THEN
        RETURN;
    END IF;

    SELECT COUNT(*)
    INTO v_direct_children_count
    FROM twin
    WHERE head_twin_id = p_parent_id;

    UPDATE twin
    SET head_hierarchy_counter_direct_children = COALESCE(v_direct_children_count, 0)
    WHERE id = p_parent_id;
END;
$$ LANGUAGE plpgsql;

-- Initialize all twin head direct children counters
CREATE OR REPLACE FUNCTION initialize_twin_head_direct_children_counters()
    RETURNS VOID AS
$$
BEGIN
    WITH head_counts AS (
        SELECT head_twin_id, COUNT(*) AS cnt
        FROM twin
        WHERE head_twin_id IS NOT NULL
        GROUP BY head_twin_id
    )
    UPDATE twin t
    SET head_hierarchy_counter_direct_children = hc.cnt
    FROM head_counts hc
    WHERE t.id = hc.head_twin_id;
END;
$$ LANGUAGE plpgsql;

-- twin_after_insert_wrapper: add counter update for parent head
CREATE OR REPLACE FUNCTION twin_after_insert_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    RAISE NOTICE 'Process insert for: %', new.id;
    PERFORM hierarchyUpdateTreeHard(new.id, hierarchyDetectTree(new.id));

    IF NEW.head_twin_id IS NOT NULL THEN
        PERFORM update_twin_head_direct_children_counter(NEW.head_twin_id);
    END IF;

    RETURN NEW;
END;
$$;

-- twin_after_update_wrapper: add counter update when head_twin_id changes
CREATE OR REPLACE FUNCTION twin_after_update_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id THEN
        RAISE NOTICE 'Process update for: %', new.id;
        PERFORM hierarchyUpdateTreeSoft(new.id, public.hierarchyDetectTree(new.id));

        IF OLD.head_twin_id IS NOT NULL THEN
            PERFORM update_twin_head_direct_children_counter(OLD.head_twin_id);
        END IF;
        IF NEW.head_twin_id IS NOT NULL THEN
            PERFORM update_twin_head_direct_children_counter(NEW.head_twin_id);
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

-- twin_after_delete_wrapper: update parent's counter when twin is deleted
CREATE OR REPLACE FUNCTION twin_after_delete_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF OLD.head_twin_id IS NOT NULL THEN
        PERFORM update_twin_head_direct_children_counter(OLD.head_twin_id);
    END IF;
    RETURN OLD;
END;
$$;

CREATE TRIGGER twin_after_delete_wrapper_trigger
    AFTER DELETE
    ON twin
    FOR EACH ROW
EXECUTE PROCEDURE twin_after_delete_wrapper();

-- Backfill existing data
SELECT initialize_twin_head_direct_children_counters();
