-- Grant MANAGE permissions to domainAdminViewer group (id: 00000000-0000-0000-0006-000000000003)
-- TWINFLOW manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000289', '00000000-0000-0004-0002-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWINFLOW SCHEMA manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000290', '00000000-0000-0004-0003-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN CLASS manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000291', '00000000-0000-0004-0004-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN CLASS FIELD manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000292', '00000000-0000-0004-0005-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN CLASS CARD manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000293', '00000000-0000-0004-0006-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TRANSITION manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000294', '00000000-0000-0004-0007-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- LINK manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000295', '00000000-0000-0004-0008-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- DOMAIN manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000296', '00000000-0000-0004-0009-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN STATUS manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000297', '00000000-0000-0004-0010-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000298', '00000000-0000-0004-0011-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- COMMENT manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000299', '00000000-0000-0004-0012-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- ATTACHMENT manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000300', '00000000-0000-0004-0013-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- USER manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000301', '00000000-0000-0004-0014-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- USER GROUP manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000302', '00000000-0000-0004-0015-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- DATA LIST manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000303', '00000000-0000-0004-0016-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- DATA LIST OPTION manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000304', '00000000-0000-0004-0017-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- DATA LIST SUBSET manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000305', '00000000-0000-0004-0018-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- PERMISSION manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000306', '00000000-0000-0004-0019-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- USER GROUP INVOLVE ASSIGNEE manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000307', '00000000-0000-0004-0020-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- PERMISSION GRANT SPACE ROLE manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000308', '00000000-0000-0004-0021-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- PERMISSION GRANT TWIN ROLE manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000309', '00000000-0000-0004-0022-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- PERMISSION GRANT USER manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000310', '00000000-0000-0004-0023-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- PERMISSION GRANT USER GROUP manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000311', '00000000-0000-0004-0024-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- PERMISSION GROUP manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000312', '00000000-0000-0004-0025-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- PERMISSION SCHEMA manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000313', '00000000-0000-0004-0026-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- USER PERMISSION manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000314', '00000000-0000-0004-0027-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- I18N manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000315', '00000000-0000-0004-0028-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- FACTORY_ERASER manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000316', '00000000-0000-0004-0029-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- FACTORY manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000317', '00000000-0000-0004-0030-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- FACTORY_MULTIPLIER manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000318', '00000000-0000-0004-0031-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- FACTORY_PIPELINE manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000319', '00000000-0000-0004-0032-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- FACTORY_CONDITION_SET manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000320', '00000000-0000-0004-0033-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- FACTORY_BRANCH manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000321', '00000000-0000-0004-0034-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- DRAFT manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000322', '00000000-0000-0004-0035-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- DOMAIN BUSINESS ACCOUNT manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000323', '00000000-0000-0004-0036-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- DOMAIN USER manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000324', '00000000-0000-0004-0037-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- BUSINESS ACCOUNT manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000325', '00000000-0000-0004-0038-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- SPACE ROLE manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000326', '00000000-0000-0004-0039-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- FEATURER manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000327', '00000000-0000-0004-0040-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TIER manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000328', '00000000-0000-0004-0041-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- FACE manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000329', '00000000-0000-0004-0042-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- FACTORY_PIPELINE_STEP manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000330', '00000000-0000-0004-0043-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- HISTORY manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000331', '00000000-0000-0004-0044-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- PROJECTION manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000332', '00000000-0000-0004-0045-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- PROJECTION EXCLUSION manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000333', '00000000-0000-0004-0046-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN CLASS FIELD RULE manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000334', '00000000-0000-0004-0047-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWINFLOW FACTORY manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000335', '00000000-0000-0004-0048-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN CLASS FREEZE manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000336', '00000000-0000-0004-0049-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- HISTORY NOTIFICATION manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000337', '00000000-0000-0004-0050-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- SCHEDULER manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000338', '00000000-0000-0004-0051-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN CLASS DYNAMIC MARKER manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000339', '00000000-0000-0004-0052-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN VALIDATOR SET manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000340', '00000000-0000-0004-0053-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN VALIDATOR TRIGGER manage
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000341', '00000000-0000-0004-0054-000000000001', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
-- TWIN VALIDATOR TRIGGER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000342', '00000000-0000-0004-0054-000000000003', '00000000-0000-0000-0006-000000000003', '00000000-0000-0000-0000-000000000000', default) on conflict (id) do nothing;
