-- twin_last_change maintenance in wrapper-trigger pattern (without touching existing twin wrappers):
-- - core functions (insert/update)
-- - after-insert/after-update wrappers on table twin

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

create or replace function twin_last_change_after_insert_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform twin_last_change_after_insert(new.id);
    return new;
end;
$$;

create or replace function twin_last_change_after_update_wrapper()
    returns trigger
    language plpgsql
as
$$
begin
    perform twin_last_change_after_update(
        new.id,
        old.name, new.name,
        old.description, new.description,
        old.external_id, new.external_id,
        old.owner_user_id, new.owner_user_id,
        old.assigner_user_id, new.assigner_user_id,
        old.created_by_user_id, new.created_by_user_id,
        old.head_twin_id, new.head_twin_id,
        old.twin_status_id, new.twin_status_id
    );
    return new;
end;
$$;

drop trigger if exists twin_last_change_after_insert_wrapper_trigger on twin;
create trigger twin_last_change_after_insert_wrapper_trigger
    after insert
    on twin
    for each row
execute procedure twin_last_change_after_insert_wrapper();

drop trigger if exists twin_last_change_after_update_wrapper_trigger on twin;
create trigger twin_last_change_after_update_wrapper_trigger
    after update
    on twin
    for each row
execute procedure twin_last_change_after_update_wrapper();

