alter table public.permission_grant_assignee_propagation
    alter column permission_schema_id set not null;

alter table public.permission_grant_assignee_propagation
    alter column permission_id set not null;

alter table public.permission_grant_assignee_propagation
    alter column propagation_by_twin_class_id set not null;

alter table public.permission_grant_assignee_propagation
    alter column granted_by_user_id set not null;