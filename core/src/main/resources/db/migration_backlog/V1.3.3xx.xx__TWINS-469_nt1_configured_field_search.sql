create table if not exists twin_class_field_search
(
    id                  uuid      not null primary key,
    domain_id           uuid references domain (id) on delete cascade on update cascade,
    name         varchar   not null
     );

create table if not exists twin_class_field_search_predicate
(
    id                  uuid      not null primary key,
    twin_class_field_search_id           uuid references twin_class_field_search (id) on delete cascade on update cascade,
    field_finder_featurer_id int4    not null references featurer (id) on delete restrict on update cascade,
    field_finder_params      hstore    not null             default ''::hstore
    );

create index if not exists twin_class_field_search_predicate_index1
    on twin_class_field_search_predicate (field_finder_featurer_id);

create index if not exists twin_class_field_search_predicate_index2
    on twin_class_field_search_predicate (twin_class_field_search_id);
