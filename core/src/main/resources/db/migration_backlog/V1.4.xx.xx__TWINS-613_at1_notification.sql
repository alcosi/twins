--todo new rector in db featurer_type and new featurers

create table if not exists history_notification_recipient
(
    id                             uuid    not null
        constraint history_notification_recipient_pk
            primary key,
    domain_id                      uuid    not null
        constraint history_notification_recipient_domain_id_fk
            references domain
            on update cascade on delete cascade,
    recipient_resolver_featurer_id integer not null
        constraint history_notification_recipient_featurer_id_fk
            references featurer
            on update cascade on delete cascade,
    recipient_resolver_params      hstore
);


create table if not exists notification_schema
(
    id uuid not null
        constraint notification_schema_pk
            primary key,
    domain_id uuid not null
        constraint notification_schema_domain_id_fk
            references domain
            on update cascade on delete cascade,
    name_i18n_id uuid not null
        constraint notification_schema_name_i18n_id_fk
            references i18n
            on update cascade on delete cascade,
    description_i18n_id uuid
        constraint notification_schema_description_i18n_id_fk
            references i18n
            on update cascade on delete cascade,
);

create table if not exists notification_channel
(
    id uuid not null
        constraint notification_channel_pk
            primary key,
    domain_id uuid not null
        constraint notification_channel_domain_id_fk
            references domain
            on update cascade on delete cascade,
    notifier_featurer_id integer not null
        constraint notification_channel_featurer_id_fk
            references featurer
            on update cascade on delete cascade,
    notifier_params hstore
);

create table if not exists notification_channel_event
(
    id                              uuid         not null
        constraint notification_channel_event_pk
            primary key,
    notification_channel_id         uuid         not null
        constraint notification_channel_event_notification_channel_id_fk
            references notification_channel
            on update cascade on delete cascade,
    event_code                      varchar(255) not null,
    history_notification_context_id uuid         not null
        constraint notification_channel_event_history_notification_context_id_fk
            references history_notification_context
            on update cascade on delete cascade
);

create table if not exists history_notification_context
(
    id uuid not null
        constraint history_notification_context_pk
            primary key,
    domain_id uuid not null
        constraint history_notification_context_domain_id_fk
            references domain
            on update cascade on delete cascade,
    name_i18n_id uuid not null
        constraint history_notification_context_name_i18n_id_fk
            references i18n
            on update cascade on delete cascade,
    description_i18n_id uuid
        constraint history_notification_context_description_i18n_id_fk
            references i18n
            on update cascade on delete cascade,
);

create table if not exists history_notification_context_collector
(
    id                              uuid not null,
    history_notification_context_id uuid not null
        constraint history_notification_context_collector_context_id_fk
            references history_notification_context
            on update cascade on delete cascade,
    context_collector_featurer_id   integer not null
        constraint history_notification_context_collector_featurer_id_fk
            references featurer
            on update cascade on delete cascade,
    context_collector_params        hstore
);

create table if not exists history_notification_schema_map
(
    id                                uuid         not null
        constraint history_notification_schema_map_pk
            primary key,
    history_type_id                   varchar(255) not null
        constraint history_notification_schema_map_history_type_id_fk
            references history_type
            on update cascade on delete cascade,
    notification_schema_id            uuid         not null
        constraint history_notification_schema_map_notification_schema_id_fk
            references notification_schema
            on update cascade on delete cascade,
    history_notification_recipient_id uuid         not null
        constraint history_notification_schema_map_history_notification_recipient_
            references history_notification_recipient
            on update cascade on delete cascade,
    notification_channel_event_id     uuid         not null
        constraint history_notification_schema_map_notification_channel_event_id_f
            references notification_channel_event
            on update cascade on delete cascade,
    constraint history_notification_schema_map_uq
        unique (history_type_id, notification_schema_id, history_notification_recipient_id,
                notification_channel_event_id)
);

alter table domain_business_account
    add if not exists notification_schema_id uuid
        constraint domain_business_account_notification_schema_id_fk
            references notification_schema
            on update cascade on delete cascade;

alter table domain
    add if not exists notification_schema_id uuid
        constraint domain_notification_schema_id_fk
            references notification_schema
            on update cascade on delete cascade;

alter table tier
    add if not exists notification_schema_id uuid
        constraint tier_notification_schema_id_fk
            references notification_schema
            on update cascade on delete cascade;

create table if not exists history_notification_task
(
    id         uuid                                               not null
        constraint history_notification_task_pk
            primary key,
    history_id uuid                                               not null
        constraint history_notification_task_history_id_fk
            references history
            on update cascade on delete cascade,
    status     varchar(100) default 'NEED_START'::character varying not null,
    created_at timestamp    default CURRENT_TIMESTAMP             not null,
    updated_at timestamp
);

CREATE OR REPLACE FUNCTION insert_history_notification_task()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO history_notification_task (id, history_id)
VALUES (gen_random_uuid(), NEW.id);
RETURN NEW;
EXCEPTION WHEN unique_violation THEN
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.* IS DISTINCT FROM OLD.*) THEN
        NEW.updated_at = CURRENT_TIMESTAMP;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
DROP TRIGGER IF EXISTS trigger_insert_history_notification ON history;
CREATE TRIGGER trigger_insert_history_notification
    AFTER INSERT ON history
    FOR EACH ROW
    EXECUTE FUNCTION insert_history_notification_task();

DROP TRIGGER IF EXISTS trigger_update_notification_timestamp ON history_notification_task;
CREATE TRIGGER trigger_update_notification_timestamp
    BEFORE UPDATE ON history_notification_task
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();
END $$;