-- Generalize notification_context -> context (usable by faces, not only notifications),
-- notification_context_collector -> context_collector,
-- and replace face_wt001.search_target_twin_pointer_id with face_wt001.context_id.
-- Idempotent: every step is guarded, so a re-run (e.g. after a partial manual application)
-- converges to the same state instead of failing on already-applied renames.
-- to_regclass resolves via search_path and returns null for a missing table.

-- 1. table renames (no-op once renamed)
alter table if exists notification_context rename to context;
alter table if exists notification_context_collector rename to context_collector;

-- 2. column renames (guarded by old-column existence per table)
do $$
declare
    r record;
begin
    for r in
        select * from (values
            ('notification_channel_event', 'notification_context_id', 'context_id'),
            ('context_collector', 'notification_context_id', 'context_id')
        ) as v(tbl, old_name, new_name)
    loop
        if exists (select 1 from pg_attribute
                   where attrelid = to_regclass(r.tbl) and attname = r.old_name and not attisdropped) then
            execute format('alter table %I rename column %I to %I', r.tbl, r.old_name, r.new_name);
        end if;
    end loop;
end
$$;

-- 3. constraint renames (guarded by old-constraint existence per table)
do $$
declare
    r record;
begin
    for r in
        select * from (values
            ('context', 'notification_context_pk', 'context_pk'),
            ('context', 'notification_context_domain_id_fk', 'context_domain_id_fk'),
            ('context', 'notification_context_name_i18n_id_fk', 'context_name_i18n_id_fk'),
            ('context', 'notification_context_description_i18n_id_fk', 'context_description_i18n_id_fk'),
            ('context_collector', 'notification_context_collector_pk', 'context_collector_pk'),
            ('context_collector', 'notification_context_collector_context_id_fk', 'context_collector_context_id_fk'),
            ('context_collector', 'notification_context_collector_featurer_id_fk', 'context_collector_featurer_id_fk'),
            ('notification_channel_event', 'notification_channel_event_notification_context_id_fk', 'notification_channel_event_context_id_fk')
        ) as v(tbl, old_name, new_name)
    loop
        if exists (select 1 from pg_constraint
                   where conrelid = to_regclass(r.tbl) and conname = r.old_name) then
            execute format('alter table %I rename constraint %I to %I', r.tbl, r.old_name, r.new_name);
        end if;
    end loop;
end
$$;

-- 4. index renames (no-op once renamed)
alter index if exists idx_notification_context_domain_id rename to idx_context_domain_id;
alter index if exists idx_notification_context_name_i18n_id rename to idx_context_name_i18n_id;
alter index if exists idx_notification_context_description_i18n_id rename to idx_context_description_i18n_id;
alter index if exists idx_notification_context_collector_context_id rename to idx_context_collector_context_id;
alter index if exists idx_notification_context_collector_context_collector_featurer_id rename to idx_context_collector_featurer_id;
alter index if exists idx_notification_channel_event_notification_context_id rename to idx_notification_channel_event_context_id;

-- 5. face_wt001: new context_id column (optional; context deletion only detaches the widget)
alter table face_wt001
    add column if not exists context_id uuid
        constraint face_wt001_context_id_fk
            references context
            on update cascade on delete set null;

create index if not exists idx_face_wt001_context_id on face_wt001 (context_id);

-- 6. i18n type for auto-generated context names
insert into i18n_type (id, name) values ('contextName', 'Context name') on conflict (id) do nothing;

-- 7. data migration: face_wt001.search_target_twin_pointer_id -> context + context_collector
-- (featurer 4912 = ContextCollectorPointedTwin; params reproduce the old hardcoded behavior:
-- searchParams["twinId"] = pointed twin id). Skipped entirely once the column is dropped;
-- rows already pointing to a context are not re-migrated.
do $$
declare
    r record;
    v_i18n_id uuid;
    v_context_id uuid;
begin
    if not exists (select 1 from pg_attribute
                   where attrelid = 'face_wt001'::regclass
                     and attname = 'search_target_twin_pointer_id' and not attisdropped) then
        return;
    end if;
    for r in
        select w.id as wt001_id, w.search_target_twin_pointer_id as pointer_id, f.domain_id
        from face_wt001 w
        join face f on f.id = w.face_id
        where w.search_target_twin_pointer_id is not null
          and w.context_id is null
    loop
        if r.domain_id is null then
            raise notice 'face_wt001[%] skipped: face has null domain_id', r.wt001_id;
            continue;
        end if;
        v_i18n_id := gen_random_uuid();
        insert into i18n (id, name, i18n_type_id) values (v_i18n_id, 'WT001 search target', 'contextName');
        v_context_id := gen_random_uuid();
        insert into context (id, domain_id, name_i18n_id) values (v_context_id, r.domain_id, v_i18n_id);
        insert into context_collector (id, context_id, context_collector_featurer_id, context_collector_params)
        values (gen_random_uuid(), v_context_id, 4912,
                hstore('twinPointerId', r.pointer_id::text)
                || hstore('collectId', 'true')
                || hstore('collectIdKey', 'twinId'));
        update face_wt001 set context_id = v_context_id where id = r.wt001_id;
    end loop;
end
$$;

-- 8. drop old column (its fk constraint and index are dropped with it)
alter table face_wt001 drop column if exists search_target_twin_pointer_id;
