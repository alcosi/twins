alter table face_wt003
    add column if not exists substitution_from_twin_pointer_id          uuid references twin_pointer on update cascade on delete restrict,
    add column if not exists title_substitution_twin_class_field_id     uuid references twin_class_field on update cascade on delete restrict,
    add column if not exists message_substitution_twin_class_field_id   uuid references twin_class_field on update cascade on delete restrict;

create index if not exists face_wt003_substitution_from_twin_pointer_id_idx
    on face_wt003 (substitution_from_twin_pointer_id);

create index if not exists face_wt003_title_substitution_twin_class_field_id_idx
    on face_wt003 (title_substitution_twin_class_field_id);

create index if not exists face_wt003_message_substitution_twin_class_field_id_idx
    on face_wt003 (message_substitution_twin_class_field_id);
