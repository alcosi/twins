create table if not exists cud
(
    id varchar(10) not null primary key
);

insert into cud
values ('CREATE')
on conflict (id) do nothing;
insert into cud
values ('UPDATE')
on conflict (id) do nothing;
insert into cud
values ('DELETE')
on conflict (id) do nothing;

create table if not exists draft_status
(
    id varchar(255) not null
        constraint draft_status_pk
            primary key
);

INSERT INTO draft_status (id)
VALUES ('UNDER_CONSTRUCTION')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('ERASE_SCOPE_COLLECT_PLANNED')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('ERASE_SCOPE_COLLECT_NEED_START')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('ERASE_SCOPE_COLLECT_EXCEPTION')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('ERASE_SCOPE_COLLECT_IN_PROGRESS')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('CONSTRUCTION_EXCEPTION')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('NORMALIZE_EXCEPTION')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('CHECK_CONFLICTS_EXCEPTION')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('UNCOMMITED')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('COMMIT_NEED_START')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('COMMIT_IN_PROGRESS')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('COMMIT_EXCEPTION')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('LOCKED')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('OUT_OF_DATE')
on conflict (id) do nothing;
INSERT INTO draft_status (id)
VALUES ('COMMITED')
on conflict (id) do nothing;

create table if not exists draft
(
    id                 uuid                    not null
        constraint draft_pk
            primary key,
    domain_id            uuid not null
        constraint domain_business_account_domain_id_fk
            references domain
            on update cascade on delete cascade,
    business_account_id  uuid
        constraint domain_business_account_business_account_id_fk
            references business_account
            on update cascade on delete cascade,
    created_by_user_id uuid                    not null
        constraint draft_created_by_user_id_fk
            references "user"
            on update cascade,
    draft_status_id varchar
        constraint draft_draft_status_id_fk
            references draft_status
            on update cascade,
    draft_status_details varchar,
    created_at         timestamp default CURRENT_TIMESTAMP,
    twin_erase_count integer not null default 0,
    twin_erase_irrevocable_count integer not null default 0,
    twin_erase_status_count integer not null default 0,
    twin_persist_count integer not null default 0,
    twin_persist_create_count integer not null default 0,
    twin_persist_update_count integer not null default 0,
    twin_link_create_count integer not null default 0,
    twin_link_update_count integer not null default 0,
    twin_link_delete_count integer not null default 0,
    twin_attachment_create_count integer not null default 0,
    twin_attachment_update_count integer not null default 0,
    twin_attachment_delete_count integer not null default 0,
    twin_marker_create_count integer not null default 0,
    twin_marker_delete_count integer not null default 0,
    twin_tag_create_count integer not null default 0,
    twin_tag_delete_count integer not null default 0,
    twin_field_simple_create_count integer not null default 0,
    twin_field_simple_update_count integer not null default 0,
    twin_field_simple_delete_count integer not null default 0,
    twin_field_user_create_count integer not null default 0,
    twin_field_user_update_count integer not null default 0,
    twin_field_user_delete_count integer not null default 0,
    twin_field_data_list_create_count integer not null default 0,
    twin_field_data_list_update_count integer not null default 0,
    twin_field_data_list_delete_count integer not null default 0
);


create table if not exists draft_twin_persist
(
    id                         uuid         not null
        constraint draft_twin_persist_pk
            primary key,
    draft_id                   uuid         not null
        constraint draft_twin_persist_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis         bigint  not null,
    create_else_update         boolean      not null, -- true for create, false for update
--     persist_type_id            varchar      not null
--         constraint draft_twin_persist_persist_type_fk
--             references persist_type
--             on update cascade,
    twin_id                    uuid         not null, -- no fk here, because it can be new twin
    twin_class_id              uuid,
    head_twin_id               uuid,
    external_id                varchar(100),
    twin_status_id             uuid,
    name                       varchar(100),
    description                text,
    created_by_user_id         uuid ,
    assigner_user_id           uuid,
    owner_business_account_id  uuid,
    owner_user_id              uuid,
    view_permission_id         uuid,
    conflict_description       varchar(255)
);

create index if not exists draft_twin_persist_draft_id_index
    on draft_twin_persist (draft_id);
create index if not exists draft_twin_persist_head_twin_id_index
    on draft_twin_persist (head_twin_id);
create index if not exists draft_twin_persist_twin_id_index
    on draft_twin_persist (twin_id);

create table if not exists twin_erase_reason
(
    id varchar(255) not null
        constraint twin_erase_reason_pk
            primary key
);

INSERT INTO twin_erase_reason (id)
VALUES ('TARGET')
on conflict (id) do nothing;
INSERT INTO twin_erase_reason (id)
VALUES ('CHILD')
on conflict (id) do nothing;
INSERT INTO twin_erase_reason (id)
VALUES ('LINK')
on conflict (id) do nothing;
INSERT INTO twin_erase_reason (id)
VALUES ('FACTORY')
on conflict (id) do nothing;


create table if not exists draft_twin_erase_status
(
    id varchar(255) not null
        constraint draft_twin_erase_status_pk
            primary key
);

INSERT INTO draft_twin_erase_status (id)
VALUES ('UNDETECTED')
on conflict (id) do nothing;
INSERT INTO draft_twin_erase_status (id)
VALUES ('IRREVOCABLE_ERASE_DETECTED')
on conflict (id) do nothing;
INSERT INTO draft_twin_erase_status (id)
VALUES ('IRREVOCABLE_ERASE_HANDLED')
on conflict (id) do nothing;
INSERT INTO draft_twin_erase_status (id)
VALUES ('CASCADE_DELETION_PAUSE')
on conflict (id) do nothing;
INSERT INTO draft_twin_erase_status (id)
VALUES ('CASCADE_DELETION_EXTRACTED')
on conflict (id) do nothing;
INSERT INTO draft_twin_erase_status (id)
VALUES ('STATUS_CHANGE_ERASE_DETECTED')
on conflict (id) do nothing;
INSERT INTO draft_twin_erase_status (id)
VALUES ('SKIP_DETECTED')
on conflict (id) do nothing;
INSERT INTO draft_twin_erase_status (id)
VALUES ('LOCK_DETECTED')
on conflict (id) do nothing;

create table if not exists draft_twin_erase
(
    draft_id             uuid                  not null
        constraint draft_erase_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis         bigint  not null,
    twin_id              uuid                  not null -- fk here, because deletion only of existed twins
        constraint draft_twin_erase_twin_id_fk
            references twin
            on update cascade on delete cascade,
    draft_twin_erase_status_id varchar
        constraint draft_twin_erase_draft_twin_erase_status_fk
            references draft_twin_erase_status
            on update cascade,
    status_details                       varchar(255),
    twin_erase_reason_id varchar
        constraint draft_twin_erase_twin_erase_reason_fk
            references twin_erase_reason
            on update cascade,
    reason_twin_id       uuid
        constraint draft_twin_erase_reason_twin_id_fk
            references twin
            on update cascade on delete cascade,
    cascade_break_twin_id       uuid, -- no FK, because it can be relinked to some new, currently not persisted twin
    reason_link_id       uuid
        constraint draft_twin_erase_reason_link_id_fk
            references link
            on update cascade on delete cascade,

    constraint draft_twin_erase_pk
        primary key (draft_id, twin_id)
);

create index if not exists draft_twin_erase_draft_id_index
    on draft_twin_erase (draft_id);
create index if not exists draft_twin_erase_twin_id_index
    on draft_twin_erase (twin_id);

-- common table for CU operations? but not for delete
create table if not exists draft_twin_link
(
    id           uuid    not null
        constraint draft_twin_link_pk
            primary key,
    draft_id     uuid    not null
        constraint draft_twin_link_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis         bigint  not null,
    cud_id       varchar not null
        constraint draft_twin_link_cud_id_fk
            references cud
            on update cascade,
    twin_link_id uuid,          -- no FK possible to twin_link if it's creation
    src_twin_id  uuid,             -- do we need to set in not null???
    dst_twin_id  uuid,             -- do we need to set in not null???
    link_id      uuid,              -- do we need to set in not null???
    created_by_user_id uuid
);

create index if not exists draft_twin_link_draft_id_index
    on draft_twin_link (draft_id);
create index if not exists draft_twin_link_src_twin_id_index
    on draft_twin_link (src_twin_id);

create table if not exists draft_twin_attachment
(
    id                     uuid    not null
        constraint draft_twin_attachment_pk
            primary key,
    draft_id               uuid    not null
        constraint draft_twin_attachment_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis         bigint  not null,
    cud_id                 varchar not null
        constraint draft_twin_attachment_cud_id_fk
            references cud
            on update cascade,
    twin_attachment_id     uuid,
    twin_id                uuid    not null,
    twinflow_transition_id uuid,
    storage_link           varchar(255),
    view_permission_id     uuid,
    created_by_user_id         uuid,
    external_id            varchar,
    title                  varchar,
    description            varchar,
    twin_comment_id        uuid,
    twin_class_field_id    uuid
);

create index if not exists draft_twin_attachment_draft_id_index
    on draft_twin_attachment (draft_id);
create index if not exists draft_twin_attachment_twin_id_index
    on draft_twin_attachment (twin_id);


create table if not exists draft_twin_marker
(
    id                         uuid    not null
        constraint draft_twin_marker_pk
            primary key,
    draft_id                   uuid    not null
        constraint draft_twin_marker_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis         bigint  not null,
    twin_id                    uuid    not null,               -- no FW, cause twin can be in creation state
    create_else_delete         boolean not null default false, -- true for create, false for delete
    marker_data_list_option_id uuid
);

create index if not exists draft_twin_marker_draft_id_index
    on draft_twin_marker (draft_id);
create index if not exists draft_twin_marker_twin_id_index
    on draft_twin_marker (twin_id);


create table if not exists draft_twin_tag
(
    id                      uuid    not null
        constraint draft_twin_tag_draft_pk
            primary key,
    draft_id                uuid    not null
        constraint draft_twin_tag_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis         bigint  not null,
    twin_id                 uuid    not null,               -- no FW, cause twin can be in creation state
    create_else_delete      boolean not null default false, -- true for create, false for delete
    tag_data_list_option_id uuid                            -- new tags must be created before
);

create index if not exists draft_twin_tag_draft_id_index
    on draft_twin_tag (draft_id);
create index if not exists draft_twin_tag_twin_id_index
    on draft_twin_tag (twin_id);

create table if not exists draft_twin_field_data_list
(
    id                      uuid    not null
        constraint draft_twin_field_data_list_pk
            primary key,
    draft_id                uuid    not null
        constraint draft_twin_field_data_list_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis         bigint  not null,
    cud_id                  varchar not null
        constraint draft_twin_field_data_list_cud_id_fk
            references cud
            on update cascade,
    twin_field_data_list_id uuid,
    twin_id                 uuid    not null, -- no FW, cause twin can be in creation state
    twin_class_field_id     uuid    not null,
    data_list_option_id     uuid
);

create index if not exists draft_twin_field_data_list_draft_id_index
    on draft_twin_field_data_list (draft_id);
create index if not exists draft_twin_field_data_list_twin_id_index
    on draft_twin_field_data_list (twin_id);

create table if not exists draft_twin_field_simple
(
    id                   uuid    not null
        constraint draft_twin_field_simple_pk
            primary key,
    draft_id             uuid    not null
        constraint draft_twin_field_simple_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis         bigint  not null,
    cud_id               varchar not null
        constraint draft_twin_field_simple_cud_id_fk
            references cud
            on update cascade,
    twin_field_simple_id uuid,
    twin_id              uuid    not null, -- no FW, cause twin can be in creation state
    twin_class_field_id uuid    not null,
    value                text
);

create index if not exists draft_twin_field_simple_draft_id_index
    on draft_twin_field_simple (draft_id);
create index if not exists draft_twin_field_simple_twin_id_index
    on draft_twin_field_simple (twin_id);

create table if not exists draft_twin_field_user
(
    id                  uuid    not null
        constraint draft_twin_field_user_pk
            primary key,
    draft_id            uuid    not null
        constraint draft_twin_field_user_draft_id_fk
            references draft
            on update cascade on delete cascade,
    time_in_millis         bigint  not null,
    cud_id              varchar not null
        constraint draft_twin_field_user_cud_id_fk
            references cud
            on update cascade,
    twin_field_user_id  uuid,
    twin_id             uuid    not null, -- no FW, cause twin can be in creation state
    twin_class_field_id uuid    not null,
    user_id             uuid
);

create index if not exists draft_twin_field_user_draft_id_index
    on draft_twin_field_user (draft_id);
create index if not exists draft_twin_field_user_twin_id_index
    on draft_twin_field_user (twin_id);


create table if not exists draft_history
(
    id                  uuid                                not null
        constraint draft_history_pk
            primary key,
    draft_id            uuid    not null
        constraint draft_history_draft_id_fk
            references draft
            on update cascade on delete cascade,
    twin_id             uuid                                not null,  -- no FW, cause twin can be in creation state
    created_at          timestamp default CURRENT_TIMESTAMP not null,
    actor_user_id       uuid                                not null
        constraint draft_history_user_id_fk
            references "user"
            on update cascade,
    history_type_id     varchar                             not null
        constraint draft_history_history_type_id_fk
            references history_type
            on update cascade on delete restrict,
    twin_class_field_id uuid
        constraint draft_history_twin_class_field_id_fk
            references twin_class_field
            on update cascade on delete set null,
    context             jsonb,
    snapshot_message    text
);

create index if not exists history_twin_id_index
    on history (twin_id);

CREATE OR REPLACE FUNCTION nullifyIfNecessary(newValue ANYELEMENT, oldValue ANYELEMENT) RETURNS ANYELEMENT AS
$$
BEGIN
    IF -- nullify marker
        LOWER(newValue::varchar) = 'ffffffff-ffff-ffff-ffff-ffffffffffff'
    THEN
        RETURN null;
    ELSEIF -- no changes marker
        newValue is null
    THEN
        RETURN oldValue;
    ELSE
        RETURN newValue;
    END IF;
END;
$$ LANGUAGE plpgsql;






