-- insert new featurer type
INSERT INTO featurer_type (id, name, description) VALUES (47, 'Recipient resolver', '') on conflict on constraint featurer_type_pk do nothing ;
INSERT INTO featurer_type (id, name, description) VALUES (48, 'Notifier', '') on conflict on constraint featurer_type_pk do nothing ;
INSERT INTO featurer_type (id, name, description) VALUES (49, 'Context collector', '') on conflict on constraint featurer_type_pk do nothing ;

-- insert new featurer ids
insert into featurer(id, featurer_type_id, class, name, description) values (4701, 47, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4702, 47, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4703, 47, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4801, 48, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4901, 49, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4902, 49, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4903, 49, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4904, 49, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (4905, 49, '', '', '') on conflict (id) do nothing;

create table if not exists history_notification_task_staus
(
    id varchar(100) not null
        constraint history_notification_task_staus_pk
            primary key
);

INSERT INTO history_notification_task_staus (id) VALUES ('NEED_START') on conflict on constraint history_notification_task_staus_pk do nothing ;
INSERT INTO history_notification_task_staus (id) VALUES ('IN_PROGRESS') on conflict on constraint history_notification_task_staus_pk do nothing ;
INSERT INTO history_notification_task_staus (id) VALUES ('SENT') on conflict on constraint history_notification_task_staus_pk do nothing ;
INSERT INTO history_notification_task_staus (id) VALUES ('SKIPPED') on conflict on constraint history_notification_task_staus_pk do nothing ;
INSERT INTO history_notification_task_staus (id) VALUES ('FAILED') on conflict on constraint history_notification_task_staus_pk do nothing ;

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
        constraint notification_channel_featurer_id_fk
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
    id                              uuid not null,
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
    notification_schema_id uuid not null
        constraint history_notification_task_notification_schema_id_fk
            references notification_schema
            on update cascade on delete cascade,
    history_notification_task_status_id varchar(100) default 'NEED_START'::character varying not null
        constraint history_notification_task_history_notification_task_staus_id_fk
            references history_notification_task_staus
            on update cascade on delete cascade,
    status_details varchar,
    created_at timestamp    default CURRENT_TIMESTAMP             not null,
    done_at timestamp
);

CREATE OR REPLACE FUNCTION notification_schema_detect(domainid uuid, businessaccountid uuid) RETURNS uuid AS $$
DECLARE
    schemaId UUID;
BEGIN
    -- twin in BA
    IF businessAccountId IS NOT NULL THEN
        SELECT notification_schema_id INTO schemaId
        FROM domain_business_account
        WHERE domain_id = domainId AND business_account_id = businessAccountId;
        IF FOUND AND schemaId IS NOT NULL THEN
            RETURN schemaId;
        END IF;
    END IF;

    -- return domain schema, if twin not in BA
    SELECT notification_schema_id INTO schemaId FROM domain WHERE id = domainId;
    RETURN schemaId;
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END;
$$ IMMUTABLE LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION history_notification_task_insert_schema_on_hisotry_insert()
RETURNS TRIGGER AS $$
DECLARE
    v_twin_owner_business_account_id uuid;
    v_twin_class_domain_id uuid;
    v_notification_schema_id uuid;
BEGIN
    SELECT t.owner_business_account_id, tc.domain_id
    INTO v_twin_owner_business_account_id, v_twin_class_domain_id
    FROM twin t
    JOIN twin_class tc ON t.twin_class_id = tc.id
    WHERE t.id = NEW.twin_id;

    -- Detect notification schema
    IF v_twin_class_domain_id IS NOT NULL THEN
        v_notification_schema_id := notification_schema_detect(v_twin_class_domain_id, v_twin_owner_business_account_id);
    END IF;

    -- Insert into history_notification_task only if schema is detected
    IF v_notification_schema_id IS NOT NULL THEN
        INSERT INTO history_notification_task (id, history_id, notification_schema_id)
        VALUES (gen_random_uuid(), NEW.id, v_notification_schema_id);
    END IF;

    RETURN NEW;
EXCEPTION WHEN unique_violation THEN
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
DROP TRIGGER IF EXISTS trigger_insert_history_notification ON history;
CREATE TRIGGER trigger_insert_history_notification
    AFTER INSERT ON history
    FOR EACH ROW
    EXECUTE FUNCTION history_notification_task_insert_schema_on_hisotry_insert();
END $$;

-- Update tier triggers to include notification_schema_id
CREATE OR REPLACE FUNCTION tiers_update_business_account_properties_on_tier_change() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.custom THEN
        RETURN NULL; --if custom changed to true then return. Tier properties will apply to BA, only if this tier selected on BA create or update
    END IF;

    IF OLD.permission_schema_id IS DISTINCT FROM NEW.permission_schema_id OR
       OLD.twinflow_schema_id IS DISTINCT FROM NEW.twinflow_schema_id OR
       OLD.twin_class_schema_id IS DISTINCT FROM NEW.twin_class_schema_id OR
       OLD.notification_schema_id IS DISTINCT FROM NEW.notification_schema_id OR
       OLD.custom IS DISTINCT FROM NEW.custom THEN --if custom changed to false - apply proprties to all domain BA

        UPDATE domain_business_account ba
        SET permission_schema_id = NEW.permission_schema_id,
            twinflow_schema_id = NEW.twinflow_schema_id,
            twin_class_schema_id = NEW.twin_class_schema_id,
            notification_schema_id = NEW.notification_schema_id
        WHERE tier_id = NEW.id and NEW.domain_id = domain_id;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Drop old trigger first (before dropping the function it depends on)
DROP TRIGGER IF EXISTS tiers_domain_business_account_tier_id_update_trigger ON domain_business_account;

-- Drop old function and create new one with renamed function
DROP FUNCTION IF EXISTS public.tiers_update_business_account_properties_on_self_tier_id_change();

CREATE OR REPLACE FUNCTION domain_business_account_on_tier_id_change()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE domain_business_account
    SET
        permission_schema_id = t.permission_schema_id,
        twinflow_schema_id = t.twinflow_schema_id,
        twin_class_schema_id = t.twin_class_schema_id,
        notification_schema_id = t.notification_schema_id
    FROM (SELECT permission_schema_id, twinflow_schema_id, twin_class_schema_id, notification_schema_id FROM tier WHERE id = NEW.tier_id) AS t
    WHERE domain_business_account.id = NEW.id;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Update trigger to use new function name
CREATE TRIGGER tiers_domain_business_account_tier_id_update_trigger
    AFTER UPDATE OF tier_id ON domain_business_account
    FOR EACH ROW
    WHEN (OLD.tier_id IS DISTINCT FROM NEW.tier_id)
    EXECUTE FUNCTION domain_business_account_on_tier_id_change();