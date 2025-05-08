alter table public.permission_grant_twin_role
    alter column permission_schema_id set not null;

alter table public.permission_grant_twin_role
    alter column permission_id set not null;

alter table public.permission_grant_twin_role
    alter column twin_class_id set not null;

alter table public.permission_grant_twin_role
    alter column twin_role_id set not null;

alter table public.permission_grant_twin_role
    alter column granted_by_user_id set not null;