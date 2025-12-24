-- new history_type
INSERT INTO history_type VALUES ('commentCreate', 'Comment ''${comment.text}'' was added', 'softEnabled') on conflict on constraint history_type_pkey do nothing;

-- insert new featurer type
INSERT INTO featurer_type (id, name, description) VALUES (47, 'Recipient resolver', '') on conflict on constraint featurer_type_pk do nothing ;
INSERT INTO featurer_type (id, name, description) VALUES (48, 'Notifier', '') on conflict on constraint featurer_type_pk do nothing ;
INSERT INTO featurer_type (id, name, description) VALUES (49, 'Context collector', '') on conflict on constraint featurer_type_pk do nothing ;

-- insert new featurer ids
insert into featurer(id, featurer_type_id, class, name, description) values (4701, 47, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4702, 47, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4703, 47, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4704, 47, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4705, 47, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4801, 48, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4901, 49, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4902, 49, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4903, 49, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4904, 49, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4905, 49, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4906, 49, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (1617, 16, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (1618, 16, '', '', '') on conflict (id) do nothing;

INSERT INTO i18n_type (id, name) VALUES ('notificationContextName', 'Notification context name') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO i18n_type (id, name) VALUES ('notificationContextDescription', 'Notification context description') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO i18n_type (id, name) VALUES ('notificationSchemaName', 'Notification schema name') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO i18n_type (id, name) VALUES ('notificationSchemaDescription', 'Notification schema description') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO i18n_type (id, name) VALUES ('recipientName', 'Recipient name') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO i18n_type (id, name) VALUES ('recipientDescription', 'Recipient description') on conflict on constraint i18n_type_pk do nothing ;


create table if not exists history_notification_task_status
(
    id varchar(100) not null
        constraint history_notification_task_status_pk
            primary key
);

INSERT INTO history_notification_task_status (id) VALUES ('NEED_START') on conflict on constraint history_notification_task_status_pk do nothing ;
INSERT INTO history_notification_task_status (id) VALUES ('IN_PROGRESS') on conflict on constraint history_notification_task_status_pk do nothing ;
INSERT INTO history_notification_task_status (id) VALUES ('SENT') on conflict on constraint history_notification_task_status_pk do nothing ;
INSERT INTO history_notification_task_status (id) VALUES ('SKIPPED') on conflict on constraint history_notification_task_status_pk do nothing ;
INSERT INTO history_notification_task_status (id) VALUES ('FAILED') on conflict on constraint history_notification_task_status_pk do nothing ;

create table if not exists history_notification_recipient
(
    id                             uuid    not null
        constraint history_notification_recipient_pk
            primary key,
    domain_id                      uuid    not null
        constraint history_notification_recipient_domain_id_fk
            references domain
            on update cascade on delete cascade,
    name_i18n_id uuid not null
        constraint history_notification_recipient_name_i18n_id_fk
            references i18n
            on update cascade on delete cascade,
    description_i18n_id uuid
        constraint history_notification_recipient_description_i18n_id_fk
            references i18n
            on update cascade on delete cascade
);

create table if not exists history_notification_recipient_collector
(
    id                              uuid not null
        constraint history_notification_recipient_collector_pk
            primary key,
    history_notification_recipient_id uuid not null
        constraint history_notification_recipient_collector_recipient_id_fk
            references history_notification_recipient
            on update cascade on delete cascade,
    recipient_resolver_featurer_id integer not null
        constraint history_notification_recipient_collector_featurer_id_fk
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
            on update cascade on delete cascade
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
        constraint notification_channel_notifier_featurer_id_fk
            references featurer
            on update cascade on delete cascade,
    notifier_params hstore
);

create table if not exists notification_context
(
    id uuid not null
        constraint notification_context_pk
            primary key,
    domain_id uuid not null
        constraint notification_context_domain_id_fk
            references domain
            on update cascade on delete cascade,
    name_i18n_id uuid not null
        constraint notification_context_name_i18n_id_fk
            references i18n
            on update cascade on delete cascade,
    description_i18n_id uuid
        constraint notification_context_description_i18n_id_fk
            references i18n
            on update cascade on delete cascade
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
    notification_context_id uuid         not null
        constraint notification_channel_event_notification_context_id_fk
            references notification_context
            on update cascade on delete cascade
);

create table if not exists notification_context_collector
(
    id                              uuid not null
        constraint notification_context_collector_pk
            primary key,
    notification_context_id uuid not null
        constraint notification_context_collector_context_id_fk
            references notification_context
            on update cascade on delete cascade,
    context_collector_featurer_id   integer not null
        constraint notification_context_collector_featurer_id_fk
            references featurer
            on update cascade on delete cascade,
    context_collector_params        hstore
);

create table if not exists history_notification_schema_map
(
    id                                uuid         not null
        constraint history_notification_schema_map_pk
            primary key,
    twin_class_id            uuid         not null
        constraint history_notification_schema_map_twin_class_id_id_fk
            references twin_class
            on update cascade on delete cascade,
    twin_class_field_id uuid
        constraint history_notification_schema_map_twin_class_field_id_fk
            references twin_class_field
            on update cascade on delete cascade,
    history_type_id                   varchar(255) not null
        constraint history_notification_schema_map_history_type_id_fk
            references history_type
            on update cascade on delete cascade,
    notification_schema_id            uuid         not null
        constraint history_notification_schema_map_notification_schema_id_fk
            references notification_schema
            on update cascade on delete cascade,
    history_notification_recipient_id uuid         not null
        constraint history_notification_schema_map_recipient_id_fk
            references history_notification_recipient
            on update cascade on delete cascade,
    notification_channel_event_id     uuid         not null
        constraint history_notification_schema_map_channel_event_id_fk
            references notification_channel_event
            on update cascade on delete cascade,
    twin_validator_set_id uuid
        constraint history_notification_schema_map_twin_validator_set_id_fk
            references twin_validator_set,
    twin_validator_set_invert boolean default false not null,
    constraint history_notification_schema_map_uq
    unique (history_type_id, notification_schema_id, twin_class_id, twin_class_field_id, history_notification_recipient_id,
            twin_validator_set_id, twin_validator_set_invert, notification_channel_event_id)
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
    notification_schema_id uuid not null
        constraint history_notification_task_notification_schema_id_fk
            references notification_schema
            on update cascade on delete cascade,
    history_notification_task_status_id varchar(100) default 'NEED_START'::character varying not null
        constraint history_notification_task_notification_task_status_id_fk
            references history_notification_task_status
            on update cascade on delete cascade,
    status_details varchar,
    created_at timestamp    default CURRENT_TIMESTAMP             not null,
    done_at timestamp
);

CREATE OR REPLACE FUNCTION notification_schema_detect(
    p_domain_id uuid,
    p_business_account_id uuid
) RETURNS uuid AS $$
DECLARE
    v_schema_id UUID;
BEGIN
    IF p_business_account_id IS NOT NULL THEN
        SELECT notification_schema_id INTO v_schema_id
        FROM domain_business_account
        WHERE domain_id = p_domain_id
          AND business_account_id = p_business_account_id;

        IF FOUND AND v_schema_id IS NOT NULL THEN
            RETURN v_schema_id;
        END IF;
    END IF;

    SELECT notification_schema_id INTO v_schema_id
    FROM domain
    WHERE id = p_domain_id;

    RETURN v_schema_id;
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION twin_context_get(
    p_twin_id uuid
) RETURNS TABLE (
                    owner_business_account_id uuid,
                    domain_id uuid
                ) AS $$
BEGIN
    RETURN QUERY
        SELECT
            t.owner_business_account_id,
            tc.domain_id
        FROM twin t
                 JOIN twin_class tc ON t.twin_class_id = tc.id
        WHERE t.id = p_twin_id;
END;
$$ LANGUAGE plpgsql STABLE;

CREATE OR REPLACE FUNCTION history_notification_task_create(
    p_history_id uuid,
    p_twin_id uuid
) RETURNS void AS $$
DECLARE
    v_context RECORD;
    v_notification_schema_id uuid;
BEGIN
    SELECT * INTO v_context
    FROM twin_context_get(p_twin_id);

    IF v_context.domain_id IS NULL THEN
        RETURN;
    END IF;

    v_notification_schema_id := notification_schema_detect(
            v_context.domain_id,
            v_context.owner_business_account_id
                                );

    IF v_notification_schema_id IS NOT NULL THEN
        INSERT INTO history_notification_task (
            id,
            history_id,
            notification_schema_id
        ) VALUES (
                     gen_random_uuid(),
                     p_history_id,
                     v_notification_schema_id
                 );
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION business_account_properties_update_on_tier_change(
    p_tier_id uuid
) RETURNS void AS $$
BEGIN
    UPDATE domain_business_account ba
    SET
        permission_schema_id = t.permission_schema_id,
        twinflow_schema_id = t.twinflow_schema_id,
        twin_class_schema_id = t.twin_class_schema_id,
        notification_schema_id = t.notification_schema_id
    FROM tier t
    WHERE ba.tier_id = p_tier_id
      AND t.id = p_tier_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION business_account_update_all(
    p_tier_id uuid,
    p_domain_id uuid,
    p_permission_schema_id uuid,
    p_twinflow_schema_id uuid,
    p_twin_class_schema_id uuid,
    p_notification_schema_id uuid,
    p_custom boolean
) RETURNS void AS $$
BEGIN
    IF p_custom THEN
        RETURN;
    END IF;

    UPDATE domain_business_account ba
    SET
        permission_schema_id = p_permission_schema_id,
        twinflow_schema_id = p_twinflow_schema_id,
        twin_class_schema_id = p_twin_class_schema_id,
        notification_schema_id = p_notification_schema_id
    WHERE ba.tier_id = p_tier_id
      AND ba.domain_id = p_domain_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION history_after_insert_wrapper()
    RETURNS TRIGGER AS $$
BEGIN
    PERFORM history_notification_task_create(NEW.id, NEW.twin_id);
    RETURN NEW;
EXCEPTION
    WHEN unique_violation THEN
        RETURN NEW;
    WHEN OTHERS THEN
        RAISE WARNING 'Error in history_after_insert_wrapper: %', SQLERRM;
        RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION tier_after_update_wrapper()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.permission_schema_id IS DISTINCT FROM NEW.permission_schema_id OR
       OLD.twinflow_schema_id IS DISTINCT FROM NEW.twinflow_schema_id OR
       OLD.twin_class_schema_id IS DISTINCT FROM NEW.twin_class_schema_id OR
       OLD.notification_schema_id IS DISTINCT FROM NEW.notification_schema_id OR
       (OLD.custom IS DISTINCT FROM NEW.custom AND NOT NEW.custom) THEN

        PERFORM business_account_update_all(
                NEW.id,
                NEW.domain_id,
                NEW.permission_schema_id,
                NEW.twinflow_schema_id,
                NEW.twin_class_schema_id,
                NEW.notification_schema_id,
                NEW.custom
                );
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION domain_business_account_after_update_wrapper()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.tier_id IS DISTINCT FROM NEW.tier_id THEN
        PERFORM business_account_properties_update_on_tier_change(NEW.tier_id);
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS history_after_insert_trigger ON history;
CREATE TRIGGER history_after_insert_trigger
    AFTER INSERT ON history
    FOR EACH ROW
EXECUTE FUNCTION history_after_insert_wrapper();

DROP TRIGGER IF EXISTS tier_after_update_trigger ON tier;
CREATE TRIGGER tier_after_update_trigger
    AFTER UPDATE ON tier
    FOR EACH ROW
EXECUTE FUNCTION tier_after_update_wrapper();

DROP TRIGGER IF EXISTS domain_business_account_after_update_trigger ON domain_business_account;
CREATE TRIGGER domain_business_account_after_update_trigger
    AFTER UPDATE OF tier_id ON domain_business_account
    FOR EACH ROW
EXECUTE FUNCTION domain_business_account_after_update_wrapper();