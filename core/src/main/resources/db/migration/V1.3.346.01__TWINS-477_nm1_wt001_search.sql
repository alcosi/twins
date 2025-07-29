DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'face_wt001'
              AND column_name = 'search_target_twin_pointer_id'
        ) THEN
            alter table face_wt001
                rename column show_create_button to search_target_twin_pointer_id;

            alter table face_wt001
                add show_create_button boolean default true not null;

            update face_wt001 set show_create_button = search_target_twin_pointer_id;

            alter table face_wt001
                alter column search_target_twin_pointer_id drop not null;

            alter table face_wt001
                alter column search_target_twin_pointer_id drop default;

            alter table face_wt001
                alter column search_target_twin_pointer_id type uuid using null;

            create index face_wt001_search_target_twin_pointer_id_index
                on face_wt001 (search_target_twin_pointer_id);

            alter table face_wt001
                add constraint face_wt001_twin_pointer_id_fk
                    foreign key (search_target_twin_pointer_id) references twin_pointer
                        on update cascade on delete restrict;
        END IF;
    END
$$;




