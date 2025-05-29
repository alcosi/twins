CREATE TABLE if not exists identity_provider_internal_user
(
    user_id       uuid PRIMARY KEY
        constraint identity_provider_internal_user_id_fk
            references "user"
            on update cascade on delete cascade,
    password_hash TEXT         NOT NULL,
    created_at    TIMESTAMP DEFAULT now(),
    is_active     BOOLEAN   DEFAULT TRUE,
    last_login_at TIMESTAMP
);

create index if not exists identity_provider_internal_user_user_id_index
    on identity_provider_internal_user (user_id);


create table if not exists identity_provider_internal_token
(
    id                                 uuid      not null
        constraint identity_provider_internal_token_pk
            primary key,
    user_id       uuid
        constraint identity_provider_internal_token_user_id_fk
            references "user"
            on update cascade on delete cascade,
    domain_id                      uuid                not null
        constraint identity_provider_internal_token_domain_id_fk
            references domain
            on update cascade on delete cascade,
    active_business_account_id uuid
        constraint identity_provider_internal_token_active_business_account_id_fk
            references business_account
            on update cascade on delete cascade,
    access_token                       TEXT not null unique,
    access_expires_at                  TIMESTAMP,
    refresh_token                      TEXT      NOT NULL unique,
    refresh_expires_at                 TIMESTAMP NOT NULL,
    finger_print                       TEXT,
    created_at                         TIMESTAMP DEFAULT now(),
    revoked                            BOOLEAN   DEFAULT FALSE,
    revoked_at                         TIMESTAMP
);

create index if not exists  identity_provider_internal_token_user_id_index
    on identity_provider_internal_token (user_id);

INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1901, 19, '', '', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1902, 19, '', '', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1903, 19, '', '', '', false) on conflict (id) do nothing;


INSERT INTO identity_provider (id, name, description, identity_provider_connector_featurer_id, identity_provider_connector_params, identity_provider_status_id, created_at)
VALUES ('00000000-0000-0000-0008-000000000002', 'Internal', 'TEST only identity provider. Please use external IDP on production', 1902, 'refreshTokenLifetimeInSeconds => 86400, authTokenLifetimeInSeconds => 300', 'ACTIVE', now()) on conflict do nothing;
