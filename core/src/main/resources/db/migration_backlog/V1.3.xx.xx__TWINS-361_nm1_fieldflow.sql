create table if not exists twin_class_field_motion_schema
(
    id                     uuid not null
        constraint twin_class_field_motion_schema_pk
            primary key,
    name_i18n_id           uuid
        constraint twin_class_field_motion_schema_name_i18n_id_fk
            references i18n
            on update cascade,
    description_i18n_id    uuid
        constraint twin_class_field_motion_schema_description_i18n_id_fk
            references i18n
            on update cascade,
    created_by_user_id     uuid
        constraint twin_class_field_motion_schema_user_id_fk
            references "user"
            on update cascade,
    created_at             timestamp default CURRENT_TIMESTAMP
);

create index if not exists twin_class_field_motion_schema_name_i18n_id_index
    on twin_class_field_motion_schema (name_i18n_id);
create index if not exists twin_class_field_motion_schema_description_i18n_id_index
    on twin_class_field_motion_schema (description_i18n_id);
create index if not exists twin_class_field_motion_schema_created_by_user_id_index
    on twin_class_field_motion_schema (created_by_user_id);


create table if not exists twin_class_field_motion
(
    id                           uuid not null
        constraint twin_class_field_motion_pk
            primary key,
    twin_class_field_motion_schema_id                  uuid not null
        constraint twin_class_field_motion_twin_class_field_motion_schema_id_fk
            references twin_class_field_motion_schema
            on update cascade,
    twin_class_id           uuid
        constraint twin_class_field_motion_twin_class_id_fk
            references twin_class
            on update cascade,
    twin_class_field_id           uuid not null
        constraint twin_class_field_motion_twin_class_field_id_fk
            references twin_class_field
            on update cascade,
    name_i18n_id                 uuid not null
        constraint twin_class_field_motion_name_i18n_id_fk
            references i18n
            on update cascade,
    permission_id                uuid
        constraint twin_class_field_motion_permission_id_fk
            references permission
            on update cascade,
    description_i18n_id          uuid
        constraint twin_class_field_motion_description_i18n_id_fk
            references i18n
            on update cascade,
    created_at                   timestamp default CURRENT_TIMESTAMP,
    created_by_user_id           uuid
        constraint twin_class_field_motion_created_user_id_fk
            references "user"
            on update cascade,
    constraint twin_class_field_motion_uniq
        unique (twin_class_field_motion_schema_id, twin_class_id, twin_class_field_id)
);

create index if not exists twin_class_field_motion_twin_class_field_motion_schema_id_index
    on twin_class_field_motion (twin_class_field_motion_schema_id);
create index if not exists twin_class_field_motion_twin_class_id_index
    on twin_class_field_motion (twin_class_id);
create index if not exists twin_class_field_motion_twin_class_field_id_index
    on twin_class_field_motion (twin_class_field_id);
create index if not exists twin_class_field_motion_permission_id_index
    on twin_class_field_motion (permission_id);
create index if not exists twin_class_field_motion_name_i18n_id_index
    on twin_class_field_motion (name_i18n_id);
create index if not exists twin_class_field_motion_description_i18n_id_index
    on twin_class_field_motion (description_i18n_id);
create index if not exists twin_class_field_motion_created_by_user_id_index
    on twin_class_field_motion (created_by_user_id);


create table if not exists twin_class_field_motion_trigger
(
    id                             uuid              not null
        constraint twin_class_field_motion_trigger_pk
            primary key,
    twin_class_field_motion_id         uuid
        constraint twin_class_field_motion_trigger_twin_class_field_motion_id_fk
            references twin_class_field_motion
            on update cascade,
    "order"                        integer default 1 not null,
    motion_trigger_featurer_id integer           not null
        constraint twin_class_field_motion_trigger_featurer_id_fk
            references featurer,
    motion_trigger_params      hstore,
    active                         boolean default true
);

create index if not exists twin_class_field_motion_trigger_motion_trigger_featurer_id_ind
    on twin_class_field_motion_trigger (motion_trigger_featurer_id);

create unique index if not exists twin_class_field_motion_trigger_uindex
    on twin_class_field_motion_trigger (twin_class_field_motion_id, "order");


create table if not exists twin_class_field_motion_validator_rule
(
    id                     uuid not null
        constraint twin_class_field_motion_validator_pk
            primary key,
    twin_class_field_motion_id uuid not null
        constraint twin_class_field_motion_validator_twin_class_field_motion_id_fk
            references twin_class_field_motion
            on update cascade,
    "order"                integer default 1,
    active                 boolean default true,
    twin_validator_set_id  uuid
        constraint twin_class_field_motion_validator_twin_validator_set_id_fk
            references twin_validator_set
);

create unique index if not exists twin_class_field_motion_validator_rule_uindex
    on twin_class_field_motion_validator_rule (twin_class_field_motion_id, "order");

