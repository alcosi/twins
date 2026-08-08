alter table if exists scheduler
    add column if not exists alert_execution_time bigint;
