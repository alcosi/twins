alter table face_tw007
    add column if not exists save_changes_label_i18n_id uuid references i18n on update cascade on delete restrict;

create index if not exists face_tw007_save_changes_label_i18n_id
    on face_tw007 (save_changes_label_i18n_id);
