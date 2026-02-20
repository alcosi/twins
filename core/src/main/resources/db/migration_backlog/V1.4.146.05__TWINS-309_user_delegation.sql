create table if not exists user_delegation
(
    id                       uuid not null
        primary key,
    domain_id                uuid not null
        constraint user_delegation_domain_id_fk
            references domain
            on update cascade on delete cascade,
    machine_user_id          uuid not null
        constraint user_delegation_user_id_fk
            references "user"
            on update cascade on delete cascade,
    delegated_user_id uuid not null
        constraint user_delegation_user_group_id_fk
            references "user"
            on update cascade on delete cascade,
    added_at                 timestamp default CURRENT_TIMESTAMP,
    added_by_user_id         uuid
        constraint user_delegation_added_user_id_fk
            references "user"
            on update cascade on delete set null
);

drop
create unique index user_delegation_user_id_domain_id_uindex
    on user_delegation (machine_user_id, domain_id, delegated_user_id);

drop
create index idx_user_delegation_added_by_user_id
    on user_delegation (added_by_user_id);

DROP FUNCTION IF EXISTS get_users_by_groups(UUID, UUID, UUID);

-- TODO migration fot ONS to bridge and AI users
