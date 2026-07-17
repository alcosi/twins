-- Validates that a twin_factory_condition_set referenced by a factory sub-entity row
-- belongs to the same twin_factory as that row. Prevents using a condition set of
-- factory A inside a branch/eraser/pipeline/... of factory B.
--
-- Structure follows the TWINS wrapper-functions convention:
--   * business logic lives in a stored procedure (twin_factory_condition_set_factory_match_check)
--   * thin per-table/per-operation wrapper functions delegated to it via PERFORM

-- 0. Add the missing FK from a condition set to its owner factory.
-- NOTE: requires twin_factory_condition_set.twin_factory_id to reference an existing twin_factory
-- for every row (clean up orphan rows before running this migration).
alter table twin_factory_condition_set
    add constraint twin_factory_condition_set_twin_factory_id_fk
        foreign key (twin_factory_id) references twin_factory (id);

-- 1. Remove old triggers
drop trigger if exists twin_factory_branch_before_insert_wrapper_trigger on twin_factory_branch;
drop trigger if exists twin_factory_branch_before_update_wrapper_trigger on twin_factory_branch;
drop trigger if exists twin_factory_eraser_before_insert_wrapper_trigger on twin_factory_eraser;
drop trigger if exists twin_factory_eraser_before_update_wrapper_trigger on twin_factory_eraser;
drop trigger if exists twin_factory_pipeline_before_insert_wrapper_trigger on twin_factory_pipeline;
drop trigger if exists twin_factory_pipeline_before_update_wrapper_trigger on twin_factory_pipeline;
drop trigger if exists twin_factory_trigger_before_insert_wrapper_trigger on twin_factory_trigger;
drop trigger if exists twin_factory_trigger_before_update_wrapper_trigger on twin_factory_trigger;
drop trigger if exists twin_factory_multiplier_filter_before_insert_wrapper_trigger on twin_factory_multiplier_filter;
drop trigger if exists twin_factory_multiplier_filter_before_update_wrapper_trigger on twin_factory_multiplier_filter;
drop trigger if exists twin_factory_pipeline_step_before_insert_wrapper_trigger on twin_factory_pipeline_step;
drop trigger if exists twin_factory_pipeline_step_before_update_wrapper_trigger on twin_factory_pipeline_step;
drop trigger if exists twin_factory_condition_set_before_update_wrapper_trigger on twin_factory_condition_set;

-- 2. Business-logic procedure: raises when the condition set does not belong to the owner factory.
-- Reads from the DB, so it is NOT IMMUTABLE.
create or replace function twin_factory_condition_set_factory_match_check(
    p_condition_set_id uuid,
    p_owner_factory_id uuid,
    p_source_table     text,
    p_source_row_id    uuid
) returns void as $$
declare
    v_set_factory_id uuid;
begin
    -- condition set is optional on most tables
    if p_condition_set_id is null then
        return;
    end if;

    select cs.twin_factory_id
    into v_set_factory_id
    from twin_factory_condition_set cs
    where cs.id = p_condition_set_id;

    if v_set_factory_id is distinct from p_owner_factory_id then
        raise exception
            'twin_factory_condition_set % belongs to twin_factory %, but %.% belongs to twin_factory %',
            p_condition_set_id, v_set_factory_id, p_source_table, p_source_row_id, p_owner_factory_id;
    end if;
end;
$$ language plpgsql;

-- 2b. Business-logic procedure: forbids moving a condition set to another factory
-- once it is already used by any factory sub-entity. Reads from the DB, so it is NOT IMMUTABLE.
create or replace function twin_factory_condition_set_factory_lock_check(
    p_condition_set_id uuid,
    p_old_factory_id   uuid,
    p_new_factory_id   uuid
) returns void as $$
begin
    -- only the twin_factory_id change is restricted
    if p_new_factory_id is not distinct from p_old_factory_id then
        return;
    end if;

    if exists (
        select 1 from twin_factory_branch          where twin_factory_condition_set_id = p_condition_set_id
        union all
        select 1 from twin_factory_eraser          where twin_factory_condition_set_id = p_condition_set_id
        union all
        select 1 from twin_factory_pipeline        where twin_factory_condition_set_id = p_condition_set_id
        union all
        select 1 from twin_factory_trigger         where twin_factory_condition_set_id = p_condition_set_id
        union all
        select 1 from twin_factory_multiplier_filter where twin_factory_condition_set_id = p_condition_set_id
        union all
        select 1 from twin_factory_pipeline_step   where twin_factory_condition_set_id = p_condition_set_id
    ) then
        raise exception
            'cannot change twin_factory_id of twin_factory_condition_set % (from % to %): it is already used by factory sub-entities',
            p_condition_set_id, p_old_factory_id, p_new_factory_id;
    end if;
end;
$$ language plpgsql;

-- 3. Wrapper functions

-- twin_factory_branch (owner factory = twin_factory_id)
create or replace function twin_factory_branch_before_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, new.twin_factory_id,
            'twin_factory_branch', new.id);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_branch_before_update_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, new.twin_factory_id,
            'twin_factory_branch', new.id);
    return new;
end;
$$ language plpgsql;

-- twin_factory_eraser (owner factory = twin_factory_id)
create or replace function twin_factory_eraser_before_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, new.twin_factory_id,
            'twin_factory_eraser', new.id);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_eraser_before_update_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, new.twin_factory_id,
            'twin_factory_eraser', new.id);
    return new;
end;
$$ language plpgsql;

-- twin_factory_pipeline (owner factory = twin_factory_id)
create or replace function twin_factory_pipeline_before_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, new.twin_factory_id,
            'twin_factory_pipeline', new.id);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_pipeline_before_update_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, new.twin_factory_id,
            'twin_factory_pipeline', new.id);
    return new;
end;
$$ language plpgsql;

-- twin_factory_trigger (owner factory = twin_factory_id)
create or replace function twin_factory_trigger_before_insert_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, new.twin_factory_id,
            'twin_factory_trigger', new.id);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_trigger_before_update_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, new.twin_factory_id,
            'twin_factory_trigger', new.id);
    return new;
end;
$$ language plpgsql;

-- twin_factory_multiplier_filter (owner factory resolved from parent twin_factory_multiplier)
create or replace function twin_factory_multiplier_filter_before_insert_wrapper() returns trigger as $$
declare
    v_owner_factory_id uuid;
begin
    select m.twin_factory_id
    into v_owner_factory_id
    from twin_factory_multiplier m
    where m.id = new.twin_factory_multiplier_id;

    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, v_owner_factory_id,
            'twin_factory_multiplier_filter', new.id);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_multiplier_filter_before_update_wrapper() returns trigger as $$
declare
    v_owner_factory_id uuid;
begin
    select m.twin_factory_id
    into v_owner_factory_id
    from twin_factory_multiplier m
    where m.id = new.twin_factory_multiplier_id;

    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, v_owner_factory_id,
            'twin_factory_multiplier_filter', new.id);
    return new;
end;
$$ language plpgsql;

-- twin_factory_pipeline_step (owner factory resolved from parent twin_factory_pipeline)
create or replace function twin_factory_pipeline_step_before_insert_wrapper() returns trigger as $$
declare
    v_owner_factory_id uuid;
begin
    select p.twin_factory_id
    into v_owner_factory_id
    from twin_factory_pipeline p
    where p.id = new.twin_factory_pipeline_id;

    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, v_owner_factory_id,
            'twin_factory_pipeline_step', new.id);
    return new;
end;
$$ language plpgsql;

create or replace function twin_factory_pipeline_step_before_update_wrapper() returns trigger as $$
declare
    v_owner_factory_id uuid;
begin
    select p.twin_factory_id
    into v_owner_factory_id
    from twin_factory_pipeline p
    where p.id = new.twin_factory_pipeline_id;

    perform twin_factory_condition_set_factory_match_check(
            new.twin_factory_condition_set_id, v_owner_factory_id,
            'twin_factory_pipeline_step', new.id);
    return new;
end;
$$ language plpgsql;

-- twin_factory_condition_set (lock twin_factory_id once the set is in use)
create or replace function twin_factory_condition_set_before_update_wrapper() returns trigger as $$
begin
    perform twin_factory_condition_set_factory_lock_check(
            new.id, old.twin_factory_id, new.twin_factory_id);
    return new;
end;
$$ language plpgsql;

-- 4. Triggers
create trigger twin_factory_branch_before_insert_wrapper_trigger
    before insert on twin_factory_branch
    for each row execute function twin_factory_branch_before_insert_wrapper();
create trigger twin_factory_branch_before_update_wrapper_trigger
    before update on twin_factory_branch
    for each row execute function twin_factory_branch_before_update_wrapper();

create trigger twin_factory_eraser_before_insert_wrapper_trigger
    before insert on twin_factory_eraser
    for each row execute function twin_factory_eraser_before_insert_wrapper();
create trigger twin_factory_eraser_before_update_wrapper_trigger
    before update on twin_factory_eraser
    for each row execute function twin_factory_eraser_before_update_wrapper();

create trigger twin_factory_pipeline_before_insert_wrapper_trigger
    before insert on twin_factory_pipeline
    for each row execute function twin_factory_pipeline_before_insert_wrapper();
create trigger twin_factory_pipeline_before_update_wrapper_trigger
    before update on twin_factory_pipeline
    for each row execute function twin_factory_pipeline_before_update_wrapper();

create trigger twin_factory_trigger_before_insert_wrapper_trigger
    before insert on twin_factory_trigger
    for each row execute function twin_factory_trigger_before_insert_wrapper();
create trigger twin_factory_trigger_before_update_wrapper_trigger
    before update on twin_factory_trigger
    for each row execute function twin_factory_trigger_before_update_wrapper();

create trigger twin_factory_multiplier_filter_before_insert_wrapper_trigger
    before insert on twin_factory_multiplier_filter
    for each row execute function twin_factory_multiplier_filter_before_insert_wrapper();
create trigger twin_factory_multiplier_filter_before_update_wrapper_trigger
    before update on twin_factory_multiplier_filter
    for each row execute function twin_factory_multiplier_filter_before_update_wrapper();

create trigger twin_factory_pipeline_step_before_insert_wrapper_trigger
    before insert on twin_factory_pipeline_step
    for each row execute function twin_factory_pipeline_step_before_insert_wrapper();
create trigger twin_factory_pipeline_step_before_update_wrapper_trigger
    before update on twin_factory_pipeline_step
    for each row execute function twin_factory_pipeline_step_before_update_wrapper();

create trigger twin_factory_condition_set_before_update_wrapper_trigger
    before update on twin_factory_condition_set
    for each row execute function twin_factory_condition_set_before_update_wrapper();
