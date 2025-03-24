
alter table twin_class
    add if not exists page_face_id uuid
        constraint twin_class_page_face_id_fk
            references face
            on update cascade on delete restrict;


