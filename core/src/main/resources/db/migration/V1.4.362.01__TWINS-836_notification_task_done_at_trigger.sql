-- done_at is owned by the DB for the SENT transition (TWINS-836): the application never writes the
-- column (entity: updatable = false, the bulk status update does not include it). The wrapper trigger
-- fires per row on any UPDATE — including the grouped bulk UPDATE ... WHERE id IN — so every task gets
-- its own precise timestamp (clock_timestamp, not the transaction-fixed now()) at the moment it turns SENT.

-- dedicated procedure ({table}_{action} convention): computes the done_at for a status transition
create or replace function history_notification_task_done_at_touch(new_status varchar, old_status varchar, current_done_at timestamp)
    returns timestamp
    language plpgsql
as
$$
BEGIN
    IF new_status = 'SENT' AND old_status IS DISTINCT FROM 'SENT' THEN
        RETURN clock_timestamp();
    END IF;
    RETURN current_done_at;
END;
$$;

-- wrapper function: orchestration only, business logic is delegated
create or replace function history_notification_task_before_update_wrapper()
    returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.history_notification_task_status_id IS DISTINCT FROM OLD.history_notification_task_status_id THEN
        NEW.done_at := history_notification_task_done_at_touch(
                NEW.history_notification_task_status_id,
                OLD.history_notification_task_status_id,
                NEW.done_at);
    END IF;
    RETURN NEW;
END;
$$;

drop trigger if exists history_notification_task_before_update_wrapper_trigger on history_notification_task;

-- fires only when the status column is listed in the SET clause (conditional execution convention)
create trigger history_notification_task_before_update_wrapper_trigger
    before update of history_notification_task_status_id
    on history_notification_task
    for each row
execute procedure history_notification_task_before_update_wrapper();
