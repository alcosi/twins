create table if not exists twin_factory_eraser_action
(
    id varchar(20) not null primary key
);

insert into twin_factory_eraser_action
values ('RESTRICT')
on conflict (id) do nothing;
insert into twin_factory_eraser_action
values ('ERASE_IRREVOCABLE')
on conflict (id) do nothing;
insert into twin_factory_eraser_action
values ('ERASE_CANDIDATE')
on conflict (id) do nothing;


create table if not exists twin_factory_eraser
(
    id                                  uuid        not null
        constraint twin_factory_eraser_pk
            primary key,
    twin_factory_id                     uuid        not null
        constraint twin_factory_eraser_twin_factory_id_fk
            references twin_factory
            on update cascade on delete cascade,
    input_twin_class_id                 uuid not null
        constraint twin_factory_eraser_input_twin_class_id_fk
            references twin_class
            on update cascade on delete cascade,
    twin_factory_condition_set_id       uuid
        constraint twin_factory_eraser_twin_factory_condition_set_id_fk
            references twin_factory_condition_set
            on update cascade,
    twin_factory_condition_invert       boolean default false,
    active                              boolean default true,
    twin_factory_eraser_action varchar(10) not null
        constraint twin_factory_eraser_twin_factory_eraser_action_id_fk
            references twin_factory_eraser_action
            on update cascade,
    description                         varchar
);

create index if not exists twin_factory_eraser_input_twin_class_id_index
    on twin_factory_eraser (input_twin_class_id);

create index if not exists twin_factory_eraser_twin_factory_condition_set_id_index
    on twin_factory_eraser (twin_factory_condition_set_id);

create index if not exists twin_factory_eraser_twin_factory_id_index
    on twin_factory_eraser (twin_factory_id);

create index if not exists twin_factory_eraser_twin_factory_eraser_action_id_index
    on twin_factory_eraser (twin_factory_eraser_action);

-- we use separate table to configure deletion factories,
-- because this will help to reuse same erase configs in different twinflows
create table if not exists eraseflow
(
    id                     uuid not null
        constraint eraseflow_pk
            primary key,
    twin_class_id          uuid not null -- this field will help UI to show select only with matched eraseflows
        constraint twinflow_twin_class_id_fk
            references twin_class
            on update cascade,
    target_deletion_factory_id  uuid
        constraint eraseflow_target_deletion_factory_id_fk
            references twin_factory
            on update cascade on delete restrict,
    cascade_deletion_by_head_factory_id  uuid
        constraint eraseflow_cascade_deletion_by_head_factory_id
            references twin_factory
            on update cascade on delete restrict,
    cascade_deletion_by_link_default_factory_id  uuid -- this factory will be applied for cascade deletion by link, if no other was specified in eraseflow_link_cascade table
        constraint eraseflow_cascade_deletion_by_link_default_factory_id
            references twin_factory
            on update cascade on delete restrict,
    name_i18n_id           uuid
        constraint eraseflow_name_i18n_id_fk
            references i18n
            on update cascade,
    description_i18n_id    uuid
        constraint eraseflow_description_i18n_id_fk
            references i18n
            on update cascade,
    created_by_user_id     uuid not null
        constraint eraseflow_user_id_fk
            references "user"
            on update cascade,
    created_at             timestamp default CURRENT_TIMESTAMP
);

create table if not exists eraseflow_link_cascade
(
    id                     uuid not null
        constraint eraseflow_link_cascade_pk
            primary key,
    eraseflow_id          uuid not null
        constraint eraseflow_link_cascade_eraseflow_id_fk
            references eraseflow
            on update cascade,
    link_id  uuid not null -- this will make sense only if link has correct strength
        constraint eraseflow_link_cascade_link_id_fk
            references link
            on update cascade on delete restrict,
    cascade_deletion_factory_id  uuid not null
        constraint eraseflow_link_cascade_cascade_deletion_factory_id
            references twin_factory
            on update cascade on delete restrict,
    created_by_user_id     uuid
        constraint eraseflow_link_cascade_user_id_fk
            references "user"
            on update cascade,
    created_at             timestamp default CURRENT_TIMESTAMP,
    description    varchar
);

create unique index if not exists eraseflow_link_cascade_eraseflow_id_link_id_uindex
    on eraseflow_link_cascade (eraseflow_id, link_id);

-- we add new field to twinflow table, but not twinflow_schema_map,
-- this helps to more easy access eraseflow from code
alter table twinflow
    add if not exists eraseflow_id uuid;

alter table twinflow
    drop constraint if exists twinflow_eraseflow_id_fk;

alter table twinflow
    add constraint twinflow_eraseflow_id_fk
        foreign key (eraseflow_id) references eraseflow
            on update cascade on delete restrict;






