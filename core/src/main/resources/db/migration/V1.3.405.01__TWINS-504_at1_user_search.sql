create table if not exists user_search
(
    id uuid not null
        constraint user_search_pk
            primary key,
    domain_id uuid
        constraint user_search_domain_id_fk
            references domain
                on update cascade on delete cascade,
    name varchar not null,
    user_sorter_featurer_id int4
        constraint user_search_featurer_id_fk
            references featurer
                on update cascade on delete cascade,
    user_sorter_params hstore,
    force_sorting boolean default false not null
);

insert into user_search (id, domain_id, name, force_sorting) values ('00000000-0000-0000-0014-000000000004', null, 'Unlimited user search', false) on conflict do nothing ;


create table if not exists user_search_predicate
(
    id uuid not null
        constraint user_search_predicate_pk
            primary key,
    user_search_id uuid not null
        constraint user_search_predicate_user_search_id_fk
            references user_search
                on update cascade on delete cascade,
    user_finder_featurer_id int4 not null
        constraint user_search_predicate_featurer_id_fk
            references featurer
                on update cascade on delete cascade,
    user_finder_params hstore not null default ''::hstore
);

create index if not exists user_search_predicate_index1
    on user_search_predicate (user_finder_featurer_id);

create index if not exists user_search_predicate_index2
    on user_search_predicate (user_search_id);

INSERT INTO featurer_type (id, name, description) VALUES (42, 'UserSorter', 'Order users search') on conflict on constraint featurer_type_pk do nothing ;
INSERT INTO featurer_type (id, name, description) VALUES (43, 'UserFinder', 'Find users') on conflict on constraint featurer_type_pk do nothing ;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (4201, 42, '', '', '', false) on conflict (id) do nothing ;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (4301, 43, '', '', '', false) on conflict (id) do nothing ;
