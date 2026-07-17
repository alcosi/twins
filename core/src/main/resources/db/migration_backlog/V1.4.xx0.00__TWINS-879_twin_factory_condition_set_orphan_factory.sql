-- Data fix that MUST run before V1.4.320.01 adds the
--   twin_factory_condition_set.twin_factory_id -> twin_factory(id)
-- foreign key and the "condition set belongs to the same factory" triggers.
--
-- It enforces the new invariant ONE condition set -> ONE factory on existing data:
--
--  * Orphan owner: a twin_factory_condition_set whose twin_factory_id points to a
--    twin_factory that no longer exists. The real owner is inferred from the rows
--    that USE the set (branch / eraser / pipeline / trigger / multiplier_filter /
--    pipeline_step) and twin_factory_id is rewritten with it. Those referencing
--    columns are themselves FK-backed to twin_factory, so any inferred owner is
--    guaranteed to exist -> the FK in V1.4.320.01 will pass afterwards.
--
--  * Shared set: a set referenced by several different factories. The original set
--    stays with one factory; for every other factory a full copy of the set (the
--    twin_factory_condition_set row plus all its child twin_factory_condition rows)
--    is created and that factory's references are repointed to the copy. Result:
--    each factory owns its own set.
--
-- Follows the TWINS convention: business logic lives in a stored procedure, it
-- reads/writes the DB so it is NOT immutable, NULL is compared via IS DISTINCT FROM.

create or replace procedure fix_twin_factory_condition_set_orphan_factory()
    language plpgsql
as $$
declare
    v_cs          record;
    v_candidates  uuid[];
    v_keep        uuid;
    v_factory     uuid;
    v_new_set_id  uuid;
    v_fixed_count int    := 0;
    v_dup_count   int    := 0;
    v_deleted_count int  := 0;
begin
    for v_cs in
        select cs.id, cs.twin_factory_id as cur_factory_id,
               cs.name, cs.description, cs.domain_id, cs.created_by_user_id,
               cs.created_at, cs.cachable
        from twin_factory_condition_set cs
    loop
        -- every distinct factory that references this condition set
        select array_agg(distinct owner_factory_id)
        into v_candidates
        from (
            select b.twin_factory_id as owner_factory_id
                from twin_factory_branch b
                where b.twin_factory_condition_set_id = v_cs.id
            union
            select e.twin_factory_id
                from twin_factory_eraser e
                where e.twin_factory_condition_set_id = v_cs.id
            union
            select p.twin_factory_id
                from twin_factory_pipeline p
                where p.twin_factory_condition_set_id = v_cs.id
            union
            select t.twin_factory_id
                from twin_factory_trigger t
                where t.twin_factory_condition_set_id = v_cs.id
            union
            select m.twin_factory_id
                from twin_factory_multiplier_filter mf
                join twin_factory_multiplier m on m.id = mf.twin_factory_multiplier_id
                where mf.twin_factory_condition_set_id = v_cs.id
            union
            select pp.twin_factory_id
                from twin_factory_pipeline_step ps
                join twin_factory_pipeline pp on pp.id = ps.twin_factory_pipeline_id
                where ps.twin_factory_condition_set_id = v_cs.id
        ) refs
        where owner_factory_id is not null;

        if v_candidates is null or array_length(v_candidates, 1) is null then
            -- nobody references the set: if its declared owner is gone we cannot
            -- infer a real one and the set is useless -> delete it (child rows
            -- first), otherwise it is a harmless unused valid set that we keep
            if not exists (select 1 from twin_factory f where f.id = v_cs.cur_factory_id) then
                delete from twin_factory_condition
                    where twin_factory_condition_set_id = v_cs.id;
                delete from twin_factory_condition_set
                    where id = v_cs.id;
                v_deleted_count := v_deleted_count + 1;
                raise notice 'twin_factory_condition_set %: orphan and unused, deleted (owner % is gone)',
                    v_cs.id, v_cs.cur_factory_id;
            end if;
            continue;
        end if;

        -- keep the original set on its current factory when that factory still
        -- references it, otherwise move it onto the first referencing factory
        if v_cs.cur_factory_id = any (v_candidates) then
            v_keep := v_cs.cur_factory_id;
        else
            v_keep := v_candidates[1];
            update twin_factory_condition_set
            set twin_factory_id = v_keep
            where id = v_cs.id;
            v_fixed_count := v_fixed_count + 1;
            raise notice 'twin_factory_condition_set %: twin_factory_id % -> % (real owner)',
                v_cs.id, v_cs.cur_factory_id, v_keep;
        end if;

        -- every other referencing factory gets its own copy of the set
        foreach v_factory in array v_candidates loop
            if v_factory = v_keep then
                continue;
            end if;

            v_new_set_id := gen_random_uuid();

            insert into twin_factory_condition_set
                (id, name, description, domain_id, created_by_user_id,
                 updated_at, created_at, twin_factory_id, cachable)
            values
                (v_new_set_id, v_cs.name, v_cs.description, v_cs.domain_id, v_cs.created_by_user_id,
                 current_timestamp, v_cs.created_at, v_factory, v_cs.cachable);

            insert into twin_factory_condition
                (id, twin_factory_condition_set_id, conditioner_featurer_id,
                 conditioner_params, invert, active, description)
            select gen_random_uuid(), v_new_set_id, conditioner_featurer_id,
                   conditioner_params, invert, active, description
            from twin_factory_condition
            where twin_factory_condition_set_id = v_cs.id;

            -- repoint this factory's references from the original set to the copy
            update twin_factory_branch
                set twin_factory_condition_set_id = v_new_set_id
                where twin_factory_condition_set_id = v_cs.id
                  and twin_factory_id = v_factory;

            update twin_factory_eraser
                set twin_factory_condition_set_id = v_new_set_id
                where twin_factory_condition_set_id = v_cs.id
                  and twin_factory_id = v_factory;

            update twin_factory_pipeline
                set twin_factory_condition_set_id = v_new_set_id
                where twin_factory_condition_set_id = v_cs.id
                  and twin_factory_id = v_factory;

            update twin_factory_trigger
                set twin_factory_condition_set_id = v_new_set_id
                where twin_factory_condition_set_id = v_cs.id
                  and twin_factory_id = v_factory;

            update twin_factory_multiplier_filter
                set twin_factory_condition_set_id = v_new_set_id
                where twin_factory_condition_set_id = v_cs.id
                  and twin_factory_multiplier_id in (
                        select id from twin_factory_multiplier where twin_factory_id = v_factory);

            update twin_factory_pipeline_step
                set twin_factory_condition_set_id = v_new_set_id
                where twin_factory_condition_set_id = v_cs.id
                  and twin_factory_pipeline_id in (
                        select id from twin_factory_pipeline where twin_factory_id = v_factory);

            v_dup_count := v_dup_count + 1;
            raise notice 'twin_factory_condition_set %: cloned as % for factory %',
                v_cs.id, v_new_set_id, v_factory;
        end loop;
    end loop;

    raise notice 'reassigned % orphan set(s), created % duplicate set(s), deleted % unused orphan set(s)',
        v_fixed_count, v_dup_count, v_deleted_count;
end;
$$;

call fix_twin_factory_condition_set_orphan_factory();

-- one-shot fixer, not needed after the migration
drop procedure fix_twin_factory_condition_set_orphan_factory();
