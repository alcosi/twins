-- Materializes a single usage_count counter on twin_validator_set: how many times the set is
-- referenced from the configuration tables that hold a twin_validator_set_id FK. The counter is
-- seeded from current data by a single bulk UPDATE and then kept in sync by AFTER
-- insert/update/delete triggers on every referencing table.
--
-- Counted sources (12) — everything that REFERENCES twin_validator_set EXCEPT twin_validator
-- (twin_validator is the set's OWN composition, i.e. its members, not a usage of the set):
--   twin_action_validator_rule
--   twin_comment_action_alien_validator_rule
--   twinflow_transition_validator_rule
--   twin_attachment_action_alien_validator_rule
--   twin_attachment_action_self_validator_rule
--   twin_pointer_validator_rule
--   twin_class_dynamic_marker
--   twin_class_field_action_validation_rule
--   twin_class_field_recompute_on_action_validator_rule
--   twin_recompute_on_field_validator_rule
--   twin_recompute_on_action_validator_rule
--   history_notification
--
-- Follows the TWINS wrapper-functions convention (docs/db_trigger_functions_convention.md),
-- mirroring V1.4.327.03 (twin_factory_condition_set usage counters) and V1.4.348.01 (twin_factory
-- element counters): counter maintenance is a side-effect -> AFTER triggers; business logic lives
-- in a stored procedure (twin_validator_set_usage_adjust); thin per-table/per-operation wrapper
-- functions delegate to it via PERFORM; reassignment updates both the old and the new parent
-- (IS DISTINCT FROM). Because this is a single unified counter (no per-source column), the adjust
-- procedure takes no source argument — but per-table wrappers are still kept for consistency with
-- the convention and for independent extensibility per referencing table.

-- 1. Counter column
alter table twin_validator_set
    add column if not exists usage_count integer not null default 0;

-- 1b. Missing FK index on twin_class_field_action_validation_rule.twin_validator_set_id.
--     This table was created in V1.4.243.01 — after V1.4.97.01 "add_indexes_for_all_fk" — so its FK
--     column never got indexed (a CLAUDE.md violation: "index every FK column"). Added here because
--     the backfill below counts this table per validator set and the trigger-driven adjust() UPDATEs
--     resolve on twin_validator_set.id (PK), but without this index the backfill subquery and any
--     FK-driven lookup on this table fall back to a Seq Scan.
create index if not exists idx_twin_class_field_action_validation_rule_twin_validator_set_id
    on twin_class_field_action_validation_rule(twin_validator_set_id);

-- 2. Backfill from current data (single bulk UPDATE, no per-row loop)
update twin_validator_set s set
    usage_count =
        coalesce((select count(*)::int from twin_action_validator_rule                            where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from twin_comment_action_alien_validator_rule              where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from twinflow_transition_validator_rule                    where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from twin_attachment_action_alien_validator_rule           where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from twin_attachment_action_self_validator_rule            where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from twin_pointer_validator_rule                           where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from twin_class_dynamic_marker                             where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from twin_class_field_action_validation_rule               where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from twin_class_field_recompute_on_action_validator_rule   where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from twin_recompute_on_field_validator_rule                where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from twin_recompute_on_action_validator_rule               where twin_validator_set_id = s.id), 0) +
        coalesce((select count(*)::int from history_notification                                  where twin_validator_set_id = s.id), 0);

-- 2b. Partial index backing the usageCountRange search filter (TwinValidatorSetSearchService ->
--     checkFieldIntegerRange on usage_count). Built AFTER the backfill so it is populated in a single
--     pass. Partial (usage_count > 0) deliberately: the overwhelming majority of sets are unused
--     (count = 0), and the predicate keeps those rows out of the index, so the trigger-driven
--     `usage_count = usage_count + delta` UPDATEs pay index write cost only for sets in active use.
create index if not exists idx_twin_validator_set_usage_count
    on twin_validator_set(usage_count)
    where usage_count > 0;

-- 3. Business-logic procedure: applies a delta to the unified usage_count. Reads/writes the DB,
--    so it is NOT IMMUTABLE.
create or replace function twin_validator_set_usage_adjust(
    p_validator_set_id uuid,
    p_delta            int
) returns void as $$
begin
    if p_validator_set_id is null or p_delta = 0 then
        return;
    end if;

    update twin_validator_set
    set usage_count = usage_count + p_delta
    where id = p_validator_set_id;
end;
$$ language plpgsql;

-- 4. Drop old triggers (idempotency — mirrors V1.4.327.03 convention; no-op on a fresh DB,
--    safe on re-runs / partial dev states)
drop trigger if exists twin_action_validator_rule_after_insert_wrapper_trigger                                on twin_action_validator_rule;
drop trigger if exists twin_action_validator_rule_after_update_wrapper_trigger                                on twin_action_validator_rule;
drop trigger if exists twin_action_validator_rule_after_delete_wrapper_trigger                                on twin_action_validator_rule;
drop trigger if exists twin_comment_action_alien_validator_rule_after_insert_wrapper_trigger                  on twin_comment_action_alien_validator_rule;
drop trigger if exists twin_comment_action_alien_validator_rule_after_update_wrapper_trigger                  on twin_comment_action_alien_validator_rule;
drop trigger if exists twin_comment_action_alien_validator_rule_after_delete_wrapper_trigger                  on twin_comment_action_alien_validator_rule;
drop trigger if exists twinflow_transition_validator_rule_after_insert_wrapper_trigger                        on twinflow_transition_validator_rule;
drop trigger if exists twinflow_transition_validator_rule_after_update_wrapper_trigger                        on twinflow_transition_validator_rule;
drop trigger if exists twinflow_transition_validator_rule_after_delete_wrapper_trigger                        on twinflow_transition_validator_rule;
drop trigger if exists twin_attachment_action_alien_validator_rule_after_insert_wrapper_trigger               on twin_attachment_action_alien_validator_rule;
drop trigger if exists twin_attachment_action_alien_validator_rule_after_update_wrapper_trigger               on twin_attachment_action_alien_validator_rule;
drop trigger if exists twin_attachment_action_alien_validator_rule_after_delete_wrapper_trigger               on twin_attachment_action_alien_validator_rule;
drop trigger if exists twin_attachment_action_self_validator_rule_after_insert_wrapper_trigger                on twin_attachment_action_self_validator_rule;
drop trigger if exists twin_attachment_action_self_validator_rule_after_update_wrapper_trigger                on twin_attachment_action_self_validator_rule;
drop trigger if exists twin_attachment_action_self_validator_rule_after_delete_wrapper_trigger                on twin_attachment_action_self_validator_rule;
drop trigger if exists twin_pointer_validator_rule_after_insert_wrapper_trigger                               on twin_pointer_validator_rule;
drop trigger if exists twin_pointer_validator_rule_after_update_wrapper_trigger                               on twin_pointer_validator_rule;
drop trigger if exists twin_pointer_validator_rule_after_delete_wrapper_trigger                               on twin_pointer_validator_rule;
drop trigger if exists twin_class_dynamic_marker_after_insert_wrapper_trigger                                 on twin_class_dynamic_marker;
drop trigger if exists twin_class_dynamic_marker_after_update_wrapper_trigger                                 on twin_class_dynamic_marker;
drop trigger if exists twin_class_dynamic_marker_after_delete_wrapper_trigger                                 on twin_class_dynamic_marker;
drop trigger if exists twin_class_field_action_validation_rule_after_insert_wrapper_trigger                   on twin_class_field_action_validation_rule;
drop trigger if exists twin_class_field_action_validation_rule_after_update_wrapper_trigger                   on twin_class_field_action_validation_rule;
drop trigger if exists twin_class_field_action_validation_rule_after_delete_wrapper_trigger                   on twin_class_field_action_validation_rule;
drop trigger if exists twin_class_field_recompute_on_action_validator_rule_after_insert_wrapper_trigger       on twin_class_field_recompute_on_action_validator_rule;
drop trigger if exists twin_class_field_recompute_on_action_validator_rule_after_update_wrapper_trigger       on twin_class_field_recompute_on_action_validator_rule;
drop trigger if exists twin_class_field_recompute_on_action_validator_rule_after_delete_wrapper_trigger       on twin_class_field_recompute_on_action_validator_rule;
drop trigger if exists twin_recompute_on_field_validator_rule_after_insert_wrapper_trigger                    on twin_recompute_on_field_validator_rule;
drop trigger if exists twin_recompute_on_field_validator_rule_after_update_wrapper_trigger                    on twin_recompute_on_field_validator_rule;
drop trigger if exists twin_recompute_on_field_validator_rule_after_delete_wrapper_trigger                    on twin_recompute_on_field_validator_rule;
drop trigger if exists twin_recompute_on_action_validator_rule_after_insert_wrapper_trigger                   on twin_recompute_on_action_validator_rule;
drop trigger if exists twin_recompute_on_action_validator_rule_after_update_wrapper_trigger                   on twin_recompute_on_action_validator_rule;
drop trigger if exists twin_recompute_on_action_validator_rule_after_delete_wrapper_trigger                   on twin_recompute_on_action_validator_rule;
drop trigger if exists history_notification_after_insert_wrapper_trigger                                      on history_notification;
drop trigger if exists history_notification_after_update_wrapper_trigger                                      on history_notification;
drop trigger if exists history_notification_after_delete_wrapper_trigger                                      on history_notification;

-- 5. Wrapper functions (one per table per operation)

-- twin_action_validator_rule
create or replace function twin_action_validator_rule_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_action_validator_rule_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_action_validator_rule_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- twin_comment_action_alien_validator_rule
create or replace function twin_comment_action_alien_validator_rule_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_comment_action_alien_validator_rule_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_comment_action_alien_validator_rule_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- twinflow_transition_validator_rule
create or replace function twinflow_transition_validator_rule_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twinflow_transition_validator_rule_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twinflow_transition_validator_rule_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- twin_attachment_action_alien_validator_rule
create or replace function twin_attachment_action_alien_validator_rule_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_attachment_action_alien_validator_rule_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_attachment_action_alien_validator_rule_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- twin_attachment_action_self_validator_rule
create or replace function twin_attachment_action_self_validator_rule_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_attachment_action_self_validator_rule_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_attachment_action_self_validator_rule_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- twin_pointer_validator_rule
create or replace function twin_pointer_validator_rule_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_pointer_validator_rule_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_pointer_validator_rule_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- twin_class_dynamic_marker
-- NOTE: this table already has non-wrapper after insert/update/delete triggers from V1.4.117.01
-- (update_twin_class_has_dynamic_markers). The wrappers below are SEPARATE triggers with different
-- names; both fire (PostgreSQL runs all AFTER triggers on a given operation).
create or replace function twin_class_dynamic_marker_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_class_dynamic_marker_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_class_dynamic_marker_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- twin_class_field_action_validation_rule
create or replace function twin_class_field_action_validation_rule_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_class_field_action_validation_rule_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_class_field_action_validation_rule_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- twin_class_field_recompute_on_action_validator_rule
create or replace function twin_class_field_recompute_on_action_validator_rule_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_class_field_recompute_on_action_validator_rule_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_class_field_recompute_on_action_validator_rule_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- twin_recompute_on_field_validator_rule
create or replace function twin_recompute_on_field_validator_rule_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_recompute_on_field_validator_rule_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_recompute_on_field_validator_rule_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- twin_recompute_on_action_validator_rule
create or replace function twin_recompute_on_action_validator_rule_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function twin_recompute_on_action_validator_rule_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function twin_recompute_on_action_validator_rule_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- history_notification
create or replace function history_notification_after_insert_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    return new;
end;
$$ language plpgsql;

create or replace function history_notification_after_update_wrapper() returns trigger as $$
begin
    if new.twin_validator_set_id is distinct from old.twin_validator_set_id then
        perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
        perform twin_validator_set_usage_adjust(new.twin_validator_set_id, 1);
    end if;
    return new;
end;
$$ language plpgsql;

create or replace function history_notification_after_delete_wrapper() returns trigger as $$
begin
    perform twin_validator_set_usage_adjust(old.twin_validator_set_id, -1);
    return old;
end;
$$ language plpgsql;

-- 6. Triggers

create trigger twin_action_validator_rule_after_insert_wrapper_trigger
    after insert on twin_action_validator_rule
    for each row execute function twin_action_validator_rule_after_insert_wrapper();
create trigger twin_action_validator_rule_after_update_wrapper_trigger
    after update on twin_action_validator_rule
    for each row execute function twin_action_validator_rule_after_update_wrapper();
create trigger twin_action_validator_rule_after_delete_wrapper_trigger
    after delete on twin_action_validator_rule
    for each row execute function twin_action_validator_rule_after_delete_wrapper();

create trigger twin_comment_action_alien_validator_rule_after_insert_wrapper_trigger
    after insert on twin_comment_action_alien_validator_rule
    for each row execute function twin_comment_action_alien_validator_rule_after_insert_wrapper();
create trigger twin_comment_action_alien_validator_rule_after_update_wrapper_trigger
    after update on twin_comment_action_alien_validator_rule
    for each row execute function twin_comment_action_alien_validator_rule_after_update_wrapper();
create trigger twin_comment_action_alien_validator_rule_after_delete_wrapper_trigger
    after delete on twin_comment_action_alien_validator_rule
    for each row execute function twin_comment_action_alien_validator_rule_after_delete_wrapper();

create trigger twinflow_transition_validator_rule_after_insert_wrapper_trigger
    after insert on twinflow_transition_validator_rule
    for each row execute function twinflow_transition_validator_rule_after_insert_wrapper();
create trigger twinflow_transition_validator_rule_after_update_wrapper_trigger
    after update on twinflow_transition_validator_rule
    for each row execute function twinflow_transition_validator_rule_after_update_wrapper();
create trigger twinflow_transition_validator_rule_after_delete_wrapper_trigger
    after delete on twinflow_transition_validator_rule
    for each row execute function twinflow_transition_validator_rule_after_delete_wrapper();

create trigger twin_attachment_action_alien_validator_rule_after_insert_wrapper_trigger
    after insert on twin_attachment_action_alien_validator_rule
    for each row execute function twin_attachment_action_alien_validator_rule_after_insert_wrapper();
create trigger twin_attachment_action_alien_validator_rule_after_update_wrapper_trigger
    after update on twin_attachment_action_alien_validator_rule
    for each row execute function twin_attachment_action_alien_validator_rule_after_update_wrapper();
create trigger twin_attachment_action_alien_validator_rule_after_delete_wrapper_trigger
    after delete on twin_attachment_action_alien_validator_rule
    for each row execute function twin_attachment_action_alien_validator_rule_after_delete_wrapper();

create trigger twin_attachment_action_self_validator_rule_after_insert_wrapper_trigger
    after insert on twin_attachment_action_self_validator_rule
    for each row execute function twin_attachment_action_self_validator_rule_after_insert_wrapper();
create trigger twin_attachment_action_self_validator_rule_after_update_wrapper_trigger
    after update on twin_attachment_action_self_validator_rule
    for each row execute function twin_attachment_action_self_validator_rule_after_update_wrapper();
create trigger twin_attachment_action_self_validator_rule_after_delete_wrapper_trigger
    after delete on twin_attachment_action_self_validator_rule
    for each row execute function twin_attachment_action_self_validator_rule_after_delete_wrapper();

create trigger twin_pointer_validator_rule_after_insert_wrapper_trigger
    after insert on twin_pointer_validator_rule
    for each row execute function twin_pointer_validator_rule_after_insert_wrapper();
create trigger twin_pointer_validator_rule_after_update_wrapper_trigger
    after update on twin_pointer_validator_rule
    for each row execute function twin_pointer_validator_rule_after_update_wrapper();
create trigger twin_pointer_validator_rule_after_delete_wrapper_trigger
    after delete on twin_pointer_validator_rule
    for each row execute function twin_pointer_validator_rule_after_delete_wrapper();

create trigger twin_class_dynamic_marker_after_insert_wrapper_trigger
    after insert on twin_class_dynamic_marker
    for each row execute function twin_class_dynamic_marker_after_insert_wrapper();
create trigger twin_class_dynamic_marker_after_update_wrapper_trigger
    after update on twin_class_dynamic_marker
    for each row execute function twin_class_dynamic_marker_after_update_wrapper();
create trigger twin_class_dynamic_marker_after_delete_wrapper_trigger
    after delete on twin_class_dynamic_marker
    for each row execute function twin_class_dynamic_marker_after_delete_wrapper();

create trigger twin_class_field_action_validation_rule_after_insert_wrapper_trigger
    after insert on twin_class_field_action_validation_rule
    for each row execute function twin_class_field_action_validation_rule_after_insert_wrapper();
create trigger twin_class_field_action_validation_rule_after_update_wrapper_trigger
    after update on twin_class_field_action_validation_rule
    for each row execute function twin_class_field_action_validation_rule_after_update_wrapper();
create trigger twin_class_field_action_validation_rule_after_delete_wrapper_trigger
    after delete on twin_class_field_action_validation_rule
    for each row execute function twin_class_field_action_validation_rule_after_delete_wrapper();

create trigger twin_class_field_recompute_on_action_validator_rule_after_insert_wrapper_trigger
    after insert on twin_class_field_recompute_on_action_validator_rule
    for each row execute function twin_class_field_recompute_on_action_validator_rule_after_insert_wrapper();
create trigger twin_class_field_recompute_on_action_validator_rule_after_update_wrapper_trigger
    after update on twin_class_field_recompute_on_action_validator_rule
    for each row execute function twin_class_field_recompute_on_action_validator_rule_after_update_wrapper();
create trigger twin_class_field_recompute_on_action_validator_rule_after_delete_wrapper_trigger
    after delete on twin_class_field_recompute_on_action_validator_rule
    for each row execute function twin_class_field_recompute_on_action_validator_rule_after_delete_wrapper();

create trigger twin_recompute_on_field_validator_rule_after_insert_wrapper_trigger
    after insert on twin_recompute_on_field_validator_rule
    for each row execute function twin_recompute_on_field_validator_rule_after_insert_wrapper();
create trigger twin_recompute_on_field_validator_rule_after_update_wrapper_trigger
    after update on twin_recompute_on_field_validator_rule
    for each row execute function twin_recompute_on_field_validator_rule_after_update_wrapper();
create trigger twin_recompute_on_field_validator_rule_after_delete_wrapper_trigger
    after delete on twin_recompute_on_field_validator_rule
    for each row execute function twin_recompute_on_field_validator_rule_after_delete_wrapper();

create trigger twin_recompute_on_action_validator_rule_after_insert_wrapper_trigger
    after insert on twin_recompute_on_action_validator_rule
    for each row execute function twin_recompute_on_action_validator_rule_after_insert_wrapper();
create trigger twin_recompute_on_action_validator_rule_after_update_wrapper_trigger
    after update on twin_recompute_on_action_validator_rule
    for each row execute function twin_recompute_on_action_validator_rule_after_update_wrapper();
create trigger twin_recompute_on_action_validator_rule_after_delete_wrapper_trigger
    after delete on twin_recompute_on_action_validator_rule
    for each row execute function twin_recompute_on_action_validator_rule_after_delete_wrapper();

create trigger history_notification_after_insert_wrapper_trigger
    after insert on history_notification
    for each row execute function history_notification_after_insert_wrapper();
create trigger history_notification_after_update_wrapper_trigger
    after update on history_notification
    for each row execute function history_notification_after_update_wrapper();
create trigger history_notification_after_delete_wrapper_trigger
    after delete on history_notification
    for each row execute function history_notification_after_delete_wrapper();
