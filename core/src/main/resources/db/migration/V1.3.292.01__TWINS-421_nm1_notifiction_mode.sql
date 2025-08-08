create table if not exists notification_mode
(
    id varchar(50) not null
        constraint notification_mode_pk
            primary key
);

INSERT INTO notification_mode (id) VALUES ('ASYNC') on conflict do nothing;
INSERT INTO notification_mode (id) VALUES ('SYNC_AND_LOG_ON_EXCEPTION') on conflict do nothing;
INSERT INTO notification_mode (id) VALUES ('SYNC_AND_THROWS_ON_EXCEPTION') on conflict do nothing;

alter table notification_email
    add if not exists notification_mode_id varchar;

update notification_email set notification_mode_id = 'ASYNC' where notification_mode_id is null;

alter table notification_email
    alter column notification_mode_id set not null;

alter table public.notification_email
    drop column if exists async;
