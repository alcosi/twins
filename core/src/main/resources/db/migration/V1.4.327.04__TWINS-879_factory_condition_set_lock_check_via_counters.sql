-- Reworks the "condition set is locked once used" check to rely on the usage
-- counters introduced in V1.4.xx0.02 instead of scanning the 6 referencing tables
-- with a SELECT ... UNION ALL. The counters already hold the exact number of
-- references per table, so "is used" is just "sum of counters > 0" on the row
-- being updated, which is a single index lookup instead of 6 table probes.
--
-- MUST run after V1.4.xx0.02 (needs the usage_count_* columns) and replaces the
-- twin_factory_condition_set_factory_lock_check function defined in V1.4.xx0.01.
-- The twin_factory_condition_set_before_update_wrapper trigger keeps calling this
-- function unchanged.
--
-- Follows the TWINS wrapper-functions convention: business logic stays in the
-- stored function, reads the DB so it is NOT IMMUTABLE, NULL is compared via
-- IS DISTINCT FROM.

create or replace function twin_factory_condition_set_factory_lock_check(
    p_condition_set_id uuid,
    p_old_factory_id   uuid,
    p_new_factory_id   uuid
) returns void as $$
declare
    v_usage_total bigint;
begin
    -- only the twin_factory_id change is restricted
    if p_new_factory_id is not distinct from p_old_factory_id then
        return;
    end if;

    select coalesce(usage_count_branch, 0)
         + coalesce(usage_count_eraser, 0)
         + coalesce(usage_count_pipeline, 0)
         + coalesce(usage_count_trigger, 0)
         + coalesce(usage_count_multiplier_filter, 0)
         + coalesce(usage_count_pipeline_step, 0)
    into v_usage_total
    from twin_factory_condition_set
    where id = p_condition_set_id;

    if coalesce(v_usage_total, 0) > 0 then
        raise exception
            'cannot change twin_factory_id of twin_factory_condition_set % (from % to %): it is already used by factory sub-entities',
            p_condition_set_id, p_old_factory_id, p_new_factory_id;
    end if;
end;
$$ language plpgsql;
