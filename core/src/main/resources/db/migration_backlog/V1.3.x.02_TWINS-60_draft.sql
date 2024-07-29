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
VALUES ('UNCOMMITED')
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
    twin_create_count  integer,
    twin_update_count  integer,
    twin_erase_count   integer,
    draft_status_id varchar
        constraint draft_draft_status_id_fk
            references draft_status
            on update cascade,
    created_at         timestamp default CURRENT_TIMESTAMP
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
    view_permission_id         uuid
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


create table if not exists draft_twin_erase
(
    draft_id             uuid                  not null
        constraint draft_erase_draft_id_fk
            references draft
            on update cascade on delete cascade,
    twin_id              uuid                  not null -- fk here, because deletion only of existed twins
        constraint draft_twin_erase_twin_id_fk
            references twin
            on update cascade on delete cascade,
    erase_ready          boolean default false not null,
    reason_twin_id       uuid                  not null
        constraint draft_twin_erase_reason_twin_id_fk
            references twin
            on update cascade on delete cascade,
    twin_erase_reason_id varchar
        constraint draft_twin_erase_twin_erase_reason_fk
            references twin_erase_reason
            on update cascade,
    erase_twin_status_id uuid,                          -- if null, then twin will be force deleted from db
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
    cud_id       varchar not null
        constraint draft_twin_link_cud_id_fk
            references cud
            on update cascade,
    twin_link_id uuid    not null, -- no FK possible to twin_link if it's creation
    src_twin_id  uuid,             -- do we need to set in not null???
    dst_twin_id  uuid,             -- do we need to set in not null???
    link_id      uuid              -- do we need to set in not null???
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
    cud_id                 varchar not null
        constraint draft_twin_attachment_cud_id_fk
            references cud
            on update cascade,
    twin_attachment_id     uuid,
    twin_id                uuid    not null,
    twinflow_transition_id uuid,
    storage_link           varchar(255),
    view_permission_id     uuid,
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
    cud_id                  varchar not null
        constraint draft_twin_field_data_list_cud_id_fk
            references cud
            on update cascade,
    twin_field_data_list_id uuid    not null,
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
    cud_id               varchar not null
        constraint draft_twin_field_simple_cud_id_fk
            references cud
            on update cascade,
    twin_field_simple_id uuid    not null,
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
    cud_id              varchar not null
        constraint draft_twin_field_user_cud_id_fk
            references cud
            on update cascade,
    twin_field_user_id  uuid    not null,
    twin_id             uuid    not null, -- no FW, cause twin can be in creation state
    twin_class_field_id uuid    not null,
    user_id             uuid
);

create index if not exists draft_twin_field_user_draft_id_index
    on draft_twin_field_user (draft_id);
create index if not exists draft_twin_field_user_twin_id_index
    on draft_twin_field_user (twin_id);







