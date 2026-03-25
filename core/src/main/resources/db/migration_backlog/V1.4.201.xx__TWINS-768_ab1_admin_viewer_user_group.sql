-- Create domainAdminViewer group with view-level permissions only (no CRUD)

-- i18n for group name
INSERT INTO i18n (id, name, key, i18n_type_id)
VALUES ('00000000-0000-0001-0006-000000000003', 'user_group[domain_admin_viewer]', null, 'userGroupName')
on conflict (id) do nothing;

-- i18n for group description
INSERT INTO i18n (id, name, key, i18n_type_id)
VALUES ('00000000-0000-0001-0006-000000000004', 'user_group[domain_admin_viewer]', null, 'userGroupDescription')
on conflict (id) do nothing;

-- i18n translations for name
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('00000000-0000-0001-0006-000000000003', 'en', 'Domain admin viewer', 2)
on conflict (i18n_id, locale) do nothing;

-- i18n translations for description
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('00000000-0000-0001-0006-000000000004', 'en', 'Domain administrator with view-only permissions (no CRUD operations)', 2)
on conflict (i18n_id, locale) do nothing;

-- Create the domain admin viewer user group
INSERT INTO user_group (id, domain_id, business_account_id, user_group_type_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0006-000000000003', null, null,
        'systemScopeDomainManage', '00000000-0000-0001-0006-000000000003',
        '00000000-0000-0001-0006-000000000004')
on conflict (id) do nothing;

-- Grant VIEW permissions to domainAdminViewer group
-- GENERAL view permissions
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000208',
        '00000000-0000-0004-0001-000000000201', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000209',
        '00000000-0000-0004-0001-000000000301', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWINFLOW view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000210',
        '00000000-0000-0004-0002-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWINFLOW SCHEMA view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000211',
        '00000000-0000-0004-0003-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWIN CLASS view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000212',
        '00000000-0000-0004-0004-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWIN CLASS FIELD view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000213',
        '00000000-0000-0004-0005-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWIN CLASS CARD view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000214',
        '00000000-0000-0004-0006-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TRANSITION view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000215',
        '00000000-0000-0004-0007-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- LINK view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000216',
        '00000000-0000-0004-0008-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- DOMAIN view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000217',
        '00000000-0000-0004-0009-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- DOMAIN TWINS VIEW ALL
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000218',
        '00000000-0000-0004-0009-000000000006', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWIN STATUS view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000219',
        '00000000-0000-0004-0010-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWIN view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000220',
        '00000000-0000-0004-0011-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- COMMENT view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000221',
        '00000000-0000-0004-0012-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- ATTACHMENT view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000222',
        '00000000-0000-0004-0013-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- USER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000223',
        '00000000-0000-0004-0014-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- USER GROUP view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000224',
        '00000000-0000-0004-0015-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- DATA LIST view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000225',
        '00000000-0000-0004-0016-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- DATA LIST OPTION view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000226',
        '00000000-0000-0004-0017-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- DATA LIST SUBSET view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000227',
        '00000000-0000-0004-0018-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PERMISSION view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000228',
        '00000000-0000-0004-0019-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- USER GROUP INVOLVE ASSIGNEE view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000229',
        '00000000-0000-0004-0020-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PERMISSION GRANT SPACE ROLE view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000230',
        '00000000-0000-0004-0021-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PERMISSION GRANT TWIN ROLE view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000231',
        '00000000-0000-0004-0022-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PERMISSION GRANT USER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000232',
        '00000000-0000-0004-0023-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PERMISSION GRANT USER GROUP view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000233',
        '00000000-0000-0004-0024-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PERMISSION GROUP view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000234',
        '00000000-0000-0004-0025-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PERMISSION SCHEMA view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000235',
        '00000000-0000-0004-0026-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- USER PERMISSION view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000236',
        '00000000-0000-0004-0027-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- I18N view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000237',
        '00000000-0000-0004-0028-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- ERASER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000238',
        '00000000-0000-0004-0029-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- FACTORY view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000239',
        '00000000-0000-0004-0030-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- MULTIPLIER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000240',
        '00000000-0000-0004-0031-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PIPELINE view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000241',
        '00000000-0000-0004-0032-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- CONDITION SET view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000242',
        '00000000-0000-0004-0033-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- BRANCH view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000243',
        '00000000-0000-0004-0034-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- DRAFT view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000244',
        '00000000-0000-0004-0035-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- DOMAIN BUSINESS ACCOUNT view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000245',
        '00000000-0000-0004-0036-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- DOMAIN USER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000246',
        '00000000-0000-0004-0037-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- BUSINESS ACCOUNT view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000247',
        '00000000-0000-0004-0038-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- SPACE ROLE view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000248',
        '00000000-0000-0004-0039-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- FEATURER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000249',
        '00000000-0000-0004-0040-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TIER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000250',
        '00000000-0000-0004-0041-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- FACE view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000251',
        '00000000-0000-0004-0042-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PIPELINE STEP view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000252',
        '00000000-0000-0004-0043-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- HISTORY view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000253',
        '00000000-0000-0004-0044-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- HISTORY MACHINE USER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000254',
        '00000000-0000-0004-0044-000000000006', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PROJECTION view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000255',
        '00000000-0000-0004-0045-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- PROJECTION EXCLUSION view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000256',
        '00000000-0000-0004-0046-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWIN CLASS FIELD RULE view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000257',
        '00000000-0000-0004-0047-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWINFLOW FACTORY view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000258',
        '00000000-0000-0004-0048-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWIN CLASS FREEZE view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000259',
        '00000000-0000-0004-0049-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- HISTORY NOTIFICATION view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000260',
        '00000000-0000-0004-0050-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- SCHEDULER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000261',
        '00000000-0000-0004-0051-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWIN CLASS DYNAMIC MARKER view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000262',
        '00000000-0000-0004-0052-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;

-- TWIN VALIDATOR SET view
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000263',
        '00000000-0000-0004-0053-000000000003', '00000000-0000-0000-0006-000000000003',
        '00000000-0000-0000-0000-000000000000', default)
on conflict (id) do nothing;
