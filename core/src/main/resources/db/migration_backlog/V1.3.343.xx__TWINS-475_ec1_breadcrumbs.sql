insert into face_component_type(id, name, description)
values ('BREADCRUMBS', 'Breadcrumbs', null)
on conflict do nothing;


insert into face_component(id, face_component_type_id, name, description)
values ('BC001', 'BREADCRUMBS', 'Breadcrumb navigation', null)
on conflict do nothing;


create table if not exists face_bc001(
    id                              uuid not null primary key,
    face_id                         uuid not null references face on update cascade on delete cascade,
    twin_pointer_validator_rule_id  uuid references twin_pointer_validator_rule on update cascade on delete restrict,
    name                            varchar
);

create index if not exists face_bc001_face_id_idx
    on face_bc001 (face_id);

create index if not exists face_bc001_twin_pointer_validator_rule_id_idx
    on face_bc001 (twin_pointer_validator_rule_id);


create table if not exists face_bc001_item(
    id                  uuid not null primary key,
    face_bc001_id       uuid not null references face_bc001 on update cascade on delete cascade,
    "order"             integer not null,
    twin_pointer_id     uuid not null references twin_pointer on update cascade on delete restrict,
    icon_resource_id    uuid references resource on update cascade on delete restrict,
    label_id            uuid references i18n on update cascade on delete restrict
);

create index if not exists face_bc001_item_face_bc001_id_idx
    on face_bc001_item(face_bc001_id);

create index if not exists face_bc001_twin_pointer_id_idx
    on face_bc001_item(twin_pointer_id);

create index if not exists face_bc001_icon_resource_id_idx
    on face_bc001_item(icon_resource_id);

create index if not exists face_bc001_label_id_idx
    on face_bc001_item(label_id);


alter table face_navbar_nb001_menu_item
    add column if not exists target_twin_id uuid references twin on update cascade on delete restrict;

create index if not exists face_navbar_nb001_menu_item_target_twin_id_idx
    on face_navbar_nb001_menu_item(target_twin_id);


alter table twin_class
    add column if not exists bread_crumbs_face_id uuid references face on update cascade on delete restrict;

create index if not exists twin_class_bread_crumbs_face_id_idx
    on twin_class (bread_crumbs_face_id);


alter table twin
    add column if not exists page_face_id uuid references face on update cascade on delete restrict,
    add column if not exists bread_crumbs_face_id uuid references face on update cascade on delete restrict;

create index if not exists twin_page_face_id_idx
    on twin (page_face_id);

create index if not exists twin_bread_crumbs_face_id_idx
    on twin (bread_crumbs_face_id);
