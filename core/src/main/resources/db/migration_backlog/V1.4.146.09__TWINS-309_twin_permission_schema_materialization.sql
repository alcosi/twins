ALTER TABLE twin ADD COLUMN IF NOT EXISTS permission_schema_id UUID DEFAULT null;

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
        permission_schema_id = COALESCE(s.permission_schema_id, dbu.permission_schema_id, d.permission_schema_id)
    FROM twin_class tc
        JOIN domain d ON tc.domain_id = d.id
        LEFT JOIN domain_business_account dbu ON dbu.domain_id = d.id and dbu.business_account_id is not distinct from t.owner_business_account_id
        LEFT JOIN space s ON s.twin_id is not distinct from data_to_use.permission_schema_space_id
    WHERE t.id = p_twin_id and t.twin_class_id = tc.id;

    -- update hier. and schemas for twin-in children and their children, recursively
    WITH RECURSIVE descendants AS (
        SELECT id, 1 AS depth
        FROM twin
        WHERE head_twin_id = p_twin_id
        UNION ALL
        SELECT t.id, d.depth + 1
        FROM twin t
                 INNER JOIN descendants d ON t.head_twin_id = d.id
        WHERE d.depth < 10
    ), updated_data AS (
        SELECT dt.id, (hierarchyDetectTree(dt.id)).* -- use function and expand result
        FROM descendants dt
    )
    UPDATE twin t
    SET hierarchy_tree = text2ltree(ud.hierarchy),
        permission_schema_space_id = ud.permission_schema_space_id,
        twinflow_schema_space_id = ud.twinflow_schema_space_id,
        twin_class_schema_space_id = ud.twin_class_schema_space_id,
        alias_space_id = ud.alias_space_id,
        permission_schema_id = COALESCE(s.permission_schema_id, dbu.permission_schema_id, d.permission_schema_id)
    FROM updated_data ud
        JOIN twin_class tc ON t.twin_class_id = tc.id
        JOIN domain d ON tc.domain_id = d.id
        LEFT JOIN domain_business_account dbu ON dbu.domain_id = d.id and dbu.business_account_id is not distinct from t.owner_business_account_id
        LEFT JOIN space s ON s.twin_id is not distinct from data_to_use.permission_schema_space_id
    WHERE t.id = ud.id;
END;
$$;

-- update all twins
UPDATE twin t
SET permission_schema_id = COALESCE(s.permission_schema_id, dbu.permission_schema_id, d.permission_schema_id)
FROM twin_class tc
         JOIN domain d ON tc.domain_id = d.id
         LEFT JOIN domain_business_account dbu ON dbu.domain_id = d.id
         LEFT JOIN space s ON true -- Здесь мы не можем сослаться на t, поэтому используем фильтр ниже
WHERE t.twin_class_id = tc.id
  AND (dbu.business_account_id IS NOT DISTINCT FROM t.owner_business_account_id OR dbu.business_account_id IS NULL)
  AND (s.twin_id IS NOT DISTINCT FROM t.permission_schema_space_id OR s.twin_id IS NULL);



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


create function domain_business_account_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    IF OLD.permission_schema_id IS DISTINCT FROM NEW.permission_schema_id THEN
        PERFORM
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
