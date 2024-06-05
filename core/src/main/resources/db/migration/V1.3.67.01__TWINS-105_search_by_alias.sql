drop table if exists search_by_link;
drop table if exists search_by_twin;
drop table if exists search_by_twin_class;
drop table if exists search_by_twin_status;
drop table if exists search_by_user;
drop table if exists search_param;


create table if not exists search_predicate
(
    id                                  uuid    not null
        constraint search_predicate_pk
            primary key,
    search_id                           uuid
        constraint search_predicate_search_id_fk
            references search
            on update cascade on delete cascade,
    search_field_id                     varchar
        constraint search_predicate_search_field_id_fk
            references search_field
            on update cascade on delete cascade,
    search_criteria_builder_featurer_id integer not null
        constraint search_predicate_featurer_id_fk
            references featurer
            on update cascade on delete cascade,
    search_criteria_builder_params      hstore,
    "exclude"                           boolean default false
);

create index if not exists search_predicate_search_id_index
    on search_predicate (search_id);

create index if not exists search_predicate_search_field_id_index
    on search_predicate (search_field_id);

UPDATE search_field
SET id = 'linkId'
WHERE id LIKE 'linkDstTwinId' ESCAPE '#';
INSERT INTO search_field (id)
VALUES ('twinClassId')
on conflict do nothing;
INSERT INTO search_field (id)
VALUES ('twinNameLike')
on conflict do nothing;
INSERT INTO search_field (id)
VALUES ('twinId')
on conflict do nothing;
INSERT INTO search_field (id)
VALUES ('tagDataListOptionId')
on conflict do nothing;
INSERT INTO search_field (id)
VALUES ('markerDataListOptionId')
on conflict do nothing;
INSERT INTO search_field (id)
VALUES ('hierarchyTreeContainsId')
on conflict do nothing;

alter table search
    add if not exists head_twin_search_id uuid;
alter table search
    drop constraint if exists head_search_search_id_fk;

alter table search
    add if not exists head_twin_search_id uuid;
alter table search
    drop constraint if exists search_head_search_id_fk;

alter table search
    add constraint search_head_search_id_fk
        foreign key (head_twin_search_id) references search;
