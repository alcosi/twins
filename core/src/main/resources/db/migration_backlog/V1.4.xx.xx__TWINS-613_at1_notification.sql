--todo drop -> public.
--todo add check (if not exists)
--todo new rector in db featurer_type and new featurers

-- create history_notification_recipient
create table if not exists history_notification_recipient
(
    id uuid not null
        constraint history_notification_recipient_pk
            primary key,
    domain_id uuid,
    recipient_resolver_featurer_id integer
        constraint history_notification_recipient_featurer_id_fk
            references featurer
            on update cascade,
    recipient_resolver_params hstore
);

-- create notification_schema
create table if not exists notification_schema
(
    id uuid not null
        constraint notification_schema_pk
            primary key,
    domain_id uuid
        constraint notification_schema_domain_id_fk
            references domain,
    name_i18n_id uuid not null
        constraint notification_schema_name_i18n_id_fk
            references i18n,
    description_i18n_id uuid not null
        constraint notification_schema_description_i18n_id_fk
            references i18n
);

create table if not exists notification_channel
(
    id uuid not null
        constraint notification_channel_pk
            primary key,
    domain_id uuid
    constraint notification_channel_domain_id_fk
        references domain
            on update cascade,
    notifier_featurer_id integer
        constraint notification_channel_featurer_id_fk
            references featurer
            on update cascade,
    notifier_params hstore
);

create table if not exists history_notification_context
(
    id uuid not null
        constraint history_notification_context_pk
            primary key,
    domain_id uuid
        constraint history_notification_context_domain_id_fk
            references domain,
    name_i18n_id uuid not null
        constraint history_notification_context_name_i18n_id_fk
            references i18n,
    description_i18n_id uuid not null
        constraint history_notification_context_description_i18n_id_fk
            references i18n
);

create table if not exists history_notification_context_collector
(
    id                              uuid not null,
    history_notification_context_id uuid
        constraint history_notification_context_collector_context_id_fk
            references history_notification_context,
    context_collector_featurer_id   integer
        constraint history_notification_context_collector_featurer_id_fk
            references featurer
            on update cascade,
    context_collector_params        hstore
);

create table if not exists history_notification_schema_map
(
    id                                uuid         not null
        constraint history_notification_schema_map_pk
            primary key,
    history_type_id                   varchar(255) not null
        constraint history_notification_schema_map_history_type_id_fk
            references public.history_type,
    notification_schema_id           uuid         not null
        constraint history_notification_schema_map_notification_schema_id_fk
            references public.notification_schema
            on update cascade,
    history_notification_recipient_id uuid         not null
        constraint history_notification_schema_map_history_notification_recipient_
            references public.history_notification_recipient
            on update cascade,
    notification_channel_id           uuid
        constraint history_notification_schema_map_notification_channel_id_fk
            references public.notification_channel
            on update cascade,
    history_notification_context_id   uuid         not null
        constraint history_notification_schema_map_history_notification_context_id
            references public.history_notification_context
            on update cascade
);









