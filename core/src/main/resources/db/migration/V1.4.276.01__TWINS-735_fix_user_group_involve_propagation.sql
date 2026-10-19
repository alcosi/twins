create or replace function user_group_involve_by_assignee_propagation(
    new_assigner_user_id uuid,
    old_assigner_user_id uuid,
    p_new_twin_class_id uuid,
    p_old_twin_class_id uuid,
    p_new_twin_status_id uuid,
    p_old_twin_status_id uuid,
    p_owner_business_account_id uuid
) returns void
    volatile
    language plpgsql
as
$$
DECLARE
    old_group_id     uuid;
    old_group_domain uuid;
    new_group_id     uuid;
    new_group_domain uuid;
BEGIN
    IF current_setting('app.user_group_involve_by_assignee_propagation_trigger', true) IS DISTINCT FROM 'on' THEN
        RAISE EXCEPTION 'Function can be called only from trigger';
    END IF;

    -- Resolve old propagation group
    IF old_assigner_user_id IS NOT NULL THEN
        SELECT a.user_group_id, tc.domain_id
        INTO old_group_id, old_group_domain
        FROM user_group_involve_assignee a
                 JOIN twin_class tc ON tc.id = a.propagation_by_twin_class_id
        WHERE a.propagation_by_twin_class_id = p_old_twin_class_id
          AND (a.propagation_by_twin_status_id = p_old_twin_status_id
            OR a.propagation_by_twin_status_id IS NULL)
        LIMIT 1;
    END IF;

    -- Resolve new propagation group
    IF new_assigner_user_id IS NOT NULL THEN
        SELECT a.user_group_id, tc.domain_id
        INTO new_group_id, new_group_domain
        FROM user_group_involve_assignee a
                 JOIN twin_class tc ON tc.id = a.propagation_by_twin_class_id
        WHERE a.propagation_by_twin_class_id = p_new_twin_class_id
          AND (a.propagation_by_twin_status_id = p_new_twin_status_id
            OR a.propagation_by_twin_status_id IS NULL)
        LIMIT 1;
    END IF;

    -- Remove from old if involvement key changed
    IF old_group_id IS NOT NULL
        AND (old_assigner_user_id IS DISTINCT FROM new_assigner_user_id
            OR old_group_id IS DISTINCT FROM new_group_id
            OR old_group_domain IS DISTINCT FROM new_group_domain)
    THEN
        PERFORM user_group_involved_user_remove(
                old_assigner_user_id, old_group_id, old_group_domain, p_owner_business_account_id);
    END IF;

    -- Add to new if involvement key changed
    IF new_group_id IS NOT NULL
        AND (new_assigner_user_id IS DISTINCT FROM old_assigner_user_id
            OR new_group_id IS DISTINCT FROM old_group_id
            OR new_group_domain IS DISTINCT FROM old_group_domain)
    THEN
        PERFORM user_group_involved_user_add(
                new_assigner_user_id, new_group_id, new_group_domain, p_owner_business_account_id);
    END IF;
END;
$$;

select user_group_involve_reinit();
