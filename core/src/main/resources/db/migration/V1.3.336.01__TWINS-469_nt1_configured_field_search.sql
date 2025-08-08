create table if not exists twin_class_field_search
(
    id                  uuid      not null primary key,
    domain_id           uuid references domain (id) on delete cascade on update cascade,
    name         varchar   not null
     );

insert into twin_class_field_search (id, domain_id, name) values ('00000000-0000-0000-0014-000000000001', null, 'Unlimited class fields search') on conflict do nothing ;

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


DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.table_constraints
            WHERE constraint_type = 'FOREIGN KEY'
              AND constraint_name = 'face_tc001_option_twin_class_field_search_id_fk'
              AND table_name = 'face_tc001_option'
        ) THEN
            insert into twin_class_field_search (id, domain_id, name) select twin_class_field_search_id, face.domain_id, ''
                                                                             from face_tc001_option, face_tc001, face where face_tc001_option.face_tc001_id = face_tc001.id and face_tc001.face_id = face.id and twin_class_field_search_id IS NOT NULL on conflict do nothing ;

            ALTER TABLE face_tc001_option
                ADD CONSTRAINT face_tc001_option_twin_class_field_search_id_fk
                    FOREIGN KEY (twin_class_field_search_id)
                        REFERENCES twin_class_field_search
                        ON UPDATE CASCADE
                        ON DELETE RESTRICT;
        END IF;
    END $$;
