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
DROP FUNCTION IF EXISTS public.updateHierarchyTree(UUID, TEXT);
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
BEGIN
    -- loop for build hier. from twin-in to root
    LOOP
        -- get head-id & space flag of class for twin-in
        SELECT t.head_twin_id, COALESCE(tc.space, false) INTO parent_id, class_space
        FROM public.twin t
                 LEFT JOIN public.twin_class tc ON t.twin_class_id = tc.id
        WHERE t.id = current_id;

        -- add 'S_' to id if twin class is space
        -- also replace - to _ for compatibility with ltree
        IF class_space THEN
            hierarchy := 'S_' || replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        ELSE
            hierarchy := replace(current_id::text, '-', '_') || (CASE WHEN hierarchy = '' THEN '' ELSE '.' END) || hierarchy;
        END IF;

        -- exit loop when root reached
        EXIT WHEN parent_id IS NULL;

        -- next twin upper in hier.
        current_id := parent_id;
    END LOOP;
    RETURN hierarchy;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION public.updateHierarchyTree(twin_id UUID, new_hierarchy_tree TEXT)
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
        SELECT id
        FROM public.twin
        WHERE head_twin_id = twin_id
        UNION ALL
        SELECT t.id
        FROM public.twin t
                 INNER JOIN descendants d ON t.head_twin_id = d.id
    )
    UPDATE public.twin
    -- its to difficult to calculate hier. for children "on fly" with space checking. I use dectect... function call.
    -- if we need "on fly" calculation, it will take extra time, and i'm not sure what the performance will increase. Questionable optimization.
    SET hierarchy_tree = text2ltree(replace(public.detectHierarchyTree(id), '-', '_'))
    WHERE id IN (SELECT id FROM descendants);
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION public.processHierarchyTreeUpdate()
    RETURNS TRIGGER AS $$
BEGIN
    -- TODO remove triggering on name change, its only for debug.
    IF TG_OP = 'UPDATE' AND (OLD.name IS DISTINCT FROM NEW.name OR OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id) THEN
        RAISE NOTICE 'Process update for: %', NEW.id;
        IF OLD.head_twin_id IS DISTINCT FROM NEW.head_twin_id THEN
            PERFORM public.updateHierarchyTree(NEW.id, public.detectHierarchyTree(COALESCE(NEW.head_twin_id, NEW.id)));
        ELSE
            PERFORM public.updateHierarchyTree(NEW.id, NULL);
        END IF;
    ELSIF TG_OP = 'INSERT' THEN
        RAISE NOTICE 'Process insert for: %', NEW.id;
        PERFORM public.updateHierarchyTree(NEW.id, public.detectHierarchyTree(COALESCE(NEW.head_twin_id, NEW.id)));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- TODO remove triggering on name change, its only for debug.
CREATE TRIGGER updateHierarchyTreeTrigger
    AFTER INSERT OR UPDATE OF name, head_twin_id ON public.twin
    FOR EACH ROW
EXECUTE FUNCTION public.processHierarchyTreeUpdate();




-- TWIN-CLASS SECTION------------------------------------------------------------------------------------
-- if space field in twin class changed trigger update... function for all twin-descendants of this class
CREATE OR REPLACE FUNCTION public.recalculateHierarchyForClassTwins()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.space IS DISTINCT FROM NEW.space THEN
        PERFORM public.updateHierarchyTree(t.id, NULL)
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
                PERFORM public.updateHierarchyTree(root_twin.id, NULL);
            END LOOP;
    END $$;

-------------------------------------------------------------------------
-- search function
CREATE OR REPLACE FUNCTION ltree_check(hierarchy_tree ltree, ltree_value text)
    RETURNS boolean AS $$
BEGIN
    RETURN hierarchy_tree <@ ltree_value::ltree;
END;
$$ LANGUAGE plpgsql IMMUTABLE STRICT;
