-- Sales user group (00000000-0000-0000-0006-000000000004): view-only admin profile.
-- VIEW + section MANAGE only. No CREATE / UPDATE / DELETE / TRANSITION_PERFORM.
-- Group may already exist on wnr_twins_dev; i18n / user_group ids match that row.
-- Grants: ON CONFLICT DO NOTHING covers both PK and unique (permission_id, user_group_id)
-- because live one-shot grants used gen_random_uuid.

-- i18n for group name
INSERT INTO i18n (id, name, key, i18n_type_id) VALUES ('00000000-0001-0001-0006-000000000004', 'user_group[domain_sales_viewer]', null, 'userGroupName') on conflict (id) do nothing;
-- i18n for group description
INSERT INTO i18n (id, name, key, i18n_type_id) VALUES ('00000000-0002-0001-0006-000000000004', 'user_group[domain_sales_viewer]', null, 'userGroupDescription') on conflict (id) do nothing;
-- i18n translations for name
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0001-0001-0006-000000000004', 'en', 'Sales user group', 2) on conflict (i18n_id, locale) do nothing;
-- i18n translations for description
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0002-0001-0006-000000000004', 'en', 'Sales group with view-only admin permissions (no CRUD operations)', 2) on conflict (i18n_id, locale) do nothing;
-- Create the Sales user group
INSERT INTO user_group (id, domain_id, business_account_id, user_group_type_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0006-000000000004', null, null, 'systemScopeDomainManage', '00000000-0001-0001-0006-000000000004', '00000000-0002-0001-0006-000000000004') on conflict (id) do nothing;

-- Grant VIEW + section MANAGE permissions to Sales group

-- SYSTEM_APP_INFO_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000401', '00000000-0000-0004-0001-000000000201', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- LOG_SUBSTITUTION_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000402', '00000000-0000-0004-0001-000000000301', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWINFLOW_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000403', '00000000-0000-0004-0002-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_CLASS_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000404', '00000000-0000-0004-0004-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_CLASS_FIELD_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000405', '00000000-0000-0004-0005-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TRANSITION_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000406', '00000000-0000-0004-0007-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- LINK_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000407', '00000000-0000-0004-0008-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- LINK_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000408', '00000000-0000-0004-0008-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- DOMAIN_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000409', '00000000-0000-0004-0009-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- DOMAIN_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000410', '00000000-0000-0004-0009-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- DOMAIN_TWINS_VIEW_ALL
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000411', '00000000-0000-0004-0009-000000000006', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_STATUS_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000412', '00000000-0000-0004-0010-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000413', '00000000-0000-0004-0011-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000414', '00000000-0000-0004-0011-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- COMMENT_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000415', '00000000-0000-0004-0012-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- ATTACHMENT_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000416', '00000000-0000-0004-0013-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- USER_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000417', '00000000-0000-0004-0014-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- USER_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000418', '00000000-0000-0004-0014-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- USER_GROUP_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000419', '00000000-0000-0004-0015-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- USER_GROUP_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000420', '00000000-0000-0004-0015-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000421', '00000000-0000-0004-0019-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000422', '00000000-0000-0004-0019-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_GRANT_SPACE_ROLE_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000423', '00000000-0000-0004-0021-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_GRANT_SPACE_ROLE_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000424', '00000000-0000-0004-0021-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_GRANT_TWIN_ROLE_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000425', '00000000-0000-0004-0022-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_GRANT_TWIN_ROLE_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000426', '00000000-0000-0004-0022-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_GRANT_USER_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000427', '00000000-0000-0004-0023-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_GRANT_USER_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000428', '00000000-0000-0004-0023-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_GRANT_USER_GROUP_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000429', '00000000-0000-0004-0024-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_GRANT_USER_GROUP_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000430', '00000000-0000-0004-0024-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_GROUP_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000431', '00000000-0000-0004-0025-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_GROUP_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000432', '00000000-0000-0004-0025-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_SCHEMA_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000433', '00000000-0000-0004-0026-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- PERMISSION_SCHEMA_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000434', '00000000-0000-0004-0026-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- USER_PERMISSION_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000435', '00000000-0000-0004-0027-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- USER_PERMISSION_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000436', '00000000-0000-0004-0027-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- DOMAIN_BUSINESS_ACCOUNT_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000437', '00000000-0000-0004-0036-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- DOMAIN_BUSINESS_ACCOUNT_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000438', '00000000-0000-0004-0036-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- DOMAIN_USER_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000439', '00000000-0000-0004-0037-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- BUSINESS_ACCOUNT_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000440', '00000000-0000-0004-0038-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- BUSINESS_ACCOUNT_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000441', '00000000-0000-0004-0038-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- FACE_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000442', '00000000-0000-0004-0042-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- FACE_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000443', '00000000-0000-0004-0042-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- HISTORY_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000444', '00000000-0000-0004-0044-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- HISTORY_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000445', '00000000-0000-0004-0044-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- HISTORY_MACHINE_USER_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000446', '00000000-0000-0004-0044-000000000006', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_LINK_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000447', '00000000-0000-0004-0059-000000000001', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_LINK_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000448', '00000000-0000-0004-0059-000000000003', '00000000-0000-0000-0006-000000000004', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;

-- refactoring
UPDATE i18n_translation SET translation = 'Domain super admin' WHERE i18n_id = '00000000-0000-0001-0006-000000000001' AND locale = 'en';
UPDATE i18n_translation SET translation = 'The user can create, delete, and modify everything.' WHERE i18n_id = '00000000-0000-0001-0006-000000000002';

-- Domain admin viewer (...0003): fill missing system VIEW + section MANAGE.
-- No CREATE / UPDATE / DELETE / TRANSITION_PERFORM.
-- Some original viewer VIEW grants from V1.4.210.01 never landed: grant ids
-- 00000000-0000-0000-0007-000000000212..216 were already used by TWIN_TRIGGER on ...0001.
-- Drop leftover group ...0002 (WNR-only duplicate of Domain admin). Move its members to ...0003.

-- ATTACHMENT_RESTRICTION_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000449', '00000000-0000-0004-0061-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- ATTACHMENT_RESTRICTION_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000450', '00000000-0000-0004-0061-000000000003', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- FACTORY_MULTIPLIER_PARAM_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000451', '00000000-0000-0004-0031-000000000006', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- LINK_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000452', '00000000-0000-0004-0008-000000000003', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TRANSITION_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000453', '00000000-0000-0004-0007-000000000003', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_CLASS_CARD_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000454', '00000000-0000-0004-0006-000000000003', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_CLASS_FIELD_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000455', '00000000-0000-0004-0005-000000000003', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_CLASS_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000456', '00000000-0000-0004-0004-000000000003', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_LINK_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000457', '00000000-0000-0004-0059-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_LINK_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000458', '00000000-0000-0004-0059-000000000003', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_POINTER_MANAGE
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000459', '00000000-0000-0004-0058-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;
-- TWIN_POINTER_VIEW
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000460', '00000000-0000-0004-0058-000000000003', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict do nothing;

-- Members of ...0002 -> ...0003 (same domain). Trigger fills user_group_type_id.
INSERT INTO user_group_map (id, user_group_id, domain_id, business_account_id, user_id, added_at, added_by_user_id)
SELECT gen_random_uuid(),
       '00000000-0000-0000-0006-000000000003',
       src.domain_id,
       NULL,
       src.user_id,
       now(),
       '00000000-0000-0000-0000-000000000000'
FROM user_group_map src
WHERE src.user_group_id = '00000000-0000-0000-0006-000000000002'
  AND NOT EXISTS (
    SELECT 1
    FROM user_group_map already
    WHERE already.user_group_id = '00000000-0000-0000-0006-000000000003'
      AND already.user_id = src.user_id
      AND already.domain_id IS NOT DISTINCT FROM src.domain_id
        AND already.business_account_id IS NULL
)
    on conflict do nothing;

DELETE FROM permission_grant_global
WHERE user_group_id = '00000000-0000-0000-0006-000000000002';

DELETE FROM user_group_map
WHERE user_group_id = '00000000-0000-0000-0006-000000000002';

DO $$
DECLARE
r record;
BEGIN
FOR r IN
SELECT DISTINCT user_group_footprint_id
FROM user_group_footprint_map
WHERE user_group_id = '00000000-0000-0000-0006-000000000002'
    LOOP
        PERFORM user_group_footprint_invalidate(r.user_group_footprint_id);
END LOOP;
END
$$;

DELETE FROM user_group
WHERE id = '00000000-0000-0000-0006-000000000002';

