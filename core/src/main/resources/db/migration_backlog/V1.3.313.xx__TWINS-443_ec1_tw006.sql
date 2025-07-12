create table if not exists face_tw006(
    id uuid not null primary key,
    face_id uuid not null references face on update cascade on delete restrict,
    twin_pointer_validator_rule_id uuid references twin_pointer_validator_rule on update cascade on delete cascade,
    target_twin_pointer_id uuid references twin_pointer on update cascade on delete restrict,
    style_classes varchar,
    ui_type varchar
);

create index if not exists face_tw006_face_id_idx
    on face_tw006 (face_id);

create index if not exists face_tw006_twin_pointer_validator_rule_id_idx
    on face_tw006 (twin_pointer_validator_rule_id);

create index if not exists face_tw006_target_twin_pointer_id_idx
    on face_tw006 (target_twin_pointer_id);
