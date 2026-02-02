ALTER TABLE twin_class
    ADD COLUMN IF NOT EXISTS extends_hierarchy_counter_direct_children int NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS head_hierarchy_counter_direct_children int NOT NULL DEFAULT 0;

CREATE OR REPLACE FUNCTION update_direct_children_counters(
    p_parent_id UUID,
    p_hierarchy_type TEXT
) RETURNS VOID AS $$
DECLARE
    v_direct_children_count INT;
BEGIN
    IF p_hierarchy_type = 'extends' THEN
        SELECT COUNT(*)
        INTO v_direct_children_count
        FROM twin_class
        WHERE extends_twin_class_id = p_parent_id;

        UPDATE twin_class
        SET extends_hierarchy_counter_direct_children = COALESCE(v_direct_children_count, 0)
        WHERE id = p_parent_id;

    ELSIF p_hierarchy_type = 'head' THEN
        SELECT COUNT(*)
        INTO v_direct_children_count
        FROM twin_class
        WHERE head_twin_class_id = p_parent_id;

        UPDATE twin_class
        SET head_hierarchy_counter_direct_children = COALESCE(v_direct_children_count, 0)
        WHERE id = p_parent_id;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION initialize_all_direct_children_counters()
    RETURNS VOID AS $$
BEGIN
    WITH extends_counts AS (
        SELECT extends_twin_class_id, COUNT(*) as cnt
        FROM twin_class
        WHERE extends_twin_class_id IS NOT NULL
        GROUP BY extends_twin_class_id
    )
    UPDATE twin_class tc
    SET extends_hierarchy_counter_direct_children = ec.cnt
    FROM extends_counts ec
    WHERE tc.id = ec.extends_twin_class_id;

    WITH head_counts AS (
        SELECT head_twin_class_id, COUNT(*) as cnt
        FROM twin_class
        WHERE head_twin_class_id IS NOT NULL
        GROUP BY head_twin_class_id
    )
    UPDATE twin_class tc
    SET head_hierarchy_counter_direct_children = hc.cnt
    FROM head_counts hc
    WHERE tc.id = hc.head_twin_class_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION twin_class_after_insert_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- Call tree update on insert when extends_twin_class_id is set
    PERFORM hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);
    PERFORM twin_class_has_segments_check(new.head_twin_class_id);

    -- Update direct children counters for parents
    IF NEW.extends_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(NEW.extends_twin_class_id, 'extends');
    END IF;

    IF NEW.head_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(NEW.head_twin_class_id, 'head');
    END IF;

    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION twin_class_after_delete_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- Remove i18n and translations for deleted twin_class
    PERFORM twin_class_on_delete_i18n_and_translations_delete(old);
    PERFORM twin_class_has_segments_check(old.head_twin_class_id);

    -- Update direct children counters for parents
    IF OLD.extends_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(OLD.extends_twin_class_id, 'extends');
    END IF;

    IF OLD.head_twin_class_id IS NOT NULL THEN
        PERFORM update_direct_children_counters(OLD.head_twin_class_id, 'head');
    END IF;

    RETURN OLD;
END;
$$;


CREATE OR REPLACE FUNCTION twin_class_after_update_wrapper() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- Update tree if extends_twin_class_id changed
    IF NEW.extends_twin_class_id IS DISTINCT FROM OLD.extends_twin_class_id
        -- we need to update tree only in case if extends_twin_class_id was updated and tree wasn't
        AND NEW.extends_hierarchy_tree IS NOT DISTINCT FROM OLD.extends_hierarchy_tree THEN
        PERFORM hierarchy_twin_class_extends_process_tree_update(old, new, TG_OP);
    END IF;

    -- Update tree and has_segments if head_twin_class_id changed
    IF NEW.head_twin_class_id IS DISTINCT FROM OLD.head_twin_class_id THEN
        PERFORM hierarchy_twin_class_head_process_tree_update(old, new, TG_OP);
        PERFORM twin_class_has_segments_check(old.head_twin_class_id);
        PERFORM twin_class_has_segments_check(new.head_twin_class_id);
    END IF;

    -- Update has_segments if segment changed
    IF NEW.segment IS DISTINCT FROM OLD.segment THEN
        PERFORM twin_class_has_segments_check(new.head_twin_class_id);
    END IF;

    -- Recalculate hierarchy if schema space fields changed
    IF (NEW.permission_schema_space IS DISTINCT FROM OLD.permission_schema_space)
        OR (NEW.twinflow_schema_space IS DISTINCT FROM OLD.twinflow_schema_space)
        OR (NEW.twin_class_schema_space IS DISTINCT FROM OLD.twin_class_schema_space)
        OR (NEW.alias_space IS DISTINCT FROM OLD.alias_space) THEN
        PERFORM twin_class_hierarchy_recalculate(old, new);
    END IF;

    -- Auto update permissions if key changed
    IF NEW.key IS DISTINCT FROM OLD.key THEN
        PERFORM permissions_autoupdate_on_twin_class_update(old, new);
    END IF;

    -- Update inherited bread_crumbs_face_id if changed
    IF NEW.bread_crumbs_face_id IS DISTINCT FROM OLD.bread_crumbs_face_id THEN
        PERFORM twin_class_update_inherited_bread_crumbs_face_id(old, new);
    END IF;

    -- Update inherited page_face_id if changed
    IF NEW.page_face_id IS DISTINCT FROM OLD.page_face_id THEN
        PERFORM twin_class_update_inherited_page_face_id(old, new);
    END IF;

    -- Update inherited marker_data_list_id id if changed
    IF NEW.marker_data_list_id IS DISTINCT FROM OLD.marker_data_list_id THEN
        PERFORM twin_class_update_inherited_marker_data_list(old, new);
    END IF;

    -- Update inherited tag_data_list_id id if changed
    IF NEW.tag_data_list_id IS DISTINCT FROM OLD.tag_data_list_id THEN
        PERFORM twin_class_update_inherited_tag_data_list(old, new);
    END IF;

    -- Update direct children counters if parent references changed
    IF NEW.extends_twin_class_id IS DISTINCT FROM OLD.extends_twin_class_id THEN
        -- Update old parent's counter
        IF OLD.extends_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(OLD.extends_twin_class_id, 'extends');
        END IF;

        -- Update new parent's counter
        IF NEW.extends_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(NEW.extends_twin_class_id, 'extends');
        END IF;
    END IF;

    IF NEW.head_twin_class_id IS DISTINCT FROM OLD.head_twin_class_id THEN
        -- Update old parent's counter
        IF OLD.head_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(OLD.head_twin_class_id, 'head');
        END IF;

        -- Update new parent's counter
        IF NEW.head_twin_class_id IS NOT NULL THEN
            PERFORM update_direct_children_counters(NEW.head_twin_class_id, 'head');
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

SELECT initialize_all_direct_children_counters();