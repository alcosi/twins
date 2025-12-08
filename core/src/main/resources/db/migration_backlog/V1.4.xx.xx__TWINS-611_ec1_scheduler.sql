create table if not exists scheduler
(
    id                    uuid primary key,
    domain_id             uuid references domain on update cascade on delete restrict,
    scheduler_featurer_id int                                 not null references featurer on update cascade on delete restrict,
    scheduler_params      hstore    default ''::hstore        not null,
    active                boolean                             not null,
    log_enabled           boolean                             not null,
    cron                  varchar,
    fixed_rate            int,
    description           varchar                             not null,
    created_at            timestamp default CURRENT_TIMESTAMP not null,
    updated_at            timestamp default CURRENT_TIMESTAMP not null
);

create index if not exists schedule_scheduler_featurer_id_index
    on scheduler (scheduler_featurer_id);

create index if not exists schedule_domain_id_index
    on scheduler (domain_id);

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
