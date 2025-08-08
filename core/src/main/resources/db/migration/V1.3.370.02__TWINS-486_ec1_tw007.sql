create table if not exists face_tw007(
    id                                  uuid primary key,
    face_id                             uuid not null references face on update cascade on delete restrict,
    twin_pointer_validator_rule_id      uuid references twin_pointer_validator_rule on update cascade on delete restrict,
    twin_class_search_id                uuid not null references twin_class_search on update cascade on delete restrict,
    target_twin_pointer_id              uuid references twin_pointer on update cascade on delete restrict,
    icon_resource_id                    uuid references resource on update cascade on delete restrict,
    label_id                            uuid not null references i18n on update cascade on delete restrict,
    class_selector_label_i18n_id        uuid not null references i18n on update cascade on delete restrict
);

create index if not exists face_tw007_twin_pointer_validator_rule_id_idx
    on face_tw007 (twin_pointer_validator_rule_id);

create index if not exists face_tw007_twin_class_search_id_idx
    on face_tw007 (twin_class_search_id);

create index if not exists face_tw007_target_twin_pointer_id_idx
    on face_tw007 (target_twin_pointer_id);

create index if not exists face_tw007_icon_resource_id_idx
    on face_tw007 (icon_resource_id);

create index if not exists face_tw007_label_id_idx
    on face_tw007 (label_id);

create index if not exists face_tw007_class_selector_label_i18n_id_idx
    on face_tw007 (class_selector_label_i18n_id);


alter table face_tc001_option
    alter column class_selector_label_i18n_id set not null;
