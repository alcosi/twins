INSERT INTO featurer_type (id, name, description) VALUES (25, 'DomainInitiator', '') ON CONFLICT (id) DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description)
VALUES (2501, 25, 'org.twins.core.featurer.domain.initiator.DomainInitiatorBasic', 'DomainInitiatorBasic', '') ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description)
VALUES (2502, 25, 'org.twins.core.featurer.domain.initiator.DomainInitiatorB2B', 'DomainInitiatorB2B', '') ON CONFLICT (id) DO NOTHING;

create table if not exists domain_type
(
    id                  varchar not null
        primary key,
    name                varchar(255),
    description                varchar(255),
    domain_initiator_featurer_id integer not null
        constraint domain_initiator_featurer_id_fk
            references featurer
            on update cascade,
    domain_initiator_params      hstore,
    default_token_handler_featurer_id              integer
        constraint default_domain_token_handler_featurer_id_fk
            references featurer
            on update cascade,
    default_token_handler_params                   hstore,
    default_user_group_manager_featurer_id         integer
        constraint domain_user_group_manager_featurer_id_fk
            references featurer
            on update cascade,
    default_user_group_manager_params              hstore
);

create index if not exists domain_type_domain_initiator_featurer_id_index
    on domain_type (domain_initiator_featurer_id);

create index if not exists domain_type_default_token_handler_featurer_id_index
    on domain_type (default_token_handler_featurer_id);

create index if not exists domain_type_default_user_group_manager_featurer_id_index
    on domain_type (default_user_group_manager_featurer_id);

insert into domain_type (
                         id,
                         name,
                         description,
                         domain_initiator_featurer_id,
                         domain_initiator_params,
                         default_token_handler_featurer_id,
                         default_token_handler_params,
                         default_user_group_manager_featurer_id,
                         default_user_group_manager_params)
values ('basic', 'Basic', 'Single level domain. With no business account', 2501, null, 1901, null, 2101, null) on conflict do nothing ;

insert into domain_type (
    id,
    name,
    description,
    domain_initiator_featurer_id,
    domain_initiator_params,
    default_token_handler_featurer_id,
    default_token_handler_params,
    default_user_group_manager_featurer_id,
    default_user_group_manager_params)
values ('b2b', 'B2B', 'Double level domain. With business accounts support', 2502, null, 1901, null, 2101, null) on conflict do nothing;

alter table domain
    add if not exists domain_type_id varchar;

update domain set domain_type_id = 'basic' where domain.domain_type_id is null and business_account_initiator_featurer_id is null;
update domain set domain_type_id = 'b2b' where domain.domain_type_id is null and business_account_initiator_featurer_id is not null;

alter table domain
    alter column domain_type_id set not null;

alter table domain
    drop constraint if exists domain_domain_type_id_fk;

alter table domain
    add constraint domain_domain_type_id_fk
        foreign key (domain_type_id) references domain_type
            on update cascade;

-- we have fk loop, so this field can be nullable just for creation
alter table domain
    alter column permission_schema_id drop not null;

-- we have fk loop, so this field can be nullable just for creation
alter table domain
    alter column twinflow_schema_id drop not null;

-- we have fk loop, so this field can be nullable just for creation
alter table domain
    alter column twin_class_schema_id drop not null;

alter table domain
    add if not exists alias_counter int default 0;






