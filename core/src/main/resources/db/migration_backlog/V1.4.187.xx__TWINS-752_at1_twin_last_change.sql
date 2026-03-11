create table if not exists twin_last_change (
    twin_id uuid not null,
    twin_class_field_id uuid not null,
    last_changed_at timestamp not null,
    constraint twin_last_change_pk
        primary key (twin_id, twin_class_field_id),
    constraint twin_last_change_twin_fk
        foreign key (twin_id)
            references twin(id)
            on update cascade
            on delete cascade,
    constraint twin_last_change_field_fk
        foreign key (twin_class_field_id)
            references twin_class_field(id)
            on update cascade
            on delete cascade
);

create index if not exists idx_twin_last_change_field_time
    on twin_last_change (twin_class_field_id, last_changed_at);

create or replace function twin_last_change_track()
    returns trigger
    language plpgsql
as
$$
declare
    v_twin_id uuid;
begin
    v_twin_id := new.id;

    if (tg_op = 'INSERT') then
        -- base_name
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000003'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;

        -- base_description
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000004'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;

        -- base_external_id
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000005'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;

        -- base_owner_user
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000006'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;

        -- base_assignee_user
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000007'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;

        -- base_creator_user
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000008'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;

        -- base_head
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000009'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;

        -- base_status
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000010'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;

        -- base_created_at
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000011'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;

        return null;
    end if;

    -- UPDATE: upsert only when tracked fields actually changed
    if (old.name is distinct from new.name) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000003'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (old.description is distinct from new.description) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000004'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (old.external_id is distinct from new.external_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000005'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (old.owner_user_id is distinct from new.owner_user_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000006'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (old.assigner_user_id is distinct from new.assigner_user_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000007'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (old.created_by_user_id is distinct from new.created_by_user_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000008'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (old.head_twin_id is distinct from new.head_twin_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000009'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    if (old.twin_status_id is distinct from new.twin_status_id) then
        insert into twin_last_change (twin_id, twin_class_field_id, last_changed_at)
        values (v_twin_id, '00000000-0000-0000-0011-000000000010'::uuid, now())
        on conflict (twin_id, twin_class_field_id) do update
        set last_changed_at = excluded.last_changed_at;
    end if;

    return null;
end;
$$;

drop trigger if exists twin_last_change_track_trigger on twin;
create trigger twin_last_change_track_trigger
    after insert or update
    on twin
    for each row
execute function twin_last_change_track();

