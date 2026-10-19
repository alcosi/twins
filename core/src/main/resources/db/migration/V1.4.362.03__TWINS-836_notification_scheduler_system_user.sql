-- System user the history-notification scheduler acts on behalf of (TWINS-836).
-- Its permissions are hardcoded in PermissionService.SYSTEM_USER_PERMISSIONS (DOMAIN_TWINS_VIEW_ALL),
-- valid in every domain — no group membership or permission grants needed. The row exists so the
-- identity can be resolved wherever ApiUser.getUser() is called.
insert into "user" (id, name, created_at)
values ('00000000-0000-0000-0000-000000000002', 'NOTIFICATION_SCHEDULER', now())
on conflict (id) do nothing;
