create or replace function recalculate_inherited_fields(selected_domain_id uuid) returns void
    language plpgsql
as
$$
begin
    insert into face (id, domain_id, face_component_id, name, description, created_at, created_by_user_id)
    values ('657db1d7-f996-448a-afc6-77586aa556ce', null, 'BC001', 'RECALCULATE FUNC FACE', 'face only to use in recalculate function', now(), '00000000-0000-0000-0000-000000000000')
    on conflict do nothing;

    create temp table face_data as
    select id, page_face_id
    from twin_class
    where page_face_id is not null and domain_id=selected_domain_id;

    create temp table bc_data as
    select id, bread_crumbs_face_id
    from twin_class
    where bread_crumbs_face_id is not null and domain_id=selected_domain_id;

    update twin_class
    set page_face_id = '657db1d7-f996-448a-afc6-77586aa556ce'
    where page_face_id is not null and domain_id=selected_domain_id;

    update twin_class
    set bread_crumbs_face_id = '657db1d7-f996-448a-afc6-77586aa556ce'
    where bread_crumbs_face_id is not null and domain_id=selected_domain_id;

    update twin_class t
    set inherited_page_twin_class_id = parent.id
    from (select distinct on (child.id) child.id as child_id, parent.id
          from twin_class child
                   left join twin_class parent on (
              child.extends_hierarchy_tree <@ parent.extends_hierarchy_tree
                  and parent.id != child.id
                  and parent.page_face_id is not null
              )
          order by child.id, nlevel(parent.extends_hierarchy_tree) desc) parent
    where t.id = parent.child_id
      and (t.inherited_page_twin_class_id is distinct from parent.id) and domain_id=selected_domain_id;

    update twin_class t
    set inherited_bread_crumbs_twin_class_id = parent.id
    from (select distinct on (child.id) child.id as child_id, parent.id
          from twin_class child
                   left join twin_class parent on (
              child.extends_hierarchy_tree <@ parent.extends_hierarchy_tree
                  and parent.id != child.id
                  and parent.bread_crumbs_face_id is not null
              )
          order by child.id, nlevel(parent.extends_hierarchy_tree) desc) parent
    where t.id = parent.child_id
      and (t.inherited_bread_crumbs_twin_class_id is distinct from parent.id) and domain_id=selected_domain_id;

    update twin_class tc
    set page_face_id = fd.page_face_id
    from face_data fd
    where tc.id = fd.id and domain_id=selected_domain_id;

    update twin_class
    set inherited_page_face_id = null
    where inherited_page_twin_class_id is null and domain_id=selected_domain_id;

    update twin_class tc
    set bread_crumbs_face_id = fd.bread_crumbs_face_id
    from bc_data fd
    where tc.id = fd.id and domain_id=selected_domain_id;

    update twin_class
    set inherited_bread_crumbs_face_id = null
    where inherited_bread_crumbs_twin_class_id is null and domain_id=selected_domain_id;

    drop table if exists face_data;
    drop table if exists bc_data;
    delete from face where id='657db1d7-f996-448a-afc6-77586aa556ce';
end;
$$;
