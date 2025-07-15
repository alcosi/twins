alter table face_tw004
    add column if not exists style_classes varchar,
    add column if not exists label_i18n_id uuid references i18n on update cascade on delete restrict;

create index if not exists face_tw004_label_i18n_id_idx
    on face_tw004 (label_i18n_id);
