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
