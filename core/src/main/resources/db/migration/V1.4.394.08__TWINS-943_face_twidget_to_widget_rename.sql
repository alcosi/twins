-- TWINS-943 Phase 3: merge the twidget (TW) family into the widget (WT) family.
-- Data-preserving renames: ex-TW widgets fill the free WT004..WT007 gap and continue after WT008,
-- so the WT numbering becomes continuous WT001..WT010:
--   TW001 gallery -> WT004, TW002 accordion -> WT005, TW004 fields -> WT006,
--   TW005 transitions -> WT007, TW006 actions -> WT009, TW007 class change -> WT010.
-- The client is updated in the same release, so component ids and paths switch immediately (no Phase 4).

do $$
declare
    m record;
    r record;
begin
    -- 0. pre-check: the target WT ids must be free. A dump/import could already occupy them
    -- (WT008 comments is intentionally not a target - it already exists since .04).
    -- Fail fast with a clear message instead of a PK violation halfway through step 5.
    if exists (select from face_component where id in ('WT004', 'WT005', 'WT006', 'WT007', 'WT009', 'WT010')) then
        raise exception 'TWINS-943: face_component ids are already occupied: %. Resolve the conflict manually, then rerun the migration',
            (select string_agg(id, ', ' order by id) from face_component where id in ('WT004', 'WT005', 'WT006', 'WT007', 'WT009', 'WT010'));
    end if;

    -- 1. tables
    for m in select * from (values
        ('face_tw001', 'face_wt004'),
        ('face_tw002', 'face_wt005'),
        ('face_tw002_accordion_item', 'face_wt005_accordion_item'),
        ('face_tw004', 'face_wt006'),
        ('face_tw005', 'face_wt007'),
        ('face_tw005_button', 'face_wt007_button'),
        ('face_tw006', 'face_wt009'),
        ('face_tw006_action', 'face_wt009_action'),
        ('face_tw007', 'face_wt010')
    ) as t(old_name, new_name) loop
        if exists (select from pg_tables where schemaname = current_schema() and tablename = m.old_name) then
            execute format('alter table %I rename to %I', m.old_name, m.new_name);
        end if;
    end loop;

    -- 2. child FK columns
    for m in select * from (values
        ('face_wt005_accordion_item', 'face_tw002_id', 'face_wt005_id'),
        ('face_wt007_button', 'face_tw005_id', 'face_wt007_id'),
        ('face_wt009_action', 'face_tw006_id', 'face_wt009_id')
    ) as t(tbl, old_col, new_col) loop
        if exists (select from information_schema.columns cl
                   where cl.table_schema = current_schema() and cl.table_name = m.tbl and cl.column_name = m.old_col) then
            execute format('alter table %I rename column %I to %I', m.tbl, m.old_col, m.new_col);
        end if;
    end loop;

    -- 3. indexes and constraints (postgres does not rename them together with the table).
    -- Child names start with the parent prefix, so parent mappings cover them (replace affects all occurrences).
    for m in select * from (values
        ('face_tw001', 'face_wt004'),
        ('face_tw002', 'face_wt005'),
        ('face_tw004', 'face_wt006'),
        ('face_tw005', 'face_wt007'),
        ('face_tw006', 'face_wt009'),
        ('face_tw007', 'face_wt010')
    ) as t(old_name, new_name) loop
        for r in select indexname from pg_indexes
                 where schemaname = current_schema() and starts_with(indexname, m.old_name) loop
            execute format('alter index %I rename to %I', r.indexname, replace(r.indexname, m.old_name, m.new_name));
        end loop;
        for r in select t.relname as tablename, c.conname from pg_constraint c
                 join pg_class t on t.oid = c.conrelid
                 join pg_namespace n on n.oid = t.relnamespace
                 where n.nspname = current_schema() and starts_with(c.conname, m.old_name) loop
            execute format('alter table %I rename constraint %I to %I', r.tablename, r.conname, replace(r.conname, m.old_name, m.new_name));
        end loop;
    end loop;

    -- 3b. indexes the prefix loop above cannot fully handle: idx_-prefixed FK indexes
    -- (V1.4.97.01) do not start with the table name, and the TW006 action index carries
    -- a legacy underscore glitch (face_tw_006_id) that prefix replace leaves inside the name.
    for m in select * from (values
        ('idx_face_tw001_target_twin_pointer_id', 'idx_face_wt004_target_twin_pointer_id'),
        ('idx_face_tw002_target_twin_pointer_id', 'idx_face_wt005_target_twin_pointer_id'),
        ('idx_face_tw004_target_twin_pointer_id', 'idx_face_wt006_target_twin_pointer_id'),
        ('idx_face_tw005_target_twin_pointer_id', 'idx_face_wt007_target_twin_pointer_id'),
        ('idx_face_tw005_twin_pointer_validator_rule_id', 'idx_face_wt007_twin_pointer_validator_rule_id'),
        ('idx_face_tw007_face_id', 'idx_face_wt010_face_id'),
        ('face_tw006_action_face_tw_006_id_idx', 'face_wt009_action_face_wt009_id_idx'),
        ('face_wt009_action_face_tw_006_id_idx', 'face_wt009_action_face_wt009_id_idx')
    ) as t(old_name, new_name) loop
        if exists (select from pg_indexes where schemaname = current_schema() and indexname = m.old_name) then
            execute format('alter index %I rename to %I', m.old_name, m.new_name);
        end if;
    end loop;
end $$;

-- 4. component type merge: TWIDGET -> WIDGET (the type is not exposed via the API).
-- The TWIDGET type row is kept (marked deprecated) for compatibility with data dumps referencing it.
update face_component set face_component_type_id = 'WIDGET' where face_component_type_id = 'TWIDGET';
update face_component_type set description = 'Deprecated: merged into WIDGET (TWINS-943), kept for dump compatibility' where id = 'TWIDGET';

-- 5. component id switch: TWxxx -> WTxxx (face.face_component_id cascades via face_face_component_id_fk on update cascade)
update face_component set id = 'WT004' where id = 'TW001';
update face_component set id = 'WT005' where id = 'TW002';
update face_component set id = 'WT006' where id = 'TW004';
update face_component set id = 'WT007' where id = 'TW005';
update face_component set id = 'WT009' where id = 'TW006';
update face_component set id = 'WT010' where id = 'TW007';

-- 6. registry gap fix: the messages widget (face_wt003, since V1.3.245.02) never got a face_component
-- row - the original WT003 gallery row was renamed to TW001 back in V1.3.204.01. The face.face_component_id
-- FK makes the row mandatory for any messages face, so register it now.
insert into face_component (id, face_component_type_id, name, description)
values ('WT003', 'WIDGET', 'Twin messages widget', null) on conflict do nothing;
