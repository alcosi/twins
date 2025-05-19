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

INSERT INTO identity_provider (id, name, description, identity_provider_connector_featurer_id, identity_provider_connector_params, identity_provider_status_id, created_at)
VALUES ('00000000-0000-0000-0008-000000000001', 'Stub', 'TEST only identity provider, with "user_id,business_id" tokens.', 1901, null::hstore, 'ACTIVE', now()) on conflict do nothing;

DO $$
    BEGIN
        IF EXISTS(SELECT *
                  FROM information_schema.columns
                  WHERE table_name='domain' and column_name='token_handler_featurer_id')
        THEN
            alter table domain
                rename column token_handler_featurer_id to identity_provider_id;
            alter table domain
                drop constraint if exists domain_token_checker_featurer_id_fk;

            alter table domain
                alter column identity_provider_id type uuid using '00000000-0000-0000-0008-000000000001'::uuid;

            alter table domain
                drop column if exists token_handler_params;
            alter table public.domain
                add constraint domain_identity_provider_id_fk
                    foreign key (identity_provider_id) references public.identity_provider
                        on update cascade on delete restrict;
        END IF;
    END $$;

alter table domain
    alter column identity_provider_id set not null;

alter table domain_type
    drop column if exists default_token_handler_params;

alter table domain_type
    drop column if exists default_token_handler_featurer_id;

alter table domain_type
    add if not exists default_identity_provider_id uuid
        constraint domain_type_default_identity_provider_id_fk
            references identity_provider;

-- change this in future migrations, because this IDP is test only
update domain_type set default_identity_provider_id = '00000000-0000-0000-0008-000000000001' where default_identity_provider_id is null;

alter table domain_type
    alter column default_identity_provider_id set not null;



