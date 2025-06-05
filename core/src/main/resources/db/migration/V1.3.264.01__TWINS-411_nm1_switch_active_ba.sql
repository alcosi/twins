alter table domain_user
    add if not exists last_active_business_account_id uuid
        constraint domain_user_last_active_business_account_id_fk
            references business_account
            on update cascade on delete restrict;


alter table identity_provider_internal_token
    alter column access_expires_at set not null;

alter table identity_provider_internal_token
    alter column refresh_token drop not null;

alter table identity_provider_internal_token
    alter column refresh_expires_at drop not null;

update identity_provider_internal_token set created_at = now() where created_at is null;
alter table identity_provider_internal_token
    alter column created_at set not null;