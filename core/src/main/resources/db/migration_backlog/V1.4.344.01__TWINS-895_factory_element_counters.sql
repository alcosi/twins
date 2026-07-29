-- Materializes 4 factory element counters (pipelines / multipliers / branches / erasers) on
-- twin_factory, maintained by AFTER insert/update/delete triggers. These counters used to be
-- computed on the fly via COUNT(*) in the factory read/mapping path; they are now denormalized
-- columns kept in sync by triggers. factoryUsagesCount stays @Transient (it is a multi-source
-- inbound count and is still computed on the fly).
--
-- Follows the TWINS wrapper-functions convention (docs/db_trigger_functions_convention.md) and
-- mirrors V1.4.327.03 (twin_factory_condition_set usage counters): counter maintenance is a
-- side-effect -> AFTER triggers; business logic lives in a stored procedure
-- (twin_factory_elements_count_adjust); thin per-table/per-operation wrapper functions delegate
-- to it via PERFORM; reassignment updates both the old and the new parent (IS DISTINCT FROM).
--
-- IMPORTANT: twin_factory_pipeline / twin_factory_branch / twin_factory_eraser already have
-- after_*_wrapper functions + triggers created by V1.4.327.03 (they maintain the
-- twin_factory_condition_set usage counters). Those wrappers are EXTENDED here with CREATE OR
-- REPLACE so they ALSO maintain the twin_factory element counters; the existing condition-set
-- PERFORM calls are preserved verbatim. twin_factory_multiplier had no prior wrappers and gets
-- fresh ones.

-- 1. Counter columns
alter table twin_factory
    add column if not exists factory_pipelines_count   integer not null default 0,
    add column if not exists factory_multipliers_count integer not null default 0,
    add column if not exists factory_branches_count    integer not null default 0,
    add column if not exists factory_erasers_count     integer not null default 0;

-- 2. Backfill from current data (single bulk UPDATE, no per-row loop)
update twin_factory f set
    factory_pipelines_count   = coalesce((select count(*) from twin_factory_pipeline   where twin_factory_id = f.id), 0),
    factory_multipliers_count = coalesce((select count(*) from twin_factory_multiplier where twin_factory_id = f.id), 0),
    factory_branches_count    = coalesce((select count(*) from twin_factory_branch    where twin_factory_id = f.id), 0),
    factory_erasers_count     = coalesce((select count(*) from twin_factory_eraser    where twin_factory_id = f.id), 0);

-- 3. Business-logic procedure: applies a delta to the counter column matching the source table.
--    Reads/writes the DB, so it is NOT IMMUTABLE.
create or replace function twin_factory_elements_count_adjust(
    p_factory_id uuid,
    p_source     text,
    p_delta      int
) returns void as $$
begin
    if p_factory_id is null or p_delta = 0 then
        return;
    end if;

    -- A single per-source UPDATE writes only the affected column. A CASE-expression UPDATE
    -- touching all four columns would also bump tuple/WAL churn on every child DML, so we branch.
    if p_source = 'pipeline' then
        update twin_factory set factory_pipelines_count = factory_pipelines_count + p_delta where id = p_factory_id;
    elseif p_source = 'multiplier' then
        update twin_factory set factory_multipliers_count = factory_multipliers_count + p_delta where id = p_factory_id;
    elseif p_source = 'branch' then
        update twin_factory set factory_branches_count = factory_branches_count + p_delta where id = p_factory_id;
    elseif p_source = 'eraser' then
        update twin_factory set factory_erasers_count = factory_erasers_count + p_delta where id = p_factory_id;
    end if;
end;
$$ language plpgsql;

-- 4. Drop old triggers (pipeline/branch/eraser had them from V1.4.327.03; multiplier is new)
drop trigger if exists twin_factory_pipeline_after_insert_wrapper_trigger   on twin_factory_pipeline;
drop trigger if exists twin_factory_pipeline_after_update_wrapper_trigger   on twin_factory_pipeline;
drop trigger if exists twin_factory_pipeline_after_delete_wrapper_trigger   on twin_factory_pipeline;
drop trigger if exists twin_factory_branch_after_insert_wrapper_trigger     on twin_factory_branch;
drop trigger if exists twin_factory_branch_after_update_wrapper_trigger     on twin_factory_branch;
drop trigger if exists twin_factory_branch_after_delete_wrapper_trigger     on twin_factory_branch;
drop trigger if exists twin_factory_eraser_after_insert_wrapper_trigger     on twin_factory_eraser;
drop trigger if exists twin_factory_eraser_after_update_wrapper_trigger     on twin_factory_eraser;
drop trigger if exists twin_factory_eraser_after_delete_wrapper_trigger     on twin_factory_eraser;
drop trigger if exists twin_factory_multiplier_after_insert_wrapper_trigger on twin_factory_multiplier;
drop trigger if exists twin_factory_multiplier_after_update_wrapper_trigger on twin_factory_multiplier;
drop trigger if exists twin_factory_multiplier_after_delete_wrapper_trigger on twin_factory_multiplier;

-- 5. Wrapper functions

-- twin_factory_pipeline (extends V1.4.327.03 condition-set wrapper)
create or replace function twin_factory_pipeline_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'pipeline', 1);
    perform twin_factory_elements_count_adjust(new.twin_factory_id, 'pipeline', 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_pipeline_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'pipeline', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'pipeline', 1);
    end if;
    if new.twin_factory_id is distinct from old.twin_factory_id then
        perform twin_factory_elements_count_adjust(old.twin_factory_id, 'pipeline', -1);
        perform twin_factory_elements_count_adjust(new.twin_factory_id, 'pipeline', 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_pipeline_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'pipeline', -1);
    perform twin_factory_elements_count_adjust(old.twin_factory_id, 'pipeline', -1);
    return old;
end;
$$ language plpgsql;

-- twin_factory_branch (extends V1.4.327.03 condition-set wrapper)
create or replace function twin_factory_branch_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'branch', 1);
    perform twin_factory_elements_count_adjust(new.twin_factory_id, 'branch', 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_branch_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'branch', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'branch', 1);
    end if;
    if new.twin_factory_id is distinct from old.twin_factory_id then
        perform twin_factory_elements_count_adjust(old.twin_factory_id, 'branch', -1);
        perform twin_factory_elements_count_adjust(new.twin_factory_id, 'branch', 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_branch_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'branch', -1);
    perform twin_factory_elements_count_adjust(old.twin_factory_id, 'branch', -1);
    return old;
end;
$$ language plpgsql;

-- twin_factory_eraser (extends V1.4.327.03 condition-set wrapper)
create or replace function twin_factory_eraser_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'eraser', 1);
    perform twin_factory_elements_count_adjust(new.twin_factory_id, 'eraser', 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_eraser_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_condition_set_id is distinct from old.twin_factory_condition_set_id then
        perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'eraser', -1);
        perform twin_factory_condition_set_usage_adjust(new.twin_factory_condition_set_id, 'eraser', 1);
    end if;
    if new.twin_factory_id is distinct from old.twin_factory_id then
        perform twin_factory_elements_count_adjust(old.twin_factory_id, 'eraser', -1);
        perform twin_factory_elements_count_adjust(new.twin_factory_id, 'eraser', 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_eraser_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_usage_adjust(old.twin_factory_condition_set_id, 'eraser', -1);
    perform twin_factory_elements_count_adjust(old.twin_factory_id, 'eraser', -1);
    return old;
end;
$$ language plpgsql;

-- twin_factory_multiplier (fresh — no prior wrapper)
create or replace function twin_factory_multiplier_after_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_elements_count_adjust(new.twin_factory_id, 'multiplier', 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_multiplier_after_update_wrapper() returns trigger as $$
begin
    if new.twin_factory_id is distinct from old.twin_factory_id then
        perform twin_factory_elements_count_adjust(old.twin_factory_id, 'multiplier', -1);
        perform twin_factory_elements_count_adjust(new.twin_factory_id, 'multiplier', 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_multiplier_after_delete_wrapper() returns trigger as $$
begin
    perform twin_factory_elements_count_adjust(old.twin_factory_id, 'multiplier', -1);
    return old;
end;
$$ language plpgsql;

-- 6. Triggers

create trigger twin_factory_pipeline_after_insert_wrapper_trigger
    after insert on twin_factory_pipeline
    for each row execute function twin_factory_pipeline_after_insert_wrapper();
create trigger twin_factory_pipeline_after_update_wrapper_trigger
    after update on twin_factory_pipeline
    for each row execute function twin_factory_pipeline_after_update_wrapper();
create trigger twin_factory_pipeline_after_delete_wrapper_trigger
    after delete on twin_factory_pipeline
    for each row execute function twin_factory_pipeline_after_delete_wrapper();

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

create trigger twin_factory_multiplier_after_insert_wrapper_trigger
    after insert on twin_factory_multiplier
    for each row execute function twin_factory_multiplier_after_insert_wrapper();
create trigger twin_factory_multiplier_after_update_wrapper_trigger
    after update on twin_factory_multiplier
    for each row execute function twin_factory_multiplier_after_update_wrapper();
create trigger twin_factory_multiplier_after_delete_wrapper_trigger
    after delete on twin_factory_multiplier
    for each row execute function twin_factory_multiplier_after_delete_wrapper();
