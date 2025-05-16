create table if not exists identity_provider_status
(
    id          varchar not null
        primary key,
    description text
);

INSERT INTO identity_provider_status (id, description)
VALUES ('ACTIVE', 'Active status'),
       ('DISABLED', 'Disabled status')
on conflict (id) do nothing;

create table if not exists identity_provider
(
    id                        uuid                                          not null
        constraint identity_provider_pk
            primary key,
    name                      varchar(50),
    description               varchar(255),
    identity_provider_connector_featurer_id integer
        constraint identity_provider_connector_featurer_id_fk
            references featurer
            on update cascade,
    identity_provider_connector_params      hstore,
    identity_provider_status_id             varchar   default 'ACTIVE'::character varying not null
        constraint identity_provider_identity_provider_status_fk
            references identity_provider_status,
    created_at                timestamp default CURRENT_TIMESTAMP
);

insert into featurer(id, featurer_type_id, class, name, description)
values (3301, 20, '', '', '')
on conflict (id) do nothing;

INSERT INTO identity_provider (id, name, description, identity_provider_connector_featurer_id, identity_provider_connector_params, identity_provider_status_id, created_at)
VALUES ('00000000-0000-0000-0008-000000000001', 'Stub', 'TEST only identity provider, with "user_id,business_id" tokens.', 3301, null::hstore, 'ACTIVE', now()) on conflict do nothing;

alter table domain
    add if not exists identity_provider_id uuid
        constraint domain_identity_provider_id_fk
            references identity_provider;

update domain set identity_provider_id = '00000000-0000-0000-0008-000000000001' where domain.identity_provider_id is null;

alter table domain
    alter column identity_provider_id set not null;

