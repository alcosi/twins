-- add new field unique_in_batch
alter table notification_channel_event
    add if not exists unique_in_batch boolean default false not null;