create table if not exists twin_action
(
    id varchar(255) not null
        constraint twin_action_pk
            primary key
);

insert into twin_action (id)
values ('EDIT')
on conflict do nothing;
insert into twin_action (id)
values ('DELETE')
on conflict do nothing;
insert into twin_action (id)
values ('COMMENT')
on conflict do nothing;
insert into twin_action (id)
values ('MOVE')
on conflict do nothing;
insert into twin_action (id)
values ('WATCH')
on conflict do nothing;
insert into twin_action (id)
values ('TIME_TRACK')
on conflict do nothing;
insert into twin_action (id)
values ('ATTACHMENT_ADD')
on conflict do nothing;
insert into twin_action (id)
values ('ATTACHMENT_DELETE')
on conflict do nothing;

create table if not exists twin_class_action_permission
(
    id             uuid         not null
        constraint twin_class_action_permission_pk
            primary key,
    twin_class_id  uuid         not null
        constraint twin_class_action_permission_twin_class_id_fk
            references twin_class
            on update cascade,
    twin_action_id varchar(255) not null
        constraint twin_class_action_permission_twin_action_id_fk
            references twin_action
            on update cascade,
    permission_id  uuid         not null
        constraint twin_class_action_permission_permission_id_fk
            references permission
            on update cascade
);

create index if not exists twin_class_action_permission_twin_action_id_index
    on twin_action_permission (twin_action_id);

create index if not exists twin_class_action_permission_twin_class_id_index
    on twin_action_permission (twin_class_id);

create table if not exists twin_class_action_validator
(
    id                         uuid         not null
        constraint twin_class_action_validator_pk
            primary key,
    twin_class_id              uuid         not null
        constraint twin_class_action_validator_twin_class_id_fk
            references twin_class
            on update cascade,
    twin_action_id             varchar(255) not null
        constraint twin_class_action_validator_twin_action_id_fk
            references twin_action
            on update cascade,
    "order"                    integer               default 1,
    twin_validator_featurer_id integer      not null
        constraint twin_class_action_validator_featurer_id_fk_2
            references featurer,
    twin_validator_params      hstore,
    invert                     boolean      not null default false,
    active                     boolean      not null default true
);

create index if not exists twin_class_action_validator_twin_validator_featurer_id_
    on twin_action_validator (twin_validator_featurer_id);

create index if not exists twin_class_action_validator_twin_action_id_index
    on twin_action_validator (twin_action_id);

create index if not exists twin_class_action_validator_twin_class_id_index
    on twin_action_validator (twin_class_id);

create unique index if not exists twin_class_action_validator_order_uniq
    on twin_action_validator (twin_class_id, twin_action_id, "order");

do
$$
    begin
        if exists (select 1
                   from information_schema.columns
                   where table_schema = 'public'
                     and table_name = 'twinflow_transition_validator'
                     and column_name = 'transition_validator_featurer_id') then
            alter table twinflow_transition_validator
                rename column transition_validator_featurer_id to twin_validator_featurer_id;
        end if;
    end
$$;

do
$$
    begin
        if exists (select 1
                   from information_schema.columns
                   where table_schema = 'public'
                     and table_name = 'twinflow_transition_validator'
                     and column_name = 'transition_validator_params') then
            alter table twinflow_transition_validator
                rename column transition_validator_params to twin_validator_params;
        end if;
    end
$$;

INSERT INTO featurer (id, featurer_type_id, class, name, description)
VALUES (1606, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorTwinHasLink', 'TwinValidatorTwinHasLink', '')
on conflict do nothing;




