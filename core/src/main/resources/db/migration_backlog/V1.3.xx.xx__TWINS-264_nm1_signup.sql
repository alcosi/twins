-- this can be a problem because of duplicates
create unique index if not exists user_email_uindex
    on public."user" (email);

create table if not exists user_email_verification
(
    id                  uuid not null
        constraint user_email_verification_pk
            primary key,
    user_id             uuid
        constraint user_email_verification_user_id_fk
            references "user"
            on update cascade on delete cascade,
    identity_provider_id uuid
        constraint user_email_verification_identity_provider_id_fk
            references identity_provider
            on update cascade on delete cascade,
    email varchar not null,
    verification_code_twins varchar not null unique,
    verification_code_idp varchar,
    created_at          timestamp default CURRENT_TIMESTAMP
);

INSERT INTO user_status (id) VALUES ('EMAIL_VERIFICATION_REQUIRED') on conflict do nothing;

alter table domain
    add if not exists domain_user_initiator_featurer_id integer
        constraint domain_domain_user_initiator_featurer_id_fk
            references featurer
            on update cascade;

alter table domain
    add if not exists domain_user_initiator_params hstore;

INSERT INTO featurer_type (id, name)VALUES (34, 'DomainUserInitiator') ON CONFLICT (id) DO NOTHING;

insert into featurer(id, featurer_type_id, class, name, description)
values (3401, 34, '', '', '')
on conflict (id) do nothing;

insert into featurer(id, featurer_type_id, class, name, description)
values (3402, 34, '', '', '')
on conflict (id) do nothing;
update domain set domain_user_initiator_featurer_id = 3401 where domain_type_id = 'basic' and domain.domain_user_initiator_featurer_id is null;
update domain set domain_user_initiator_featurer_id = 3402 where domain_type_id = 'basic' and domain.domain_user_initiator_featurer_id is null;




