create table if not exists user_group_act_as_user_involve
(
    id               uuid not null
        primary key,
    domain_id        uuid not null
        constraint user_group_act_as_user_involve_domain_id_fk
            references domain
            on update cascade on delete cascade,
    machine_user_id          uuid not null
        constraint user_group_act_as_user_involve_user_id_fk
            references "user"
            on update cascade on delete cascade,
    involve_in_user_group_id    uuid not null
        constraint user_group_act_as_user_involve_user_group_id_fk
            references user_group
            on update cascade on delete cascade,
    added_at         timestamp default CURRENT_TIMESTAMP,
    added_by_user_id uuid
        constraint user_group_act_as_user_involve_added_user_id_fk
            references "user"
            on update cascade
);

create unique index if not exists user_group_act_as_user_involve_user_id_domain_id_uindex
    on user_group_act_as_user_involve (machine_user_id, domain_id, involve_in_user_group_id);
