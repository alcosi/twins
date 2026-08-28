alter table history_notification_task
    add column if not exists attempt_count integer not null default 0;
