create table if not exists domain_version
(
    id          uuid                                not null
        constraint domain_version_pk
            primary key,
    domain_id   uuid                                not null
        constraint domain_version_domain_id_fk
            references domain
            on update cascade on delete cascade,
    name        varchar,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    released_at timestamp
);

create index if not exists domain_version_domain_id_index
    on domain_version (domain_id);


alter table domain
    add if not exists current_domain_version_id uuid
        constraint domain_domain_version_id_fk
            references domain_version
            on update cascade on delete restrict;

create table if not exists domain_version_changes
(
    id                uuid
        constraint domain_version_changes_pk
            primary key,
    domain_version_id uuid     not null
        constraint domain_version_changes_domain_version_id_fk
            references domain_version
            on update cascade on delete cascade,
    time_in_ms        integer  not null, -- this will help to run queries in the correct order
    table_name        varchar  not null,
    operation         smallint not null, -- 1 - create, 2 - update, 0 - delete
    row_id            varchar  not null
);

create index if not exists domain_version_changes_domain_version_id_index
    on domain_version_changes (domain_version_id);

create index if not exists domain_version_changes_table_name_index
    on domain_version_changes (table_name);


create table if not exists domain_version_ghost
(
    domain_id  uuid        not null
        constraint domain_version_ghost_domain_id_fk
            references public.domain
            on update cascade on delete cascade,
    user_id    uuid        not null
        constraint domain_version_ghost_user_id_fk
            references public."user"
            on update cascade on delete cascade,
    table_name varchar(50) not null,
    constraint domain_version_ghost_pk
        primary key (domain_id, user_id, table_name)
);







