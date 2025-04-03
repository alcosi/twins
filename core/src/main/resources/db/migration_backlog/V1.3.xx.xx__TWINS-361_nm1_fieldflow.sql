create table if not exists fieldflow
(
    id                     uuid not null
        constraint fieldflow_pk
            primary key,
    twin_class_id          uuid not null
        constraint fieldflow_twin_class_id_fk
            references twin_class
            on update cascade on delete cascade,
    twin_class_field_id           uuid not null
        constraint fieldflow_twin_class_field_id_fk
            references twin_class_field
            on update cascade on delete cascade,
    name_i18n_id           uuid
        constraint fieldflow_name_i18n_id_fk
            references i18n
            on update cascade,
    description_i18n_id    uuid
        constraint fieldflow_description_i18n_id_fk
            references i18n
            on update cascade,
    created_by_user_id     uuid
        constraint fieldflow_user_id_fk
            references "user"
            on update cascade,
    created_at             timestamp default CURRENT_TIMESTAMP
);


create table if not exists fieldflow_motion
(
    id                           uuid not null
        constraint fieldflow_motion_pk
            primary key,
    fieldflow_id                  uuid not null
        constraint fieldflow_motion_fieldflow_id_fk
            references fieldflow
            on update cascade,
    name_i18n_id                 uuid not null
        constraint fieldflow_motion_name_i18n_id_fk
            references i18n
            on update cascade,
    twin_class_id           uuid
        constraint fieldflow_motion_twin_class_id_fk
            references twin_class
            on update cascade,
    twin_class_field_id           uuid not null
        constraint fieldflow_motion_twin_class_field_id_fk
            references twin_class_field
            on update cascade,
    permission_id                uuid
        constraint fieldflow_motion_permission_id_fk
            references permission
            on update cascade,
    description_i18n_id          uuid
        constraint fieldflow_motion_description_i18n_id_fk
            references i18n
            on update cascade,
    created_at                   timestamp default CURRENT_TIMESTAMP,
    created_by_user_id           uuid
        constraint fieldflow_motion_created_user_id_fk
            references "user"
            on update cascade,
    constraint fieldflow_motion_uniq
        unique (fieldflow_id, twin_class_id, twin_class_field_id)
);

create index if not exists fieldflow_motion_dst_twin_status_id_index
    on fieldflow_motion (dst_twin_status_id);

create index if not exists fieldflow_motion_inbuilt_twin_factory_id_index
    on fieldflow_motion (inbuilt_twin_factory_id);

create index if not exists fieldflow_motion_permission_id_index
    on fieldflow_motion (permission_id);

create index if not exists fieldflow_motion_fieldflow_id_index
    on fieldflow_motion (fieldflow_id);


create table if not exists fieldflow_motion_trigger
(
    id                             uuid              not null
        constraint fieldflow_motion_trigger_pk
            primary key,
    fieldflow_motion_id         uuid
        constraint fieldflow_motion_trigger_fieldflow_motion_id_fk
            references fieldflow_motion
            on update cascade,
    "order"                        integer default 1 not null,
    motion_trigger_featurer_id integer           not null
        constraint fieldflow_motion_trigger_featurer_id_fk
            references featurer,
    motion_trigger_params      hstore,
    active                         boolean default true
);

create index if not exists fieldflow_motion_trigger_motion_trigger_featurer_id_ind
    on fieldflow_motion_trigger (motion_trigger_featurer_id);

create unique index if not exists fieldflow_motion_trigger_fieldflow_motion_id_order
    on fieldflow_motion_trigger (fieldflow_motion_id, "order");


create table if not exists fieldflow_motion_validator_rule
(
    id                     uuid not null
        constraint fieldflow_motion_validator_pk
            primary key,
    fieldflow_motion_id uuid not null
        constraint fieldflow_motion_validator_fieldflow_motion_id_fk
            references fieldflow_motion
            on update cascade,
    "order"                integer default 1,
    active                 boolean default true,
    twin_validator_set_id  uuid
        constraint fieldflow_motion_validator_twin_validator_set_id_fk
            references twin_validator_set
);

create unique index if not exists fieldflow_motion_validator_fieldflow_motion_id_order_uind
    on fieldflow_motion_validator_rule (fieldflow_motion_id, "order");

