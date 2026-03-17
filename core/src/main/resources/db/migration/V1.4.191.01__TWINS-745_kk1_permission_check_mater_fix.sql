create or replace function public.permission_check_mater(p_permission_schema_id uuid, p_permission_id uuid, p_permission_space_id uuid, p_user_id uuid, p_user_group_footprint_id uuid, p_twin_class_id uuid, p_twin_creator boolean, p_twin_assignee boolean) returns boolean
    immutable
    language plpgsql
as
$$
begin
    if p_permission_id is null then return true;
end if;
    if p_permission_space_id is null then
        return
            exists (
                select 1
                from permission_grant_twin_role pgtr
                where pgtr.permission_id = p_permission_id
                  and pgtr.permission_schema_id = p_permission_schema_id
                  and pgtr.twin_class_id = p_twin_class_id
                  and (
                    (pgtr.granted_to_assignee and p_twin_assignee)
                        or (pgtr.granted_to_creator and p_twin_creator)
                    )
            )
                or exists (
                select 1
                from permission_mater_global pmg
                where pmg.permission_id = p_permission_id
                  and pmg.user_group_footprint_id = p_user_group_footprint_id
                  and pmg.grants_count > 0
            )
                or exists (
                select 1
                from permission_mater_user_group pmug
                where pmug.permission_id = p_permission_id
                  and pmug.permission_schema_id = p_permission_schema_id
                  and pmug.user_group_footprint_id = p_user_group_footprint_id
                  and pmug.grants_count > 0
            )
                or exists (
                select 1
                from permission_grant_user pgu
                where pgu.permission_id = p_permission_id
                  and pgu.permission_schema_id = p_permission_schema_id
                  and pgu.user_id = p_user_id
            );

else
        return
            exists (
                select 1
                from permission_grant_twin_role pgtr
                         left join twin sp on sp.id = p_permission_space_id
                where pgtr.permission_id = p_permission_id
                  and pgtr.permission_schema_id = p_permission_schema_id
                  and pgtr.twin_class_id = p_twin_class_id
                  and (
                    (pgtr.granted_to_assignee and p_twin_assignee)
                        or (pgtr.granted_to_creator and p_twin_creator)
                        or (pgtr.granted_to_space_assignee and sp.assigner_user_id = p_user_id)
                        or (pgtr.granted_to_space_creator and sp.created_by_user_id = p_user_id)
                    )
            )
                or exists (
                select 1
                from permission_mater_global pmg
                where pmg.permission_id = p_permission_id
                  and pmg.user_group_footprint_id = p_user_group_footprint_id
                  and pmg.grants_count > 0
            )
                or exists (
                select 1
                from permission_mater_user_group pmug
                where pmug.permission_id = p_permission_id
                  and pmug.permission_schema_id = p_permission_schema_id
                  and pmug.user_group_footprint_id = p_user_group_footprint_id
                  and pmug.grants_count > 0
            )
                or exists (
                select 1
                from permission_grant_user pgu
                where pgu.permission_id = p_permission_id
                  and pgu.permission_schema_id = p_permission_schema_id
                  and pgu.user_id = p_user_id
            )
                or exists (
                select 1
                from permission_mater_space_user pmsu
                where pmsu.permission_id = p_permission_id
                  and pmsu.permission_schema_id = p_permission_schema_id
                  and pmsu.twin_id = p_permission_space_id
                  and pmsu.user_id = p_user_id
                  and pmsu.grants_count > 0
            )
                or exists (
                select 1
                from permission_mater_space_user_group pmsug
                where pmsug.permission_id = p_permission_id
                  and pmsug.permission_schema_id = p_permission_schema_id
                  and pmsug.twin_id = p_permission_space_id
                  and pmsug.user_group_footprint_id = p_user_group_footprint_id
                  and pmsug.grants_count > 0
            );
end if;

end;
$$