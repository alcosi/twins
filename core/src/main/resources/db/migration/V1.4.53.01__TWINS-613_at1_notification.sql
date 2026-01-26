alter table history_notification_recipient_collector
    add if not exists "order" integer default 1 not null;