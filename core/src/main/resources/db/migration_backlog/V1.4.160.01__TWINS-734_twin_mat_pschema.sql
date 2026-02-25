UPDATE domain_business_account dba
SET
    permission_schema_id = COALESCE(dba.permission_schema_id, d.permission_schema_id),
    twinflow_schema_id = COALESCE(dba.twinflow_schema_id, d.twinflow_schema_id),
    twin_class_schema_id = COALESCE(dba.twin_class_schema_id, d.twin_class_schema_id)
FROM domain d
WHERE dba.domain_id = d.id;

ALTER TABLE domain
    ALTER COLUMN permission_schema_id SET NOT NULL,
    ALTER COLUMN twinflow_schema_id SET NOT NULL,
    ALTER COLUMN twin_class_schema_id SET NOT NULL;

ALTER TABLE domain_business_account
    ALTER COLUMN permission_schema_id SET NOT NULL,
    ALTER COLUMN twinflow_schema_id SET NOT NULL,
    ALTER COLUMN twin_class_schema_id SET NOT NULL;

ALTER TABLE twin ADD COLUMN IF NOT EXISTS permission_schema_id UUID DEFAULT null;

ALTER TABLE twin ADD COLUMN IF NOT EXISTS custom_view_permission_id boolean DEFAULT false;

create or replace function twin_before_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.custom_view_permission_id IS DISTINCT FROM OLD.custom_view_permission_id and new.custom_view_permission_id THEN
        SELECT view_permission_id INTO NEW.view_permission_id FROM twin_class WHERE id = NEW.twin_class_id;
    END IF;

    RETURN NEW;
END;
$$;

drop trigger if exists twin_before_update_wrapper_trigger on twin;
-- auto-generated definition
create trigger twin_before_update_wrapper_trigger
    before update
    on twin
    for each row
execute procedure twin_before_update_wrapper();

create or replace function twin_class_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    -- fn's if view_permission_id changed
    IF NEW.view_permission_id IS DISTINCT FROM OLD.view_permission_id THEN
        UPDATE twin t SET view_permission_id = NEW.view_permission_id FROM twin_class tc WHERE not t.custom_view_permission_id and t.twin_class_id = NEW.id;
    END IF;

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

UPDATE twin t
SET view_permission_id = tc.view_permission_id
FROM twin_class tc
WHERE not t.custom_view_permission_id and
      t.twin_class_id = tc.id and
      t.view_permission_id is null;

UPDATE twin t
SET custom_view_permission_id = true
FROM twin_class tc
WHERE not t.custom_view_permission_id and
      t.twin_class_id = tc.id and
      t.view_permission_id is not null and
      t.view_permission_id is distinct from tc.view_permission_id;

DROP FUNCTION IF EXISTS hierarchydetecttree(uuid);
create or replace function hierarchydetecttree(p_twin_id uuid)
    returns TABLE( hierarchy text, permission_schema_space_id uuid, twinflow_schema_space_id uuid, twin_class_schema_space_id uuid ,
                   alias_space_id uuid, permission_schema_id uuid, twinflow_schema_id uuid, twin_class_schema_id uuid)
    volatile
    language plpgsql
as
$$
DECLARE
    current_id  UUID   := p_twin_id;
    parent_id   UUID;
    visited_ids UUID[] := ARRAY[p_twin_id];
    local_permission_schema_space_enabled BOOLEAN;
    local_twinflow_schema_space_enabled   BOOLEAN;
    local_twin_class_schema_space_enabled BOOLEAN;
    local_alias_space_enabled             BOOLEAN;
BEGIN
    RAISE NOTICE 'Detected hier. for id: %', p_twin_id;
    -- init return values
    hierarchy := '';
    permission_schema_space_id := NULL;
    twinflow_schema_space_id := NULL;
    twin_class_schema_space_id := NULL;
    alias_space_id := NULL;
    permission_schema_id := NULL;
    twinflow_schema_id := NULL;
    twin_class_schema_id := NULL;
    LOOP
        SELECT t.head_twin_id,
               tc.permission_schema_space,
               tc.twinflow_schema_space,
               tc.twin_class_schema_space,
               tc.alias_space
        INTO parent_id,
            local_permission_schema_space_enabled,
            local_twinflow_schema_space_enabled,
            local_twin_class_schema_space_enabled,
            local_alias_space_enabled
        FROM public.twin t
                 LEFT JOIN public.twin_class tc ON t.twin_class_id = tc.id
        WHERE t.id = current_id;
        -- cycle protection
        IF parent_id = ANY (visited_ids) THEN RAISE EXCEPTION 'Cycle detected in hierarchy for twin_id %', p_twin_id;
        END IF;
        -- detect first space in hierarchy
        IF permission_schema_space_id IS NULL AND local_permission_schema_space_enabled THEN permission_schema_space_id := current_id;
        END IF;
        IF twinflow_schema_space_id IS NULL AND local_twinflow_schema_space_enabled THEN twinflow_schema_space_id := current_id;
        END IF;
        IF twin_class_schema_space_id IS NULL AND local_twin_class_schema_space_enabled THEN twin_class_schema_space_id := current_id;
        END IF;
        IF alias_space_id IS NULL AND local_alias_space_enabled THEN alias_space_id := current_id;
        END IF;
        -- build ltree-compatible hierarchy
        hierarchy := replace(current_id::text, '-', '_') || CASE WHEN hierarchy = '' THEN '' ELSE '.' END || hierarchy;

        EXIT WHEN parent_id IS NULL;

        visited_ids := array_append(visited_ids, parent_id);
        current_id := parent_id;
    END LOOP;
    -- single query to resolve schemas from computed spaces
    SELECT
        COALESCE(sp.permission_schema_id, dba.permission_schema_id, d.permission_schema_id, '00000000-0000-0000-0012-000000000001'::uuid),
        COALESCE(stf.twinflow_schema_id, dba.twinflow_schema_id, d.twinflow_schema_id, '00000000-0000-0000-0013-000000000001'::uuid),
        COALESCE(stc.twin_class_schema_id, dba.twin_class_schema_id, d.twin_class_schema_id, '00000000-0000-0000-0014-000000000001'::uuid)
    INTO
        permission_schema_id,
        twinflow_schema_id,
        twin_class_schema_id
    FROM public.twin t
        -- нужен domain_id через twin_class
             JOIN public.twin_class tc ON tc.id = t.twin_class_id
        -- space level (иерархически вычисленные)
             LEFT JOIN public.space sp ON sp.twin_id = permission_schema_space_id
             LEFT JOIN public.space stf ON stf.twin_id = twinflow_schema_space_id
             LEFT JOIN public.space stc ON stc.twin_id = twin_class_schema_space_id
        -- domain
             LEFT JOIN public.domain d ON d.id = tc.domain_id
        -- domain_business_account
             LEFT JOIN public.domain_business_account dba ON dba.domain_id = tc.domain_id AND dba.business_account_id  is not distinct from t.owner_business_account_id
    WHERE t.id = p_twin_id;

    RAISE NOTICE 'Return detected hier. for: %', hierarchy;

    RETURN QUERY SELECT hierarchy, permission_schema_space_id, twinflow_schema_space_id, twin_class_schema_space_id, alias_space_id, permission_schema_id, twinflow_schema_id, twin_class_schema_id;
END;
$$;




DROP FUNCTION IF EXISTS hierarchyupdatetreehard(uuid, record);
create OR REPLACE function hierarchyupdatetreehard(p_twin_id uuid, detect_data record) returns void
    VOLATILE
    language plpgsql
as
$$
DECLARE
    data_to_use RECORD;
BEGIN
    -- if hier. in params - use it. if not - detect it and use.
    IF detect_data IS NOT NULL THEN
        data_to_use := detect_data;
    ELSE
        data_to_use := public.hierarchyDetectTree(p_twin_id);
    END IF;
    RAISE NOTICE 'Update detected hier. for: %', data_to_use.hierarchy;

    -- update hier. and schemas for twin-in
    UPDATE twin t
    SET hierarchy_tree = text2ltree(data_to_use.hierarchy),
        permission_schema_space_id = data_to_use.permission_schema_space_id,
        twinflow_schema_space_id = data_to_use.twinflow_schema_space_id,
        twin_class_schema_space_id = data_to_use.twin_class_schema_space_id,
        alias_space_id = data_to_use.alias_space_id,
        permission_schema_id = data_to_use.permission_schema_id;

    -- update hier. and schemas for twin-in children and their children, recursively
    WITH RECURSIVE descendants AS (
        SELECT id, 1 AS depth
        FROM public.twin
        WHERE head_twin_id = p_twin_id
        UNION ALL
        SELECT t.id, d.depth + 1
        FROM public.twin t
                 INNER JOIN descendants d ON t.head_twin_id = d.id
        WHERE d.depth < 10
    ), updated_data AS (
        SELECT dt.id, (public.hierarchyDetectTree(dt.id)).* -- use function and expand result
        FROM descendants dt
    )
    UPDATE public.twin t
    SET hierarchy_tree = text2ltree(ud.hierarchy),
        permission_schema_space_id = ud.permission_schema_space_id,
        twinflow_schema_space_id = ud.twinflow_schema_space_id,
        twin_class_schema_space_id = ud.twin_class_schema_space_id,
        alias_space_id = ud.alias_space_id,
        permission_schema_id = ud.permission_schema_id
    FROM updated_data ud
    WHERE t.id = ud.id;
END;
$$;




-- if domain ps_id changes - trigger -> dbu.permission_schema_id update if not custom?
-- todo!!!!!!!!!!!! update all twins
UPDATE twin t
SET permission_schema_id = COALESCE(s.permission_schema_id, dbu.permission_schema_id, d.permission_schema_id, '00000000-0000-0000-0012-000000000001')
FROM twin_class tc
         JOIN domain d ON tc.domain_id = d.id
         LEFT JOIN domain_business_account dbu ON dbu.domain_id = d.id
         LEFT JOIN space s ON true -- Здесь мы не можем сослаться на t, поэтому используем фильтр ниже
WHERE t.twin_class_id = tc.id
  AND (dbu.business_account_id IS NOT DISTINCT FROM t.owner_business_account_id OR t.owner_business_account_id IS NULL)
  AND (s.twin_id IS NOT DISTINCT FROM t.permission_schema_space_id OR t.permission_schema_space_id IS NULL);


-- ALTER TABLE twin alter COLUMN permission_schema_id set not null;


DROP TRIGGER IF EXISTS tiers_domain_business_account_tier_id_update_trigger ON domain_business_account;
DROP TRIGGER IF EXISTS domain_business_account_after_update_trigger ON domain_business_account;
drop function if exists tiers_update_business_account_properties_on_self_tier_id_change();
drop function if exists tiers_update_business_account_properties_on_tier_change();
drop function if exists business_account_properties_update_on_tier_change(uuid);

---------------------------------------------------------------
-----------------------------DBU AU---------------------------
---------------------------------------------------------------
create or replace function domain_business_account_properties_update_on_tier_id_change(p_tier_id uuid) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    UPDATE domain_business_account ba
    SET
        permission_schema_id = t.permission_schema_id,
        twinflow_schema_id = t.twinflow_schema_id,
        twin_class_schema_id = t.twin_class_schema_id,
        notification_schema_id = t.notification_schema_id
    FROM tier t
    WHERE ba.tier_id = p_tier_id
      AND t.id = p_tier_id;
END;
$$;

-- todo
CREATE OR REPLACE FUNCTION twin_permission_schema_id_update_by_dbu(p_new_permission_schema_id uuid, p_old_permission_schema_id uuid, p_business_account_id uuid) RETURNS void
    VOLATILE
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE twin t
    SET
        permission_schema_id = p_new_permission_schema_id

    WHERE
        t.permission_schema_space_id IS NULL
      AND t.permission_schema_id = p_old_permission_schema_id
      AND t.owner_business_account_id != p_business_account_id;
END;
$$;


create function domain_business_account_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    -- todo what happens if dba deleted - cascade? or on business_account changes?(restrict)
    IF OLD.permission_schema_id IS DISTINCT FROM NEW.permission_schema_id and NEW.permission_schema_id is not null THEN
        PERFORM twin_permission_schema_id_update_by_dbu(NEW.permission_schema_id, OLD.permission_schema_id, NEW.business_account_id);
    END IF;
    IF OLD.tier_id IS DISTINCT FROM NEW.tier_id THEN
        PERFORM domain_business_account_properties_update_on_tier_id_change(NEW.tier_id);
    END IF;

    RETURN NULL;
END;
$$;

create trigger domain_business_account_after_update_trigger
    after update
    on domain_business_account
    for each row
execute procedure domain_business_account_after_update_wrapper();

---------------------------------------------------------------
-----------------------------TIER AU---------------------------
---------------------------------------------------------------
drop function if exists  business_account_update_all(uuid, uuid, uuid, uuid, uuid, uuid, boolean);
create or replace function domain_business_account_update_props_on_update_tier(p_tier_id uuid, p_domain_id uuid, p_permission_schema_id uuid, p_twinflow_schema_id uuid, p_twin_class_schema_id uuid, p_notification_schema_id uuid, p_custom boolean) returns void
    volatile
    language plpgsql
as
$$
BEGIN
    IF p_custom THEN
        RETURN;
    END IF;

    UPDATE domain_business_account ba
    SET
        permission_schema_id = p_permission_schema_id,
        twinflow_schema_id = p_twinflow_schema_id,
        twin_class_schema_id = p_twin_class_schema_id,
        notification_schema_id = p_notification_schema_id
    WHERE ba.tier_id = p_tier_id
      AND ba.domain_id = p_domain_id;
END;
$$;

create or replace function tier_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF OLD.permission_schema_id IS DISTINCT FROM NEW.permission_schema_id OR
       OLD.twinflow_schema_id IS DISTINCT FROM NEW.twinflow_schema_id OR
       OLD.twin_class_schema_id IS DISTINCT FROM NEW.twin_class_schema_id OR
       OLD.notification_schema_id IS DISTINCT FROM NEW.notification_schema_id OR
       (OLD.custom IS DISTINCT FROM NEW.custom AND NOT NEW.custom) THEN

        PERFORM domain_business_account_update_props_on_update_tier(
                NEW.id,
                NEW.domain_id,
                NEW.permission_schema_id,
                NEW.twinflow_schema_id,
                NEW.twin_class_schema_id,
                NEW.notification_schema_id,
                NEW.custom
                );
    END IF;

    RETURN NEW;
END;
$$;
---------------------------------------------------------------
-----------------------------DOMAIN AU---------------------------
---------------------------------------------------------------
create or replace function domain_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.permission_schema_id IS DISTINCT FROM OLD.permission_schema_id THEN
        PERFORM
    END IF;
    RETURN NEW;
END;
$$;

drop trigger if exists domain_after_update_wrapper_trigger on domain;
create trigger domain_after_delete_wrapper_trigger
    after update
    on domain
    for each row
execute procedure domain_after_update_wrapper();
---------------------------------------------------------------
-----------------------------SPACE AU---------------------------
---------------------------------------------------------------
create or replace function space_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.permission_schema_id IS DISTINCT FROM OLD.permission_schema_id THEN
        PERFORM
    END IF;
    RETURN NEW;
END;
$$;

drop trigger if exists space_after_update_wrapper_trigger on space;
create trigger space_after_delete_wrapper_trigger
    after update
    on space
    for each row
execute procedure space_after_update_wrapper();

---------------------------------------------------------------
-----------------------------TWIN AU---------------------------
---------------------------------------------------------------
create function twin_after_update_wrapper() returns trigger
    language plpgsql
as
$$
begin
    if old.permission_schema_space_id is distinct from new.permission_schema_space_id then
        perform ;
    end if;
    if old.head_twin_id is distinct from new.head_twin_id then
        raise notice 'Process update for: %', new.id;
        perform hierarchyUpdateTreeSoft(new.id, public.hierarchyDetectTree(new.id));
    end if;
    if old.owner_business_account_id is distinct from new.owner_business_account_id then
        raise exception
            'Its forbidden to change twin.owner_business_account_id';
    end if;

    return new;
end;
$$;
