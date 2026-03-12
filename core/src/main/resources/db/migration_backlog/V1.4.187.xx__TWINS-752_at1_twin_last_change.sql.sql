-- twin_last_change maintenance integrated into existing twin wrapper pattern:
-- existing triggers on `twin` call `twin_after_insert_wrapper()` / `twin_after_update_wrapper()`.
-- We keep core functions and add PERFORM calls inside those wrappers (without changing their business logic).

create or replace function twin_last_change_after_insert(p_twin_id uuid)
    returns void
    language plpgsql
as
$$
begin
    insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
    values
        (p_twin_id, '00000000-0000-0000-0011-000000000003'::uuid, now()), -- base_name
        (p_twin_id, '00000000-0000-0000-0011-000000000004'::uuid, now()), -- base_description
        (p_twin_id, '00000000-0000-0000-0011-000000000005'::uuid, now()), -- base_external_id
        (p_twin_id, '00000000-0000-0000-0011-000000000006'::uuid, now()), -- base_owner_user
        (p_twin_id, '00000000-0000-0000-0011-000000000007'::uuid, now()), -- base_assignee_user
        (p_twin_id, '00000000-0000-0000-0011-000000000008'::uuid, now()), -- base_creator_user
        (p_twin_id, '00000000-0000-0000-0011-000000000009'::uuid, now()), -- base_head
        (p_twin_id, '00000000-0000-0000-0011-000000000010'::uuid, now()), -- base_status
        (p_twin_id, '00000000-0000-0000-0011-000000000011'::uuid, now())  -- base_created_at
    on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
end;
$$;

create or replace function twin_last_change_after_update(
    p_twin_id uuid,
    p_old_name varchar,
    p_new_name varchar,
    p_old_description varchar,
    p_new_description varchar,
    p_old_external_id varchar,
    p_new_external_id varchar,
    p_old_owner_user_id uuid,
    p_new_owner_user_id uuid,
    p_old_assigner_user_id uuid,
    p_new_assigner_user_id uuid,
    p_old_created_by_user_id uuid,
    p_new_created_by_user_id uuid,
    p_old_head_twin_id uuid,
    p_new_head_twin_id uuid,
    p_old_twin_status_id uuid,
    p_new_twin_status_id uuid
)
    returns void
    language plpgsql
as
$$
begin
    if (p_old_name is distinct from p_new_name) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (p_twin_id, '00000000-0000-0000-0011-000000000003'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (p_old_description is distinct from p_new_description) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (p_twin_id, '00000000-0000-0000-0011-000000000004'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (p_old_external_id is distinct from p_new_external_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (p_twin_id, '00000000-0000-0000-0011-000000000005'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (p_old_owner_user_id is distinct from p_new_owner_user_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (p_twin_id, '00000000-0000-0000-0011-000000000006'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (p_old_assigner_user_id is distinct from p_new_assigner_user_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (p_twin_id, '00000000-0000-0000-0011-000000000007'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (p_old_created_by_user_id is distinct from p_new_created_by_user_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (p_twin_id, '00000000-0000-0000-0011-000000000008'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (p_old_head_twin_id is distinct from p_new_head_twin_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (p_twin_id, '00000000-0000-0000-0011-000000000009'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (p_old_twin_status_id is distinct from p_new_twin_status_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (p_twin_id, '00000000-0000-0000-0011-000000000010'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;
end;
$$;

-- Extend existing twin wrappers: add only twin_last_change calls
create or replace function twin_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN
    RAISE NOTICE 'Process insert for: %', new.id;
    PERFORM hierarchyUpdateTreeHard(new.id, hierarchyDetectTree(new.id));

    IF NEW.assigner_user_id IS NOT NULL THEN
        PERFORM set_config('app.user_group_involve_by_assignee_propagation_trigger', 'on', true); -- function has direct call protection
        PERFORM user_group_involve_by_assignee_propagation(NEW.assigner_user_id, null, NEW.twin_class_id, null, NEW.twin_status_id, null, NEW.owner_business_account_id);
        PERFORM set_config('app.user_group_involve_by_assignee_propagation_trigger', 'off', true);
    END IF;

    IF NEW.head_twin_id IS NOT NULL THEN
        PERFORM update_twin_head_direct_children_counter(NEW.head_twin_id);
    END IF;

    -- Update twin_class twin counter
    IF NEW.twin_class_id IS NOT NULL THEN
        PERFORM update_twin_class_twin_counter(NEW.twin_class_id);
    END IF;

    -- twin_last_change
    PERFORM twin_last_change_after_insert(NEW.id);

    RETURN NEW;
END;
$$;

create or replace function twin_after_update_wrapper() returns trigger
    language plpgsql
as
$$
BEGIN

    IF NEW.assigner_user_id IS DISTINCT FROM OLD.assigner_user_id
           or NEW.twin_class_id IS DISTINCT FROM OLD.twin_class_id
           or NEW.twin_status_id IS DISTINCT FROM OLd.twin_status_id THEN
        PERFORM set_config('app.user_group_involve_by_assignee_propagation_trigger', 'on', true); -- function has direct call protection
        PERFORM user_group_involve_by_assignee_propagation(NEW.assigner_user_id, old.assigner_user_id,NEW.twin_class_id, old.twin_class_id, NEW.twin_status_id, old.twin_status_id, NEW.owner_business_account_id);
        PERFORM set_config('app.user_group_involve_by_assignee_propagation_trigger', 'off', true);
    END IF;

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

    -- Update twin_class twin counters if twin_class_id changed
    IF OLD.twin_class_id IS DISTINCT FROM NEW.twin_class_id THEN
        IF OLD.twin_class_id IS NOT NULL THEN
            PERFORM update_twin_class_twin_counter(OLD.twin_class_id);
        END IF;
        IF NEW.twin_class_id IS NOT NULL THEN
            PERFORM update_twin_class_twin_counter(NEW.twin_class_id);
        END IF;
    END IF;

    -- twin_last_change
    PERFORM twin_last_change_after_update(
        NEW.id,
        OLD.name, NEW.name,
        OLD.description, NEW.description,
        OLD.external_id, NEW.external_id,
        OLD.owner_user_id, NEW.owner_user_id,
        OLD.assigner_user_id, NEW.assigner_user_id,
        OLD.created_by_user_id, NEW.created_by_user_id,
        OLD.head_twin_id, NEW.head_twin_id,
        OLD.twin_status_id, NEW.twin_status_id
    );

    RETURN NEW;
END;
$$;
