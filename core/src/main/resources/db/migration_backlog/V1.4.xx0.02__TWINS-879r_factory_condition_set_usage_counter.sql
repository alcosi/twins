-- Adds per-table usage counters to twin_factory_condition_set: how many times the
-- set is referenced from each of the 6 factory sub-entity tables. The counters are
-- seeded from current data by a one-shot procedure and then kept up to date by
-- AFTER insert/update/delete triggers on every referencing table.
--
-- Follows the TWINS wrapper-functions convention:
--   * counter maintenance is a side-effect -> AFTER triggers
--   * business logic lives in a stored procedure (twin_factory_condition_set_usage_adjust)
--   * thin per-table/per-operation wrapper functions delegate to it via PERFORM

-- 1. Counter columns
alter table twin_factory_condition_set
    add column usage_count_branch            integer not null default 0,
    add column usage_count_eraser            integer not null default 0,
    add column usage_count_pipeline          integer not null default 0,
    add column usage_count_trigger           integer not null default 0,
    add column usage_count_multiplier_filter integer not null default 0,
    add column usage_count_pipeline_step     integer not null default 0;

-- 2. Seed the counters from existing data (one-shot procedure)
create or replace procedure init_twin_factory_condition_set_usage_counts()
    language plpgsql
as $$
declare
    v_id uuid;
begin
    -- each set gets its own counts computed from its own references
    for v_id in select id from twin_factory_condition_set loop
        update twin_factory_condition_set
        set usage_count_branch =
                (select count(*)::int from twin_factory_branch            where twin_factory_condition_set_id = v_id),
            usage_count_eraser =
                (select count(*)::int from twin_factory_eraser            where twin_factory_condition_set_id = v_id),
            usage_count_pipeline =
                (select count(*)::int from twin_factory_pipeline          where twin_factory_condition_set_id = v_id),
            usage_count_trigger =
                (select count(*)::int from twin_factory_trigger           where twin_factory_condition_set_id = v_id),
            usage_count_multiplier_filter =
                (select count(*)::int from twin_factory_multiplier_filter where twin_factory_condition_set_id = v_id),
            usage_count_pipeline_step =
                (select count(*)::int from twin_factory_pipeline_step     where twin_factory_condition_set_id = v_id)
        where id = v_id;
    end loop;
end;
$$;

call init_twin_factory_condition_set_usage_counts();

drop procedure init_twin_factory_condition_set_usage_counts();

-- 3. Business-logic procedure: applies a delta to the counter column that matches
-- the source table. Reads/writes the DB, so it is NOT IMMUTABLE.
create or replace function twin_factory_condition_set_usage_adjust(
    p_condition_set_id uuid,
    p_source           text,
    p_delta            int
) returns void as $$
begin
    if p_condition_set_id is null or p_delta = 0 then
        return;
    end if;

    update twin_factory_condition_set
    set usage_count_branch            = usage_count_branch            + case when p_source = 'branch'            then p_delta else 0 end,
        usage_count_eraser            = usage_count_eraser            + case when p_source = 'eraser'            then p_delta else 0 end,
        usage_count_pipeline          = usage_count_pipeline          + case when p_source = 'pipeline'          then p_delta else 0 end,
        usage_count_trigger           = usage_count_trigger           + case when p_source = 'trigger'           then p_delta else 0 end,
        usage_count_multiplier_filter = usage_count_multiplier_filter + case when p_source = 'multiplier_filter' then p_delta else 0 end,
        usage_count_pipeline_step     = usage_count_pipeline_step     + case when p_source = 'pipeline_step'     then p_delta else 0 end
    where id = p_condition_set_id;
end;
$$ language plpgsql;

-- 4. Remove old triggers
drop trigger if exists twin_factory_branch_after_insert_wrapper_trigger on twin_factory_branch;
drop trigger if exists twin_factory_branch_after_update_wrapper_trigger on twin_factory_branch;
drop trigger if exists twin_factory_branch_after_delete_wrapper_trigger on twin_factory_branch;
drop trigger if exists twin_factory_eraser_after_insert_wrapper_trigger on twin_factory_eraser;
drop trigger if exists twin_factory_eraser_after_update_wrapper_trigger on twin_factory_eraser;
drop trigger if exists twin_factory_eraser_after_delete_wrapper_trigger on twin_factory_eraser;
drop trigger if exists twin_factory_pipeline_after_insert_wrapper_trigger on twin_factory_pipeline;
drop trigger if exists twin_factory_pipeline_after_update_wrapper_trigger on twin_factory_pipeline;
drop trigger if exists twin_factory_pipeline_after_delete_wrapper_trigger on twin_factory_pipeline;
drop trigger if exists twin_factory_trigger_after_insert_wrapper_trigger on twin_factory_trigger;
drop trigger if exists twin_factory_trigger_after_update_wrapper_trigger on twin_factory_trigger;
drop trigger if exists twin_factory_trigger_after_delete_wrapper_trigger on twin_factory_trigger;
drop trigger if exists twin_factory_multiplier_filter_after_insert_wrapper_trigger on twin_factory_multiplier_filter;
drop trigger if exists twin_factory_multiplier_filter_after_update_wrapper_trigger on twin_factory_multiplier_filter;
drop trigger if exists twin_factory_multiplier_filter_after_delete_wrapper_trigger on twin_factory_multiplier_filter;
drop trigger if exists twin_factory_pipeline_step_after_insert_wrapper_trigger on twin_factory_pipeline_step;
drop trigger if exists twin_factory_pipeline_step_after_update_wrapper_trigger on twin_factory_pipeline_step;
drop trigger if exists twin_factory_pipeline_step_after_delete_wrapper_trigger on twin_factory_pipeline_step;

-- 5. Wrapper functions (one per table per operation)

-- twin_factory_branch
create or replace function twin_factory_branch_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'branch', 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_branch_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'branch', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'branch', 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_branch_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'branch', -1);
    return old;
end;
$$ language plpgsql;

-- twin_factory_eraser
create or replace function twin_factory_eraser_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'eraser', 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_eraser_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'eraser', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'eraser', 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_eraser_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'eraser', -1);
    return old;
end;
$$ language plpgsql;

-- twin_factory_pipeline
create or replace function twin_factory_pipeline_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'pipeline', 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_pipeline_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'pipeline', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'pipeline', 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_pipeline_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'pipeline', -1);
    return old;
end;
$$ language plpgsql;

-- twin_factory_trigger
create or replace function twin_factory_trigger_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'trigger', 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_trigger_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'trigger', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'trigger', 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_trigger_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'trigger', -1);
    return old;
end;
$$ language plpgsql;

-- twin_factory_multiplier_filter
create or replace function twin_factory_multiplier_filter_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'multiplier_filter', 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_multiplier_filter_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'multiplier_filter', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'multiplier_filter', 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_multiplier_filter_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'multiplier_filter', -1);
    return old;
end;
$$ language plpgsql;

-- twin_factory_pipeline_step
create or replace function twin_factory_pipeline_step_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'pipeline_step', 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_pipeline_step_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'pipeline_step', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'pipeline_step', 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_pipeline_step_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'pipeline_step', -1);
    return old;
end;
$$ language plpgsql;

-- 6. Triggers

create trigger twin_factory_branch_after_insert_wrapper_trigger
    after insert on twin_factory_branch
    for each row execute function twin_factory_branch_after_insert_wrapper();
create trigger twin_factory_branch_after_update_wrapper_trigger
    after update on twin_factory_branch
    for each row execute function twin_factory_branch_after_update_wrapper();
create trigger twin_factory_branch_after_delete_wrapper_trigger
    after delete on twin_factory_branch
    for each row execute function twin_factory_branch_after_delete_wrapper();

create trigger twin_factory_eraser_after_insert_wrapper_trigger
    after insert on twin_factory_eraser
    for each row execute function twin_factory_eraser_after_insert_wrapper();
create trigger twin_factory_eraser_after_update_wrapper_trigger
    after update on twin_factory_eraser
    for each row execute function twin_factory_eraser_after_update_wrapper();
create trigger twin_factory_eraser_after_delete_wrapper_trigger
    after delete on twin_factory_eraser
    for each row execute function twin_factory_eraser_after_delete_wrapper();

create trigger twin_factory_pipeline_after_insert_wrapper_trigger
    after insert on twin_factory_pipeline
    for each row execute function twin_factory_pipeline_after_insert_wrapper();
create trigger twin_factory_pipeline_after_update_wrapper_trigger
    after update on twin_factory_pipeline
    for each row execute function twin_factory_pipeline_after_update_wrapper();
create trigger twin_factory_pipeline_after_delete_wrapper_trigger
    after delete on twin_factory_pipeline
    for each row execute function twin_factory_pipeline_after_delete_wrapper();

create trigger twin_factory_trigger_after_insert_wrapper_trigger
    after insert on twin_factory_trigger
    for each row execute function twin_factory_trigger_after_insert_wrapper();
create trigger twin_factory_trigger_after_update_wrapper_trigger
    after update on twin_factory_trigger
    for each row execute function twin_factory_trigger_after_update_wrapper();
create trigger twin_factory_trigger_after_delete_wrapper_trigger
    after delete on twin_factory_trigger
    for each row execute function twin_factory_trigger_after_delete_wrapper();

create trigger twin_factory_multiplier_filter_after_insert_wrapper_trigger
    after insert on twin_factory_multiplier_filter
    for each row execute function twin_factory_multiplier_filter_after_insert_wrapper();
create trigger twin_factory_multiplier_filter_after_update_wrapper_trigger
    after update on twin_factory_multiplier_filter
    for each row execute function twin_factory_multiplier_filter_after_update_wrapper();
create trigger twin_factory_multiplier_filter_after_delete_wrapper_trigger
    after delete on twin_factory_multiplier_filter
    for each row execute function twin_factory_multiplier_filter_after_delete_wrapper();

create trigger twin_factory_pipeline_step_after_insert_wrapper_trigger
    after insert on twin_factory_pipeline_step
    for each row execute function twin_factory_pipeline_step_after_insert_wrapper();
create trigger twin_factory_pipeline_step_after_update_wrapper_trigger
    after update on twin_factory_pipeline_step
    for each row execute function twin_factory_pipeline_step_after_update_wrapper();
create trigger twin_factory_pipeline_step_after_delete_wrapper_trigger
    after delete on twin_factory_pipeline_step
    for each row execute function twin_factory_pipeline_step_after_delete_wrapper();
