create table if not exists twin_factory_eraser_action
(
    id varchar(10) not null primary key
);

insert into twin_factory_eraser_action
values ('NEXT')
on conflict (id) do nothing;
insert into twin_factory_eraser_action
values ('RESTRICT')
on conflict (id) do nothing;
insert into twin_factory_eraser_action
values ('ERASE')
on conflict (id) do nothing;
insert into twin_factory_eraser_action
values ('SKIP')
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
    input_twin_class_id                 uuid
        constraint twin_factory_eraser_input_twin_class_id_fk
            references twin_class
            on update cascade on delete cascade,
    twin_factory_condition_set_id       uuid
        constraint twin_factory_eraser_twin_factory_condition_set_id_fk
            references twin_factory_condition_set
            on update cascade,
    twin_factory_condition_invert       boolean default false,
    active                              boolean default true,
    final_twin_factory_eraser_action_id varchar(10) not null
        constraint twin_factory_eraser_final_twin_factory_eraser_action_id_fk
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

create table if not exists twin_factory_eraser_step
(
    id                                      uuid              not null
        constraint twin_factory_eraser_step_pk
            primary key,
    twin_factory_eraser_id                  uuid              not null
        constraint twin_factory_eraser_step_twin_factory_eraser_id_fk
            references twin_factory_eraser
            on update cascade on delete cascade,
    "order"                                 integer default 0 not null,
    twin_factory_condition_set_id           uuid
        constraint twin_factory_eraser_step_twin_factory_condition_set_id_fk
            references twin_factory_condition_set
            on update cascade,
    twin_factory_condition_invert           boolean default false,
    active                                  boolean default true,
    description                             varchar,
    on_passed_twin_factory_eraser_action_id varchar(10)       not null
        constraint twin_factory_eraser_on_passed_twin_factory_eraser_action_id_fk
            references twin_factory_eraser_action
            on update cascade,
    on_failed_twin_factory_eraser_action_id varchar(10)       not null
        constraint twin_factory_eraser_on_failed_twin_factory_eraser_action_id_fk
            references twin_factory_eraser_action
            on update cascade
);


create index if not exists twin_factory_eraser_step_twin_factory_condition_set_id_index
    on twin_factory_eraser_step (twin_factory_condition_set_id);

create unique index if not exists twin_factory_eraser_step_twin_factory_eraser_id_order_uinde
    on twin_factory_eraser_step (twin_factory_eraser_id, "order");

alter table twinflow
    add if not exists erase_twin_factory_id uuid;
alter table twinflow
    add if not exists erase_twin_status_id uuid;

alter table twinflow
    drop constraint if exists twinflow_erase_twin_factory_id_fk;
alter table twinflow
    drop constraint if exists twinflow_erase_twin_status_id_fk;

alter table twinflow
    add constraint twinflow_erase_twin_factory_id_fk
        foreign key (erase_twin_factory_id) references twin_factory
            on update cascade on delete restrict;
alter table twinflow
    add constraint twinflow_erase_twin_status_id_fk
        foreign key (erase_twin_status_id) references twin_status
            on update cascade on delete restrict;
-- creating 3 basic deletion logic
INSERT INTO twin_factory (id, key, domain_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0006-000000000001', 'eraseSimple', null, null, null)
on conflict (id) do nothing;
INSERT INTO twin_factory (id, key, domain_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0006-000000000002', 'eraseIfNoChildren', null, null, null)
on conflict (id) do nothing;
INSERT INTO twin_factory (id, key, domain_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0006-000000000003', 'eraseRestrict', null, null, null)
on conflict (id) do nothing;

INSERT INTO twin_factory_eraser (id, twin_factory_id, input_twin_class_id, twin_factory_condition_set_id,
                                 twin_factory_condition_invert, active, final_twin_factory_eraser_action_id,
                                 description)
VALUES ('ac877359-5b54-4062-84df-13f95ca1674b', '00000000-0000-0000-0006-000000000001', null, null, DEFAULT, DEFAULT,
        'ERASE',
        'simple deletion logic - this eraser will delete current twin, all children (by db FK cascading), all links  (by db FK cascading)')
on conflict (id) do nothing;
INSERT INTO twin_factory_eraser (id, twin_factory_id, input_twin_class_id, twin_factory_condition_set_id,
                                 twin_factory_condition_invert, active, final_twin_factory_eraser_action_id,
                                 description)
VALUES ('aa5c49fa-a4d7-4990-a039-22c7aa142183', '00000000-0000-0000-0006-000000000002', null, null, DEFAULT, DEFAULT,
        'ERASE', 'this eraser will delete current twin only if there is no children for it')
on conflict (id) do nothing;
INSERT INTO twin_factory_eraser (id, twin_factory_id, input_twin_class_id, twin_factory_condition_set_id,
                                 twin_factory_condition_invert, active, final_twin_factory_eraser_action_id,
                                 description)
VALUES ('8948a756-4c4c-4414-9bb5-7094d571186a', '00000000-0000-0000-0006-000000000003', null, null, DEFAULT, DEFAULT,
        'RESTRICT', 'this eraser will restrict deletion')
on conflict (id) do nothing;





