create table if not exists history_dispatch_status
(
    id varchar(20) primary key
);

insert into history_dispatch_status (id)
values ('NEW'),
       ('IN_PROGRESS'),
       ('DONE'),
       ('FAILED')
on conflict do nothing;

alter table history
    add column if not exists dispatch_status varchar(20) not null default 'NEW'
            references history_dispatch_status
            on update cascade on delete restrict;

create index if not exists history_dispatch_status_created_at_id_twin_id_idx
    on history (dispatch_status, created_at, id, twin_id);

alter table domain_user
    add column if not exists subscription_enabled boolean not null default false;

create table if not exists subscription_event_type
(
    id varchar(255) primary key
);

insert into subscription_event_type (id)
values ('TWIN_UPDATED'), ('TWIN_CREATED'), ('TWIN_DELETED')
on conflict do nothing;

-- table instead of allow_client_subscribing_for_twin_cud_operations column in domain table
create table if not exists domain_subscription_event
(
    id uuid not null primary key,
    domain_id uuid not null references domain on update cascade on delete restrict,
    subscription_event_type_id varchar(255) not null references subscription_event_type on update cascade on delete restrict,
    dispatcher_featurer_id integer not null references featurer on update cascade on delete restrict,
    dispatcher_featurer_params hstore
);

create unique index if not exists domain_sub_event_domain_id_subscription_event_type_id_uidx
    on domain_subscription_event(domain_id, subscription_event_type_id);

create index if not exists domain_subscription_event_domain_id_idx
    on domain_subscription_event(domain_id);

create index if not exists domain_subscription_event_subscription_event_type_id_idx
    on domain_subscription_event(subscription_event_type_id);

create index if not exists domain_subscription_event_dispatcher_featurer_id_idx
    on domain_subscription_event(dispatcher_featurer_id);

insert into featurer_type
values (47, 'Dispatcher', 'Dispatches messages about various events')
on conflict do nothing;

insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (4701, 47, 'org.twins.core.featurer.dispatcher.TwinEventDispatcher', 'twin events dispatcher', '',false)
on conflict (id) do nothing;

