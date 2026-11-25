-- TWINS-949: rename face_navbar_nb001* tables to face_nb001* (drop the redundant "navbar" segment),
-- including the two columns carrying the old prefix, plus their constraints and indexes.
-- Idempotent: every rename is guarded by an existence check, so a rerun skips what is already renamed
-- (style mirrors V1.4.394.08 face_tw* -> face_wt* rename).
-- Note: the counter_twin_search_id FK/index come from V1.4.xxx.01 (TWINS-949), applied before this script.
-- Unprefixed legacy names unrelated to the renamed columns (fk_menu_items_permission,
-- idx_menu_items_permission) are left untouched.

do $$
declare
    m record;
    r record;
begin
    -- 1. tables
    for m in select * from (values
        ('face_navbar_nb001', 'face_nb001'),
        ('face_navbar_nb001_menu_item', 'face_nb001_menu_item'),
        ('face_navbar_nb001_menu_item_status', 'face_nb001_menu_item_status')
    ) as t(old_name, new_name) loop
        if exists (select from pg_tables where schemaname = current_schema() and tablename = m.old_name) then
            execute format('alter table %I rename to %I', m.old_name, m.new_name);
        end if;
    end loop;

    -- 2. columns carrying the old prefix
    for m in select * from (values
        ('face_nb001_menu_item', 'face_navbar_nb001_status_id', 'face_nb001_status_id'),
        ('face_nb001_menu_item', 'parent_face_navbar_nb001_menu_item_id', 'parent_face_nb001_menu_item_id')
    ) as t(tbl, old_col, new_col) loop
        if exists (select from information_schema.columns cl
                   where cl.table_schema = current_schema() and cl.table_name = m.tbl and cl.column_name = m.old_col) then
            execute format('alter table %I rename column %I to %I', m.tbl, m.old_col, m.new_col);
        end if;
    end loop;

    -- 3. constraints and indexes (postgres does not rename them together with the table/column).
    -- replace() affects all occurrences, so names containing the renamed columns are covered too.
    for r in select indexname from pg_indexes
             where schemaname = current_schema() and indexname like '%face_navbar_nb001%' loop
        execute format('alter index %I rename to %I', r.indexname, replace(r.indexname, 'face_navbar_nb001', 'face_nb001'));
    end loop;
    for r in select t.relname as tablename, c.conname from pg_constraint c
             join pg_class t on t.oid = c.conrelid
             join pg_namespace n on n.oid = t.relnamespace
             where n.nspname = current_schema() and c.conname like '%face_navbar_nb001%' loop
        execute format('alter table %I rename constraint %I to %I', r.tablename, r.conname, replace(r.conname, 'face_navbar_nb001', 'face_nb001'));
    end loop;

    -- 4. names the replace loop cannot fully handle: the parent-FK index was truncated at 63 chars
    -- (step 3 turns it into idx_face_nb001_menu_item_parent_face_nb001_menu_i), and the parent-FK
    -- constraint never carried the prefix. Map them to the full conventional form
    -- (with the shorter names they now fit the limit).
    for m in select * from (values
        ('idx_face_nb001_menu_item_parent_face_nb001_menu_i', 'idx_face_nb001_menu_item_parent_face_nb001_menu_item_id')
    ) as t(old_name, new_name) loop
        if exists (select from pg_indexes where schemaname = current_schema() and indexname = m.old_name) then
            execute format('alter index %I rename to %I', m.old_name, m.new_name);
        end if;
    end loop;
    if exists (select from pg_constraint c
               join pg_class t on t.oid = c.conrelid
               join pg_namespace n on n.oid = t.relnamespace
               where n.nspname = current_schema() and t.relname = 'face_nb001_menu_item' and c.conname = 'fk_menu_item_parent') then
        alter table face_nb001_menu_item rename constraint fk_menu_item_parent to face_nb001_menu_item_parent_face_nb001_menu_item_id_fk;
    end if;
end $$;
