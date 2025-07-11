create table if not exists face_tw006_action (
    id uuid not null primary key,
    face_tw_006_id uuid not null references face_tw006 on update cascade on delete cascade,
    twin_action_id varchar(255) not null references twin_action on update cascade on delete cascade,
    label_i18n_id uuid references i18n on update cascade on delete restrict
);

create index if not exists face_tw006_action_face_tw_006_id_idx
    on face_tw006_action (face_tw_006_id);

create index if not exists face_tw006_action_twin_action_id_idx
    on face_tw006_action (twin_action_id);

create index if not exists face_tw006_action_label_i18n_id_idx
    on face_tw006_action (label_i18n_id);
