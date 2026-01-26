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


insert into twin_class(id, domain_id, key, permission_schema_space, abstract,
                       head_twin_class_id, extends_twin_class_id, name_i18n_id,
                       description_i18n_id, logo, created_by_user_id, created_at,
                       twin_class_owner_type_id, domain_alias_counter, marker_data_list_id,
                       tag_data_list_id, twinflow_schema_space, twin_class_schema_space,
                       alias_space, view_permission_id, head_hierarchy_tree, extends_hierarchy_tree,
                       head_hunter_featurer_id, head_hunter_featurer_params, create_permission_id,
                       edit_permission_id, delete_permission_id, page_face_id, assignee_required,
                       general_attachment_restriction_id, comment_attachment_restriction_id,
                       external_id, bread_crumbs_face_id)
values ('00000000-0000-0000-0001-000000000005', null, 'FACE_PAGE',
        null, false, null,
        null, null, null, null,
        '00000000-0000-0000-0000-000000000000', now(),
        'system', 0, null,
        null, null, null,
        null, null, null, null,
        null, null, null,
        null, null, null, true,
        null, null, null, null)
on conflict do nothing;

insert into i18n (id, key, name, i18n_type_id, domain_id)
values ('00000000-0000-0000-0012-000000000037', null, null, 'twinStatusName', null),
       ('00000000-0000-0000-0012-000000000038', null, null, 'twinStatusDescription', null)
on conflict do nothing;

insert into i18n_translation (i18n_id, locale, translation, usage_counter)
values ('00000000-0000-0000-0012-000000000037', 'en', 'Published', 0),
       ('00000000-0000-0000-0012-000000000038', 'en', 'Face page published', 0)
on conflict do nothing;

insert into twin_status (id, twins_class_id, name_i18n_id, description_i18n_id, logo, background_color, key, font_color)
values ('00000000-0000-0000-0003-000000000004', '00000000-0000-0000-0001-000000000005', '00000000-0000-0000-0012-000000000037',
        '00000000-0000-0000-0012-000000000038', null, null, null, null)
on conflict do nothing;

insert into twin (id, twin_class_id, head_twin_id,
                  external_id, twin_status_id, name,
                  description, created_by_user_id, assigner_user_id,
                  created_at, owner_business_account_id, owner_user_id,
                  hierarchy_tree, view_permission_id, permission_schema_space_id,
                  twinflow_schema_space_id, twin_class_schema_space_id, alias_space_id, page_face_id, bread_crumbs_face_id)
select
    uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, id::text),
    '00000000-0000-0000-0001-000000000005', null, null,
    '00000000-0000-0000-0003-000000000004', key, null, '00000000-0000-0000-0000-000000000000',
    null, now(), null, null,
    null, null, null, null,
    null, null, target_page_face_id, null
from face_navbar_nb001_menu_item
on conflict do nothing;


update face_navbar_nb001_menu_item t1
set
    target_twin_id = t2.id
from twin t2
where t2.id = uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, t1.id::text);
