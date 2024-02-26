CREATE EXTENSION IF NOT EXISTS ltree;
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT FROM information_schema.columns
            WHERE table_name = 'twin' AND column_name = 'hierarchy_tree'
        ) THEN
            ALTER TABLE public.twin ADD COLUMN hierarchy_tree ltree;
        END IF;
    END$$;

DROP TRIGGER IF EXISTS updateHierarchyTreeTrigger ON public.twin;
DROP TRIGGER IF EXISTS trigger_recalculateHierarchy ON public.twin_class;
DROP FUNCTION IF EXISTS public.detectHierarchyTree(UUID);
DROP FUNCTION IF EXISTS public.updateHierarchyTreeHard(UUID, TEXT);
DROP FUNCTION IF EXISTS public.updateHierarchyTreeSoft(UUID, TEXT);
DROP FUNCTION IF EXISTS public.processHierarchyTreeUpdate();
DROP FUNCTION IF EXISTS public.recalculateHierarchyForClassTwins();




-- TWIN SECTION------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.detectHierarchyTree(twin_id UUID)
    RETURNS TEXT AS $$
DECLARE
    hierarchy TEXT := '';
    current_id UUID := twin_id;
    parent_id UUID;
    class_space BOOLEAN;
    visited_ids UUID[] := ARRAY[twin_id]; -- init visited twins, with twin-in id at begin.
BEGIN
    -- loop for build hierarchy from twin-in to root
    LOOP
        -- get head-id & space flag of class for twin-in
        SELECT t.head_twin_id, COALESCE(tc.space, false) INTO parent_id, class_space
        FROM public.twin t
                 LEFT JOIN public.twin_class tc ON t.twin_class_id = tc.id
        WHERE t.id = current_id;

        -- check for cycle
        IF parent_id = ANY(visited_ids) THEN
            RAISE EXCEPTION 'Cycle detected in hierarchy for twin_id %', twin_id;
        END IF;

        -- add 'S_' to id if twin class is space
        -- also replace - to _ for compatibility with ltree
        IF class_space THEN
            hierarchy := 'S_' || replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        ELSE
            hierarchy := replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        END IF;

        -- exit loop when root reached
        EXIT WHEN parent_id IS NULL;

        -- add current_id to visited_ids before moving to the next twin
        visited_ids := array_append(visited_ids, parent_id);

        -- next twin upper in hierarchy.
        current_id := parent_id;
    END LOOP;
    RETURN hierarchy;
END;
$$ LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION public.updateHierarchyTreeHard(twin_id UUID, new_hierarchy_tree TEXT)
    RETURNS VOID AS $$
DECLARE
    hierarchy_to_use TEXT;
BEGIN
    -- if hier. in params - use it. if not - detect it and use.
    IF new_hierarchy_tree IS NOT NULL THEN
        hierarchy_to_use := new_hierarchy_tree;
    ELSE
        hierarchy_to_use := public.detectHierarchyTree(twin_id);
    END IF;

    -- update hier. for twin-in
    UPDATE public.twin
    SET hierarchy_tree = text2ltree(replace(hierarchy_to_use, '-', '_'))
    WHERE id = twin_id;

    -- update hier. for twin-in children and their children, and their...
    WITH RECURSIVE descendants AS (
        SELECT id, 1 AS depth
        FROM public.twin
        WHERE head_twin_id = twin_id
        UNION ALL
        SELECT t.id, d.depth + 1
        FROM public.twin t
                 INNER JOIN descendants d ON t.head_twin_id = d.id
        WHERE d.depth < 10
    )
    UPDATE public.twin
    SET hierarchy_tree = text2ltree(replace(public.detectHierarchyTree(id), '-', '_'))
    WHERE id IN (SELECT id FROM descendants);
END;
$$ LANGUAGE plpgsql;





CREATE OR REPLACE FUNCTION public.updateHierarchyTreeSoft(twin_id UUID, new_hierarchy_tree TEXT)
    RETURNS VOID AS $$
DECLARE
    old_hierarchy TEXT;
    new_hierarchy TEXT;
    twin_id_str TEXT;
BEGIN
   twin_id_str := replace(twin_id::text, '-', '_');

    SELECT hierarchy_tree::text INTO old_hierarchy FROM public.twin WHERE id = twin_id;

    IF new_hierarchy_tree IS NULL THEN
        new_hierarchy := public.detectHierarchyTree(twin_id);
    ELSE
        new_hierarchy := new_hierarchy_tree;
    END IF;

    new_hierarchy := replace(new_hierarchy, '-', '_');
    new_hierarchy := new_hierarchy || '.' || twin_id_str;

    UPDATE public.twin
    SET hierarchy_tree = text2ltree(replace(hierarchy_tree::text, old_hierarchy, new_hierarchy))
    WHERE hierarchy_tree <@ text2ltree(old_hierarchy);
END;
$$ LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION public.processHierarchyTreeUpdate()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND (OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id) THEN
        RAISE NOTICE 'Process update for: %', NEW.id;
        IF OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id THEN
            PERFORM public.updateHierarchyTreeSoft(NEW.id, public.detectHierarchyTree(COALESCE(NEW.head_twin_id, NEW.id)));
        ELSE
            PERFORM public.updateHierarchyTreeSoft(NEW.id, NULL);
        END IF;
    ELSIF TG_OP = 'INSERT' THEN
        RAISE NOTICE 'Process insert for: %', NEW.id;
        PERFORM public.updateHierarchyTreeHard(NEW.id, public.detectHierarchyTree(COALESCE(NEW.head_twin_id, NEW.id)));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER updateHierarchyTreeTrigger
    AFTER INSERT OR UPDATE OF head_twin_id ON public.twin
    FOR EACH ROW
EXECUTE FUNCTION public.processHierarchyTreeUpdate();





-- TWIN-CLASS SECTION------------------------------------------------------------------------------------
-- if space field in twin class changed trigger update... function for all twin-descendants of this class
CREATE OR REPLACE FUNCTION public.recalculateHierarchyForClassTwins()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.space IS DISTINCT FROM NEW.space THEN
        PERFORM public.updateHierarchyTreeSoft(t.id, NULL)
        FROM public.twin t
        WHERE t.twin_class_id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_recalculateHierarchy
    AFTER UPDATE OF space ON public.twin_class
FOR EACH ROW
EXECUTE FUNCTION public.recalculateHierarchyForClassTwins();



-------------------------------------------------------------------------
-- call update... function for all root-twins, to update all twin's hier.
DO $$
    DECLARE
        root_twin RECORD;
    BEGIN
        FOR root_twin IN SELECT id FROM public.twin WHERE head_twin_id IS NULL LOOP
                PERFORM public.updateHierarchyTreeHard(root_twin.id, NULL);
            END LOOP;
    END $$;

-------------------------------------------------------------------------
-- search function
CREATE OR REPLACE FUNCTION ltree_check(hierarchy_tree ltree, ltree_value text)
    RETURNS boolean AS $$
BEGIN
    RETURN hierarchy_tree ~ ltree_value::lquery;
END;
$$ LANGUAGE plpgsql IMMUTABLE STRICT;
