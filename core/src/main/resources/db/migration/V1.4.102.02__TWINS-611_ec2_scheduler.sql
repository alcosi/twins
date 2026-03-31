create table if not exists scheduler
(
    id                    uuid primary key,
    domain_id             uuid references domain on update cascade on delete restrict,
    scheduler_featurer_id int                                 not null references featurer on update cascade on delete restrict,
    scheduler_params      hstore,
    active                boolean                             not null,
    log_enabled           boolean                             not null,
    cron                  varchar,
    fixed_rate            int,
    description           varchar                             not null,
    created_at            timestamp default CURRENT_TIMESTAMP not null,
    updated_at            timestamp default CURRENT_TIMESTAMP not null
);

create index if not exists scheduler_scheduler_featurer_id_index
    on scheduler (scheduler_featurer_id);

create index if not exists scheduler_domain_id_index
    on scheduler (domain_id);

create unique index if not exists scheduler_scheduler_featurer_id_uindex
    on scheduler (scheduler_featurer_id);

create table if not exists scheduler_log
(
    id             uuid primary key,
    scheduler_id   uuid                                not null references scheduler on update cascade on delete cascade,
    created_at     timestamp default CURRENT_TIMESTAMP not null,
    result         varchar                             not null,
    execution_time bigint                              not null
);

create index if not exists scheduler_log_scheduler_id_index
    on scheduler_log (scheduler_id);

insert into scheduler(id, domain_id, scheduler_featurer_id, scheduler_params, active, log_enabled, cron, fixed_rate, description, created_at, updated_at)
values
    ('00000000-0000-0000-0015-000000000001', null, 5001, null, true, true, null, 2000, 'Scheduler for clearing external file storages after twin/attachment deletion', now(), now()),
    ('00000000-0000-0000-0015-000000000002', null, 5002, null, true, true, '1 0 0 * * *', null, 'Scheduler for clearing twin archive table', now(), now()),
    ('00000000-0000-0000-0015-000000000003', null, 5003, null, true, true, null, 2000, 'Scheduler for executing twin changes', now(), now()),
    ('00000000-0000-0000-0015-000000000004', null, 5004, null, true, true, null, 500, 'Scheduler for executing draft erases', now(), now()),
    ('00000000-0000-0000-0015-000000000005', null, 5005, null, true, true, null, 500, 'Scheduler for executing draft commits', now(), now()),
    ('00000000-0000-0000-0015-000000000006', null, 5006, null, true, true, '2 0 0 * * *', null, 'Scheduler for cleaning scheduler log table', now(), now()),
    ('00000000-0000-0000-0015-000000000007', null, 5007, null, true, true, '3 0 0 * * *', null, 'Scheduler for cleaning attachment delete task table', now(), now()),
    ('00000000-0000-0000-0015-000000000008', null, 5008, null, true, true, null, 500, 'Scheduler for history notifications sending', now(), now()),
    ('00000000-0000-0000-0015-000000000009', null, 5009, null, true, true, '4 0 0 * * *', null, 'Scheduler for cleaning history notification task table', now(), now())
on conflict do nothing;
