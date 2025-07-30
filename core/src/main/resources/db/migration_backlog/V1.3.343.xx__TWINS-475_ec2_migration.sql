insert into face(id, domain_id, face_component_id, name, description, created_at, created_by_user_id)
values ('dd4f2d78-e633-46d8-bbb2-f0c4d636d254', null, 'BC001', 'Breadcrumbs navigation panel', null, now(), '00000000-0000-0000-0000-000000000000')
on conflict do nothing;


insert into twin_class(id, domain_id, key, permission_schema_space, abstract,
                       head_twin_class_id, extends_twin_class_id, name_i18n_id,
                       description_i18n_id, logo, created_by_user_id, created_at,
                       twin_class_owner_type_id, domain_alias_counter, marker_data_list_id,
                       tag_data_list_id, twinflow_schema_space, twin_class_schema_space,
                       alias_space, view_permission_id, head_hierarchy_tree, extends_hierarchy_tree,
                       head_hunter_featurer_id, head_hunter_featurer_params, create_permission_id,
                       edit_permission_id, delete_permission_id, page_face_id, assignee_required,
                       general_attachment_restriction_id, comment_attachment_restriction_id,
                       external_id, face_relative_path, bread_crumbs_face_id)
values ('00000000-0000-0000-0001-000000000005', null, 'FACE_PAGE',
        null, false, null,
        null, null, null, null,
        '00000000-0000-0000-0000-000000000000', now(),
        'system', 0, null,
        null, null, null,
        null, null, null, null,
        null, null, null,
        null, null, null, true,
        null, null, null, null, 'dd4f2d78-e633-46d8-bbb2-f0c4d636d254')
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
    '00000000-0000-0000-0003-000000000001', 'navpan item', null, '00000000-0000-0000-0000-000000000000',
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
