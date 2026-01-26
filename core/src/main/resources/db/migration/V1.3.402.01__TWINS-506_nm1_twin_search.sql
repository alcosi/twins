create table if not exists twin_search_alias
(
    id                          uuid                 not null
        primary key,
    domain_id                   uuid                 not null
        constraint twin_search_alias_domain_id_fk
            references domain
            on update cascade on delete cascade,
    alias                       varchar              not null,
    twin_search_detector_featurer_id integer default 2801 not null
        constraint twin_search_alias_detector_featurer_id_fk
            references featurer
            on update cascade,
    twin_search_detector_params      hstore
);

create unique index if not exists twin_search_alias_domain_id_alias_uindex
    on twin_search_alias (domain_id, alias);

create table if not exists twin_search
(
    id                  uuid not null
        constraint twin_search_pk
            primary key,
    name                varchar,
    twin_sorter_featurer_id integer default 4101  not null
        constraint twin_search_sorter_featurer_id_fk
            references featurer,
    twin_sorter_params      hstore,
    force_sorting            boolean default false not null,
    twin_search_alias_id     uuid
        constraint twin_search_search_alias_id_fk
            references search_alias
            on update cascade on delete cascade,
    permission_id       uuid
        constraint twin_search_permission_id_fk
            references permission
            on update cascade,
    description         varchar,
    created_at          timestamp default CURRENT_TIMESTAMP,
    head_twin_search_id uuid
        constraint twin_search_head_search_id_fk
            references twin_search
);


create table if not exists twin_search_predicate
(
    id                                  uuid    not null
        constraint twin_search_predicate_pk
            primary key,
    twin_search_id                           uuid
        constraint twin_search_predicate_twin_search_id_fk
            references twin_search
            on update cascade on delete cascade,
    twin_finder_featurer_id integer not null
        constraint twin_search_predicate_twin_finder_featurer_id_fk
            references featurer
            on update cascade on delete cascade,
    twin_finder_params      hstore,
    description                         varchar(255)
);

create index if not exists twin_search_predicate_search_id_index
    on twin_search_predicate (twin_search_id);

INSERT INTO featurer_type (id, name, description)
VALUES (41, 'Twin Search Sorter', 'Order twin search') on conflict on constraint featurer_type_pk do nothing ;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (4101, 41, 'org.twins.core.featurer.twin.sorter.TwinSorterStub', 'Unsorted', '', false) on conflict do nothing ;

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'search_alias'
        ) THEN
            insert into twin_search_alias (id, domain_id, alias, twin_search_detector_featurer_id, twin_search_detector_params)
            select id, domain_id, alias, search_detector_featurer_id, search_detector_params
            from search_alias on conflict do nothing;
        END IF;
    END
$$;

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'search'
        ) THEN
            insert into twin_search (id, name, twin_sorter_featurer_id, twin_sorter_params, force_sorting, twin_search_alias_id, permission_id, description, created_at, head_twin_search_id)
            select id, name, 4101, null, false, search_alias_id, permission_id, description, created_at, head_twin_search_id
            from search on conflict do nothing;
        END IF;
    END
$$;

insert into featurer(id, featurer_type_id, class, name, description) values (2701, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2702, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2703, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2704, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2705, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2706, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2707, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2708, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2709, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2710, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2711, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2712, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2713, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2714, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2715, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2716, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2717, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2718, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2719, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2720, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2721, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2722, 27, '', '', '') on conflict (id) do nothing;
insert into featurer(id, featurer_type_id, class, name, description) values (2723, 27, '', '', '') on conflict (id) do nothing;


DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'search_predicate'
        ) THEN
            update search_predicate set search_criteria_builder_featurer_id = 2710 where search_criteria_builder_featurer_id = 2706;
            update search_predicate set search_criteria_builder_featurer_id = 2721, search_criteria_builder_params = delete(search_criteria_builder_params, 'entityId') || hstore('statusIds', search_criteria_builder_params->'entityId') where search_criteria_builder_featurer_id = 2701 and search_field_id = 'statusId';
            update search_predicate set search_criteria_builder_featurer_id = 2704, search_criteria_builder_params = delete(search_criteria_builder_params, 'entityId') || hstore('classIds', search_criteria_builder_params->'entityId') where search_criteria_builder_featurer_id = 2701 and search_field_id = 'twinClassId';
            update search_predicate set search_criteria_builder_featurer_id = 2714, search_criteria_builder_params = delete(search_criteria_builder_params, 'entityId') || hstore('twinIds', search_criteria_builder_params->'entityId') where search_criteria_builder_featurer_id = 2701 and search_field_id = 'twinId';
            update search_predicate set search_criteria_builder_featurer_id = 2702, search_criteria_builder_params = delete(search_criteria_builder_params, 'entityId') || hstore('userIds', search_criteria_builder_params->'entityId') where search_criteria_builder_featurer_id = 2701 and search_field_id = 'assigneeUserId';
            update search_predicate set search_criteria_builder_featurer_id = 2707, search_criteria_builder_params = delete(search_criteria_builder_params, 'entityId') || hstore('userIds', search_criteria_builder_params->'entityId') where search_criteria_builder_featurer_id = 2701 and search_field_id = 'createdByUserId';
            update search_predicate set search_criteria_builder_featurer_id = 2709, search_criteria_builder_params = delete(search_criteria_builder_params, 'entityId') || hstore('twinIds', search_criteria_builder_params->'entityId') where search_criteria_builder_featurer_id = 2701 and search_field_id = 'headTwinId';
            update search_predicate set search_criteria_builder_featurer_id = 2720, search_criteria_builder_params = delete(search_criteria_builder_params, 'entityId') || hstore('markerDataListOptionIds', search_criteria_builder_params->'entityId') where search_criteria_builder_featurer_id = 2701 and search_field_id = 'markerDataListOptionId';
            update search_predicate set search_criteria_builder_featurer_id = 2723, search_criteria_builder_params = delete(search_criteria_builder_params, 'entityId') || hstore('tagDataListOptionIds', search_criteria_builder_params->'entityId') where search_criteria_builder_featurer_id = 2701 and search_field_id = 'tagDataListOptionId';
            update search_predicate set search_criteria_builder_featurer_id = 2711, search_criteria_builder_params = delete(search_criteria_builder_params, 'entityId') || hstore('twinIds', search_criteria_builder_params->'entityId') where search_criteria_builder_featurer_id = 2701 and search_field_id = 'hierarchyTreeContainsId';
            update search_predicate set search_criteria_builder_featurer_id = 2701 where search_criteria_builder_featurer_id = 2702 and search_field_id = 'assigneeUserId';
            update search_predicate set search_criteria_builder_featurer_id = 2706 where search_criteria_builder_featurer_id = 2702 and search_field_id = 'createdByUserId';
            update search_predicate set search_criteria_builder_featurer_id = 2717 where search_criteria_builder_featurer_id = 2705 and search_field_id = 'linkId';
            insert into twin_search_predicate (id, twin_search_id, twin_finder_featurer_id, twin_finder_params, description)
            select id, search_id, search_criteria_builder_featurer_id, search_criteria_builder_params, description
            from search_predicate on conflict do nothing;
        END IF;
    END
$$;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'face_wt001' AND column_name = 'twin_search_id'
        ) THEN
            ALTER TABLE face_wt001
                RENAME COLUMN search_id TO twin_search_id;
        END IF;
    END $$;

alter table face_wt001
    drop constraint if exists face_wt001_search_id_fk;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.table_constraints
            WHERE constraint_name = 'face_wt001_twin_search_id_fk'
              AND table_name = 'face_wt001'
        ) THEN
            ALTER TABLE face_wt001
                ADD CONSTRAINT face_wt001_twin_search_id_fk
                    FOREIGN KEY (twin_search_id) REFERENCES twin_search;
        END IF;
    END $$;

alter table twin_search
    drop constraint if exists twin_search_search_alias_id_fk;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.table_constraints
            WHERE constraint_name = 'twin_search_twin_search_alias_id_fk'
              AND table_name = 'twin_search'
        ) THEN
            ALTER TABLE twin_search
                ADD CONSTRAINT twin_search_twin_search_alias_id_fk
                    FOREIGN KEY (twin_search_alias_id) REFERENCES twin_search_alias;
        END IF;
    END $$;

drop table if exists search_predicate;
drop table if exists search;
drop table if exists search_alias;