alter table identity_provider_internal_user
    add if not exists last_active_business_account_id uuid
        constraint identity_provider_internal_user_business_account_id_fk
            references public.business_account
            on update cascade on delete restrict;