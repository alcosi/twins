-- Materializes 2 child counters:
--   * twin_factory_pipeline.factory_pipeline_steps_count      = count of twin_factory_pipeline_step
--   * twin_factory_multiplier.factory_multiplier_filters_count = count of twin_factory_multiplier_filter
-- maintained by AFTER insert/update/delete triggers. These counters used to be computed on the
-- fly via COUNT(*) in the factory read/mapping path; they are now denormalized columns kept in
-- sync by triggers.
--
-- Mirrors V1.4.344.01 (factory element counters) and follows the TWINS wrapper-functions convention
-- (docs/db_trigger_functions_convention.md): counter maintenance is a side-effect -> AFTER triggers;
-- business logic lives in stored procedures; thin per-table/per-operation wrappers delegate via PERFORM;
-- reassignment updates both the old and the new parent (IS DISTINCT FROM).
--
-- IMPORTANT: twin_factory_pipeline_step and twin_factory_multiplier_filter already have
-- after_*_wrapper functions + triggers created by V1.4.327.03 (they maintain the
-- twin_factory_condition_set usage counters). Those wrappers are EXTENDED here with CREATE OR
-- REPLACE so they ALSO maintain these counters; the existing condition-set PERFORM calls are
-- preserved verbatim.

-- 1. Counter columns
alter table twin_factory_pipeline
    add column if not exists factory_pipeline_steps_count integer not null default 0;

alter table twin_factory_multiplier
    add column if not exists factory_multiplier_filters_count integer not null default 0;

-- 2. Backfill from current data (single bulk UPDATE per table)
update twin_factory_pipeline p set
    factory_pipeline_steps_count = coalesce((select count(*) from twin_factory_pipeline_step where twin_factory_pipeline_id = p.id), 0);

update twin_factory_multiplier m set
    factory_multiplier_filters_count = coalesce((select count(*) from twin_factory_multiplier_filter where twin_factory_multiplier_id = m.id), 0);

-- 3. Business-logic procedures (one per counter; reads/writes DB, NOT IMMUTABLE)
create or replace function twin_factory_pipeline_steps_count_adjust(
    p_pipeline_id uuid,
    p_delta       int
) returns void as $$
begin
    if p_pipeline_id is null or p_delta = 0 then
        return;
    end if;
    update twin_factory_pipeline
    set factory_pipeline_steps_count = factory_pipeline_steps_count + p_delta
    where id = p_pipeline_id;
end;
$$ language plpgsql;

create or replace function twin_factory_multiplier_filters_count_adjust(
    p_multiplier_id uuid,
    p_delta         int
) returns void as $$
begin
    if p_multiplier_id is null or p_delta = 0 then
        return;
    end if;
    update twin_factory_multiplier
    set factory_multiplier_filters_count = factory_multiplier_filters_count + p_delta
    where id = p_multiplier_id;
end;
$$ language plpgsql;

-- 4. Drop old triggers (created by V1.4.327.03)
drop trigger if exists twin_factory_pipeline_step_after_insert_wrapper_trigger on twin_factory_pipeline_step;
drop trigger if exists twin_factory_pipeline_step_after_update_wrapper_trigger on twin_factory_pipeline_step;
drop trigger if exists twin_factory_pipeline_step_after_delete_wrapper_trigger on twin_factory_pipeline_step;
drop trigger if exists twin_factory_multiplier_filter_after_insert_wrapper_trigger on twin_factory_multiplier_filter;
drop trigger if exists twin_factory_multiplier_filter_after_update_wrapper_trigger on twin_factory_multiplier_filter;
drop trigger if exists twin_factory_multiplier_filter_after_delete_wrapper_trigger on twin_factory_multiplier_filter;

-- 5. Wrapper functions (extend V1.4.327.03 condition-set wrappers)

-- twin_factory_pipeline_step
create or replace function twin_factory_pipeline_step_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'pipeline_step', 1);
    perform twin_factory_pipeline_steps_count_adjust(new.twin_factory_pipeline_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_pipeline_step_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'pipeline_step', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'pipeline_step', 1);
    end if;
    if new.twin_factory_pipeline_id is distinct from old.twin_factory_pipeline_id then
        perform twin_factory_pipeline_steps_count_adjust(old.twin_factory_pipeline_id, -1);
        perform twin_factory_pipeline_steps_count_adjust(new.twin_factory_pipeline_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_pipeline_step_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'pipeline_step', -1);
    perform twin_factory_pipeline_steps_count_adjust(old.twin_factory_pipeline_id, -1);
    return old;
end;
$$ language plpgsql;

-- twin_factory_multiplier_filter
create or replace function twin_factory_multiplier_filter_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'multiplier_filter', 1);
    perform twin_factory_multiplier_filters_count_adjust(new.twin_factory_multiplier_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_multiplier_filter_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'multiplier_filter', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'multiplier_filter', 1);
    end if;
    if new.twin_factory_multiplier_id is distinct from old.twin_factory_multiplier_id then
        perform twin_factory_multiplier_filters_count_adjust(old.twin_factory_multiplier_id, -1);
        perform twin_factory_multiplier_filters_count_adjust(new.twin_factory_multiplier_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_multiplier_filter_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'multiplier_filter', -1);
    perform twin_factory_multiplier_filters_count_adjust(old.twin_factory_multiplier_id, -1);
    return old;
end;
$$ language plpgsql;

-- 6. Triggers

create trigger twin_factory_pipeline_step_after_insert_wrapper_trigger
    after insert on twin_factory_pipeline_step
    for each row execute function twin_factory_pipeline_step_after_insert_wrapper();
create trigger twin_factory_pipeline_step_after_update_wrapper_trigger
    after update on twin_factory_pipeline_step
    for each row execute function twin_factory_pipeline_step_after_update_wrapper();
create trigger twin_factory_pipeline_step_after_delete_wrapper_trigger
    after delete on twin_factory_pipeline_step
    for each row execute function twin_factory_pipeline_step_after_delete_wrapper();

create trigger twin_factory_multiplier_filter_after_insert_wrapper_trigger
    after insert on twin_factory_multiplier_filter
    for each row execute function twin_factory_multiplier_filter_after_insert_wrapper();
create trigger twin_factory_multiplier_filter_after_update_wrapper_trigger
    after update on twin_factory_multiplier_filter
    for each row execute function twin_factory_multiplier_filter_after_update_wrapper();
create trigger twin_factory_multiplier_filter_after_delete_wrapper_trigger
    after delete on twin_factory_multiplier_filter
    for each row execute function twin_factory_multiplier_filter_after_delete_wrapper();
