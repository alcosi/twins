alter table history_notification_recipient_collector
    add if not exists exclude boolean default false;

alter table history_notification_recipient_collector
    drop column if exists "order";
