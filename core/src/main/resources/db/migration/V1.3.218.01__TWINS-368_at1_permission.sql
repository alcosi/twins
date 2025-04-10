-- permission for system group
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000032', '00000000-0000-0000-0004-000000000033', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;

-- delete extra entry form v1.3.217.02
DELETE FROM public.permission_grant_user_group WHERE id = 'cb787684-c0fd-4458-9416-f0e5313af7ae'::uuid;
