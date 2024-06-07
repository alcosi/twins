INSERT INTO featurer_type (id, name, description) VALUES (28, 'SearchDetector', '') on conflict (id) do nothing;
INSERT INTO featurer VALUES (2801, 28, 'org.twins.core.featurer.search.detector.SearchDetectorPreferProtected', 'SearchDetectorPreferProtected') on conflict (id) do nothing;

alter table search_alias
    add if not exists alias varchar;

update search_alias set alias = id, id = gen_random_uuid() where search_alias.alias is null;

alter table search_alias
    alter column alias set not null;

alter table search_alias
    add if not exists search_detector_featurer_id integer default 2801 not null;

alter table search_alias
    add if not exists search_detector_params hstore;

alter table search_alias
    drop constraint if exists search_alias_detector_featurer_id_fk;

alter table search_alias
    add constraint search_alias_detector_featurer_id_fk
        foreign key (search_detector_featurer_id) references featurer
            on update cascade;

alter table search
    drop constraint if exists search_search_alias_id_fk;

alter table search
    alter column search_alias_id type uuid using search_alias_id::uuid;

alter table search_alias
    alter column id type uuid using id::uuid;

alter table search
    add constraint search_search_alias_id_fk
        foreign key (search_alias_id) references search_alias on update cascade on delete cascade;

create unique index if not exists search_alias_domain_id_alias_uindex
    on search_alias (domain_id, alias);
