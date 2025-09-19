create table if not exists history_dispatch_status
(
    id varchar(20) not null,
    constraint history_dispatch_status_pk primary key(id)
);

insert into history_dispatch_status (id)
values ('NEW'),
       ('IN_PROGRESS'),
       ('DONE'),
       ('FAILED')
on conflict do nothing;

alter table history
    add column if not exists dispatch_status varchar(20) not null default 'NEW'
        constraint history_dispatch_status_id_fk
            references history_dispatch_status
            on update cascade on delete restrict;

CREATE INDEX ix_history_status_created_at_id_twin
    ON history (dispatch_status, created_at, id, twin_id);

alter table domain_user
    add column if not exists subscription_enabled boolean not null default false;
-- or add these fields to domain_subscription_event_type table ???
alter table domain
    add column if not exists dispatcher_featurer_id integer
        constraint domain_dispatcher_featurer_id_fk references featurer on
            update cascade;
alter table domain
    add column if not exists dispatcher_featurer_params hstore;


create table if not exists subscription_event_type
(
    id varchar(255) not null,
    constraint subscription_event_type_pk primary key(id)
);

insert into subscription_event_type (id)
values ('TWIN_UPDATED')
on conflict do nothing;

-- table instead of allow_client_subscribing_for_twin_cud_operations column in domain table
create table domain_subscription_event_type
(
    id                         uuid    not null
        constraint domain_subscription_event_type_pk primary key,
    domain_id                  uuid
        constraint domain_subscription_event_type_domain_id_fk
            references domain on update cascade,
    subscription_event_type_id varchar(255)
        constraint domain_subscription_event_type_event_type_id_fk
            references subscription_event_type on update cascade,
    subscription_enabled       boolean not null default true
);

create index if not exists domain_subscription_event_type_index1
    on domain_subscription_event_type (domain_id);

create index if not exists domain_subscription_event_type_index2
    on domain_subscription_event_type (subscription_event_type_id);

insert into featurer_type
values (44, 'Dispatcher', 'Dispatches messages about various events')
on conflict do nothing;

insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (4401, 44, 'org.twins.core.featurer.dispatcher.TwinEventDispatcher', 'twin events dispatcher', '',false)
on conflict (id) do nothing;

