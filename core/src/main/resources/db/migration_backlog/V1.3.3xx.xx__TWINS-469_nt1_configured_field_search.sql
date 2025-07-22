create table if not exists public.twin_class_field_search
(
    id                  uuid      not null primary key,
    domain_id           uuid references public.domain (id) on delete cascade on update cascade,
    name         varchar   not null
     );

create table if not exists public.twin_class_field_search_predicate
(
    id                  uuid      not null primary key,
    twin_class_field_search_id           uuid references public.twin_class_field_search (id) on delete cascade on update cascade,
    field_finder_featurer_id int4    not null references public.featurer (id) on delete cascade on update cascade,
    field_finder_params      hstore    not null             default ''::hstore
    );
