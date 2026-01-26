alter table twin_class_field_search_predicate
    alter column field_finder_params drop not null;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'face_tw004'
              AND column_name = 'twin_class_field_search_id'
        ) THEN
            insert into twin_class_field_search (id, domain_id, name)
                select uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, face_tw004.id::varchar), face.domain_id, ''
                from face_tw004, face where face_tw004.face_id = face.id on conflict do nothing;

            insert into twin_class_field_search_predicate (id, twin_class_field_search_id, field_finder_featurer_id, field_finder_params)
                select
                    uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, concat(face_tw004.id, face_tw004.field_finder_featurer_id)::text),
                    uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, face_tw004.id::varchar),
                    face_tw004.field_finder_featurer_id, field_finder_params
                from face_tw004, face where face_tw004.face_id = face.id on conflict do nothing;

            alter table face_tw004
                drop constraint face_tw004_field_finder_featurer_id_fk;

            alter table face_tw004
                rename column field_finder_featurer_id to twin_class_field_search_id;

            alter table face_tw004
                alter column twin_class_field_search_id drop not null;
            
            alter table face_tw004
                alter column twin_class_field_search_id type uuid using null;

            alter table face_tw004
                drop column field_finder_params;

            update face_tw004 set twin_class_field_search_id = uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, face_tw004.id::varchar);

            alter table face_tw004
                alter column twin_class_field_search_id set not null;

            alter table face_tw004
                add constraint face_tw004_twin_class_field_search_id_fk
                    foreign key (twin_class_field_search_id) references twin_class_field_search
                        on update cascade on delete restrict;

            alter index public.face_tw004_field_finder_featurer_id_idx rename to face_tw004_twin_class_field_search_idx;
        END IF;
    END
$$;


