create table if not exists twin_factory_eraser_action
(
    id varchar(10) not null primary key
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


alter table twinflow
    add if not exists target_deletion_factory_id uuid;
alter table twinflow
    add if not exists cascade_deletion_factory_id uuid;

alter table twinflow
    drop constraint if exists twinflow_target_deletion_factory_id_fk;
alter table twinflow
    drop constraint if exists twinflow_cascade_deletion_factory_id_fk;

alter table twinflow
    add constraint twinflow_target_deletion_factory_id_fk
        foreign key (target_deletion_factory_id) references twin_factory
            on update cascade on delete restrict;
alter table twinflow
    add constraint twinflow_cascade_deletion_factory_id_fk
        foreign key (cascade_deletion_factory_id) references twin_factory
            on update cascade on delete restrict;




