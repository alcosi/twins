-- GENERAL permissions
UPDATE public.permission SET id = '00000000-0000-0004-0001-000000000101'::uuid WHERE id = '00000000-0000-0000-0004-000000000001'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0001-000000000201'::uuid WHERE id = '00000000-0000-0000-0004-000000000156'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0001-000000000301'::uuid WHERE id = '00000000-0000-0000-0004-000000000157'::uuid;
-- TWINFLOW permissions
UPDATE public.permission SET id = '00000000-0000-0004-0002-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000028'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0002-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000172'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0002-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000174'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0002-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000175'::uuid;
insert into public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('29d711a7-6cd2-4740-b81b-962a7fbbb72e', 'Twinflow delete name', null, 'permissionName'),
                                                          ('8ee9e49d-9fe8-4024-8fc2-bf76a3a062be', 'Twinflow delete description', null, 'permissionDescription')
on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
insert into public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('29d711a7-6cd2-4740-b81b-962a7fbbb72e', 'en', 'Twinflow delete', 0),
                                                                                      ('8ee9e49d-9fe8-4024-8fc2-bf76a3a062be', 'en', 'Twinflow delete', 0)
on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
    ('00000000-0000-0004-0002-000000000005', 'TWINFLOW_DELETE', '00000000-0000-0000-0005-000000000001', '29d711a7-6cd2-4740-b81b-962a7fbbb72e', '8ee9e49d-9fe8-4024-8fc2-bf76a3a062be')
on conflict (id) do update set key=excluded.key;
-- TWINFLOW SCHEMA permissions
UPDATE public.permission SET id = '00000000-0000-0004-0003-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000029'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0003-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000176'::uuid;
insert into public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc048', 'Twinflow schema create name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956748', 'Twinflow schema create description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc049', 'Twinflow schema update name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956749', 'Twinflow schema update description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc050', 'Twinflow schema delete name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956750', 'Twinflow schema delete description', null, 'permissionDescription')
on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
insert into public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc048', 'en', 'Twinflow schema create', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956748', 'en', 'Twinflow schema create', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc049', 'en', 'Twinflow schema update', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956749', 'en', 'Twinflow schema update', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc050', 'en', 'Twinflow schema delete', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956750', 'en', 'Twinflow schema delete', 0)
on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0003-000000000002', 'TWINFLOW_SCHEMA_CREATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc048', 'ef4d65eb-594d-3b17-b5d3-66ab79956748'),
                                                                                                    ('00000000-0000-0004-0003-000000000004', 'TWINFLOW_SCHEMA_UPDATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc049', 'ef4d65eb-594d-3b17-b5d3-66ab79956749'),
                                                                                                    ('00000000-0000-0004-0003-000000000005', 'TWINFLOW_SCHEMA_DELETE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc050', 'ef4d65eb-594d-3b17-b5d3-66ab79956750')
on conflict (id) do update set key=excluded.key;
-- TWIN CLASS permissions
UPDATE public.permission SET id = '00000000-0000-0004-0004-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000002'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0004-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000034'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0004-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000037'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0004-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000035'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0004-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000036'::uuid;
-- TWIN CLASS FIELD permissions
UPDATE public.permission SET id = '00000000-0000-0004-0005-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000007'::uuid;
insert into public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc051', 'Twin class field create name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956751', 'Twin class field create description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc052', 'Twin class field view name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956752', 'Twin class field view description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc053', 'Twin class field update name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956753', 'Twin class field update description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc054', 'Twin class field delete name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956754', 'Twin class field delete description', null, 'permissionDescription')
on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
insert into public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc051', 'en', 'Twin class field create', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956751', 'en', 'Twin class field create', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc052', 'en', 'Twin class field view', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956752', 'en', 'Twin class field view', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc053', 'en', 'Twin class field update', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956753', 'en', 'Twin class field update', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc054', 'en', 'Twin class field delete', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956754', 'en', 'Twin class field delete', 0)
on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0005-000000000002', 'TWIN_CLASS_FIELD_CREATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc051', 'ef4d65eb-594d-3b17-b5d3-66ab79956751'),
                                                                                                    ('00000000-0000-0004-0005-000000000003', 'TWIN_CLASS_FIELD_VIEW', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc052', 'ef4d65eb-594d-3b17-b5d3-66ab79956752'),
                                                                                                    ('00000000-0000-0004-0005-000000000004', 'TWIN_CLASS_FIELD_UPDATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc053', 'ef4d65eb-594d-3b17-b5d3-66ab79956753'),
                                                                                                    ('00000000-0000-0004-0005-000000000005', 'TWIN_CLASS_FIELD_DELETE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc054', 'ef4d65eb-594d-3b17-b5d3-66ab79956754')
on conflict (id) do update set key=excluded.key;
-- TWIN CLASS CARD permissions
UPDATE public.permission SET id = '00000000-0000-0004-0006-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000178'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0006-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000169'::uuid;
insert into public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc006', 'Twin class card create name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956706', 'Twin class card create description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc007', 'Twin class card update name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956707', 'Twin class card update description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc008', 'Twin class card delete name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956708', 'Twin class card delete description', null, 'permissionDescription')
on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
insert into public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc006', 'en', 'Twin class card create', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956706', 'en', 'Twin class card create', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc007', 'en', 'Twin class card update', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956707', 'en', 'Twin class card update', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc008', 'en', 'Twin class card delete', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956708', 'en', 'Twin class card delete', 0)
on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0006-000000000002', 'TWIN_CLASS_CARD_CREATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc006', 'ef4d65eb-594d-3b17-b5d3-66ab79956706'),
                                                                                                    ('00000000-0000-0004-0006-000000000004', 'TWIN_CLASS_CARD_UPDATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc007', 'ef4d65eb-594d-3b17-b5d3-66ab79956707'),
                                                                                                    ('00000000-0000-0004-0006-000000000005', 'TWIN_CLASS_CARD_DELETE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc008', 'ef4d65eb-594d-3b17-b5d3-66ab79956708')
on conflict (id) do update set key=excluded.key;
-- Transition permissions
UPDATE public.permission SET id = '00000000-0000-0004-0007-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000003'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0007-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000173'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0007-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000038'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0007-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000039'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0007-000000000006'::uuid WHERE id = '00000000-0000-0000-0004-000000000170'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0007-000000000007'::uuid WHERE id = '00000000-0000-0000-0004-000000000171'::uuid;
insert into public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc002', 'Transition delete name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956702', 'Transition delete description', null, 'permissionDescription')
on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
insert into public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc002', 'en', 'Transition delete', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956702', 'en', 'Transition delete', 0)
on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0007-000000000005', 'TRANSITION_DELETE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc002', 'ef4d65eb-594d-3b17-b5d3-66ab79956702')
on conflict (id) do update set key=excluded.key;
-- Link permissions
UPDATE public.permission SET id = '00000000-0000-0004-0008-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000004'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0008-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000040'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0008-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000041'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0008-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000042'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0008-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000043'::uuid;
-- Domain permissions
UPDATE public.permission SET id = '00000000-0000-0004-0009-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000006'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0009-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000044'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0009-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000047'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0009-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000045'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0009-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000046'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0009-000000000006'::uuid WHERE id = '00000000-0000-0000-0004-000000000005'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0009-000000000007'::uuid WHERE id = '00000000-0000-0000-0004-000000000033'::uuid;
-- Twin Status permissions
UPDATE public.permission SET id = '00000000-0000-0004-0010-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000008'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0010-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000048'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0010-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000051'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0010-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000049'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0010-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000050'::uuid;
-- Twin permissions
UPDATE public.permission SET id = '00000000-0000-0004-0011-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000009'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0011-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000052'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0011-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000055'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0011-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000053'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0011-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000054'::uuid;
-- Comment permissions
UPDATE public.permission SET id = '00000000-0000-0004-0012-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000010'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0012-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000056'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0012-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000059'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0012-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000057'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0012-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000058'::uuid;
-- Attachment permissions
UPDATE public.permission SET id = '00000000-0000-0004-0013-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000011'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0013-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000060'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0013-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000063'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0013-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000061'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0013-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000062'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0013-000000000006'::uuid WHERE id = '00000000-0000-0000-0004-000000000168'::uuid;
-- User permissions
UPDATE public.permission SET id = '00000000-0000-0004-0014-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000012'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0014-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000064'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0014-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000067'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0014-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000065'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0014-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000066'::uuid;
-- User Group permissions
UPDATE public.permission SET id = '00000000-0000-0004-0015-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000013'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0015-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000068'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0015-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000071'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0015-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000069'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0015-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000070'::uuid;
-- Data List permissions
UPDATE public.permission SET id = '00000000-0000-0004-0016-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000014'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0016-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000072'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0016-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000075'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0016-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000073'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0016-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000074'::uuid;
-- Data List Option permissions
UPDATE public.permission SET id = '00000000-0000-0004-0017-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000015'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0017-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000076'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0017-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000079'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0017-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000077'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0017-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000078'::uuid;
-- Data List Subset permissions
UPDATE public.permission SET id = '00000000-0000-0004-0018-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000016'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0018-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000080'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0018-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000083'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0018-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000081'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0018-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000082'::uuid;
-- Permission permissions
UPDATE public.permission SET id = '00000000-0000-0004-0019-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000017'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0019-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000084'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0019-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000087'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0019-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000085'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0019-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000086'::uuid;
-- Permission Grant Assignee Propagation permissions
UPDATE public.permission SET id = '00000000-0000-0004-0020-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000181'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0020-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000088'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0020-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000091'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0020-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000089'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0020-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000090'::uuid;
-- Permission Grant Space Role permissions
UPDATE public.permission SET id = '00000000-0000-0004-0021-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000182'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0021-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000092'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0021-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000095'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0021-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000093'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0021-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000094'::uuid;
-- Permission Grant Twin Role permissions
UPDATE public.permission SET id = '00000000-0000-0004-0022-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000183'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0022-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000096'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0022-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000097'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0022-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000098'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0022-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000099'::uuid;
-- Permission Grant User permissions
UPDATE public.permission SET id = '00000000-0000-0004-0023-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000184'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0023-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000100'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0023-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000101'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0023-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000102'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0023-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000103'::uuid;
-- Permission Grant User Group permissions
UPDATE public.permission SET id = '00000000-0000-0004-0024-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000185'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0024-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000104'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0024-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000107'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0024-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000105'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0024-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000106'::uuid;
-- Permission Group permissions
UPDATE public.permission SET id = '00000000-0000-0004-0025-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000018'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0025-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000108'::uuid;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('b407a7b1-d16b-386a-83f5-cd0d9cffc555', 'Permission Group create name', null, 'permissionName'),
                                                          ('cf4d65eb-594d-3b17-b5d3-66ab79956756', 'Permission Group create description', null, 'permissionDescription'),
                                                          ('d507a7b1-d16b-386a-83f5-cd0d9cffc557', 'Permission Group update name', null, 'permissionName'),
                                                          ('df4d65eb-594d-3b17-b5d3-66ab79956758', 'Permission Group update description', null, 'permissionDescription'),
                                                          ('e607a7b1-d16b-386a-83f5-cd0d9cffc559', 'Permission Group delete name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956760', 'Permission Group delete description', null, 'permissionDescription')
ON CONFLICT (id) DO UPDATE SET name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('b407a7b1-d16b-386a-83f5-cd0d9cffc555', 'en', 'Permission Group create', 0),
                                                                                      ('cf4d65eb-594d-3b17-b5d3-66ab79956756', 'en', 'Permission Group create', 0),
                                                                                      ('d507a7b1-d16b-386a-83f5-cd0d9cffc557', 'en', 'Permission Group update', 0),
                                                                                      ('df4d65eb-594d-3b17-b5d3-66ab79956758', 'en', 'Permission Group update', 0),
                                                                                      ('e607a7b1-d16b-386a-83f5-cd0d9cffc559', 'en', 'Permission Group delete', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956760', 'en', 'Permission Group delete', 0)
ON CONFLICT (i18n_id, locale) DO UPDATE SET translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0025-000000000002', 'PERMISSION_GROUP_CREATE', '00000000-0000-0000-0005-000000000001', 'b407a7b1-d16b-386a-83f5-cd0d9cffc555', 'cf4d65eb-594d-3b17-b5d3-66ab79956756'),
                                                                                                    ('00000000-0000-0004-0025-000000000004', 'PERMISSION_GROUP_UPDATE', '00000000-0000-0000-0005-000000000001', 'd507a7b1-d16b-386a-83f5-cd0d9cffc557', 'df4d65eb-594d-3b17-b5d3-66ab79956758'),
                                                                                                    ('00000000-0000-0004-0025-000000000005', 'PERMISSION_GROUP_DELETE', '00000000-0000-0000-0005-000000000001', 'e607a7b1-d16b-386a-83f5-cd0d9cffc559', 'ef4d65eb-594d-3b17-b5d3-66ab79956760')
ON CONFLICT (id) DO UPDATE SET key = excluded.key;
-- Permission Schema permissions
UPDATE public.permission SET id = '00000000-0000-0004-0026-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000019'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0026-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000109'::uuid;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('8e44d042-3532-4ccf-b4e9-efad657f176e', 'Schema create name', null, 'permissionName'),
                                                          ('3578b14d-8e30-4b25-9e56-d7a5486d9a51', 'Schema create description', null, 'permissionDescription'),
                                                          ('4ea7f1d1-167d-43a5-8a25-5ca41138e09d', 'Schema update name', null, 'permissionName'),
                                                          ('7a7a93a6-7b1a-4f52-b6d6-9ec07a6766d4', 'Schema update description', null, 'permissionDescription'),
                                                          ('f53d63a9-4b8b-41a0-8a63-0b8afcfd17ca', 'Schema delete name', null, 'permissionName'),
                                                          ('c2c4b5e7-12a0-4f7b-97c1-3d03fbb0a65f', 'Schema delete description', null, 'permissionDescription')
ON CONFLICT (id) DO UPDATE SET name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('8e44d042-3532-4ccf-b4e9-efad657f176e', 'en', 'Schema create', 0),
                                                                                      ('3578b14d-8e30-4b25-9e56-d7a5486d9a51', 'en', 'Schema create', 0),
                                                                                      ('4ea7f1d1-167d-43a5-8a25-5ca41138e09d', 'en', 'Schema update', 0),
                                                                                      ('7a7a93a6-7b1a-4f52-b6d6-9ec07a6766d4', 'en', 'Schema update', 0),
                                                                                      ('f53d63a9-4b8b-41a0-8a63-0b8afcfd17ca', 'en', 'Schema delete', 0),
                                                                                      ('c2c4b5e7-12a0-4f7b-97c1-3d03fbb0a65f', 'en', 'Schema delete', 0)
ON CONFLICT (i18n_id, locale) DO UPDATE SET translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0026-000000000002', 'PERMISSION_SCHEMA_CREATE', '00000000-0000-0000-0005-000000000001', '8e44d042-3532-4ccf-b4e9-efad657f176e', '3578b14d-8e30-4b25-9e56-d7a5486d9a51'),
                                                                                                    ('00000000-0000-0004-0026-000000000004', 'PERMISSION_SCHEMA_UPDATE', '00000000-0000-0000-0005-000000000001', '4ea7f1d1-167d-43a5-8a25-5ca41138e09d', '7a7a93a6-7b1a-4f52-b6d6-9ec07a6766d4'),
                                                                                                    ('00000000-0000-0004-0026-000000000005', 'PERMISSION_SCHEMA_DELETE', '00000000-0000-0000-0005-000000000001', 'f53d63a9-4b8b-41a0-8a63-0b8afcfd17ca', 'c2c4b5e7-12a0-4f7b-97c1-3d03fbb0a65f')
ON CONFLICT (id) DO UPDATE SET key = excluded.key;
-- User Permission permissions
UPDATE public.permission SET id = '00000000-0000-0004-0027-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000110'::uuid;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('07e9a2b8-9f44-4379-bb23-6a687d5c70f4', 'User Permission manage name', null, 'permissionName'),
                                                          ('3db87e6a-10cb-4d1f-b1b6-5b69d41e3459', 'User Permission manage description', null, 'permissionDescription'),
                                                          ('f1b73647-b7d6-4d10-9d5b-086a4e8f4e68', 'User Permission create name', null, 'permissionName'),
                                                          ('7f48e22b-1205-4b44-8c48-9edb0a479c3d', 'User Permission create description', null, 'permissionDescription'),
                                                          ('82c8b983-f135-4a18-8a40-7a4b02442099', 'User Permission update name', null, 'permissionName'),
                                                          ('2906c5c0-49bc-4639-9ad6-004dbdf3d263', 'User Permission update description', null, 'permissionDescription'),
                                                          ('1f6c60f6-b1c0-4db1-9d9b-60b6f6716a0c', 'User Permission delete name', null, 'permissionName'),
                                                          ('1aa03e1e-274e-4b94-91d6-5701efb3d3e1', 'User Permission delete description', null, 'permissionDescription')
ON CONFLICT (id) DO UPDATE SET name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('07e9a2b8-9f44-4379-bb23-6a687d5c70f4', 'en', 'User Permission manage', 0),
                                                                                      ('3db87e6a-10cb-4d1f-b1b6-5b69d41e3459', 'en', 'User Permission manage', 0),
                                                                                      ('f1b73647-b7d6-4d10-9d5b-086a4e8f4e68', 'en', 'User Permission create', 0),
                                                                                      ('7f48e22b-1205-4b44-8c48-9edb0a479c3d', 'en', 'User Permission create', 0),
                                                                                      ('82c8b983-f135-4a18-8a40-7a4b02442099', 'en', 'User Permission update', 0),
                                                                                      ('2906c5c0-49bc-4639-9ad6-004dbdf3d263', 'en', 'User Permission update', 0),
                                                                                      ('1f6c60f6-b1c0-4db1-9d9b-60b6f6716a0c', 'en', 'User Permission delete', 0),
                                                                                      ('1aa03e1e-274e-4b94-91d6-5701efb3d3e1', 'en', 'User Permission delete', 0)
ON CONFLICT (i18n_id, locale) DO UPDATE SET translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0027-000000000001', 'USER_PERMISSION_MANAGE', '00000000-0000-0000-0005-000000000001', '07e9a2b8-9f44-4379-bb23-6a687d5c70f4', '3db87e6a-10cb-4d1f-b1b6-5b69d41e3459'),
                                                                                                    ('00000000-0000-0004-0027-000000000002', 'USER_PERMISSION_CREATE', '00000000-0000-0000-0005-000000000001', 'f1b73647-b7d6-4d10-9d5b-086a4e8f4e68', '7f48e22b-1205-4b44-8c48-9edb0a479c3d'),
                                                                                                    ('00000000-0000-0004-0027-000000000004', 'USER_PERMISSION_UPDATE', '00000000-0000-0000-0005-000000000001', '82c8b983-f135-4a18-8a40-7a4b02442099', '2906c5c0-49bc-4639-9ad6-004dbdf3d263'),
                                                                                                    ('00000000-0000-0004-0027-000000000005', 'USER_PERMISSION_DELETE', '00000000-0000-0000-0005-000000000001', '1f6c60f6-b1c0-4db1-9d9b-60b6f6716a0c', '1aa03e1e-274e-4b94-91d6-5701efb3d3e1')
ON CONFLICT (id) DO UPDATE SET key = excluded.key;
-- I18N permissions
UPDATE public.permission SET id = '00000000-0000-0004-0028-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000112'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0028-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000113'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0028-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000116'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0028-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000114'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0028-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000115'::uuid;
-- ERASER permissions
UPDATE public.permission SET id = '00000000-0000-0004-0029-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000026'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0029-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000117'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0029-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000120'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0029-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000118'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0029-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000119'::uuid;
-- FACTORY permissions
UPDATE public.permission SET id = '00000000-0000-0004-0030-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000020'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0030-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000121'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0030-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000124'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0030-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000122'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0030-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000123'::uuid;
-- MULTIPLIER permissions
UPDATE public.permission SET id = '00000000-0000-0004-0031-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000021'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0031-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000125'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0031-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000128'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0031-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000126'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0031-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000127'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0031-000000000006'::uuid WHERE id = '00000000-0000-0000-0004-000000000022'::uuid;
-- PIPELINE permissions
UPDATE public.permission SET id = '00000000-0000-0004-0032-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000023'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0032-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000129'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0032-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000132'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0032-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000130'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0032-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000131'::uuid;
-- CONDITION_SET permissions
UPDATE public.permission SET id = '00000000-0000-0004-0033-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000027'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0033-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000133'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0033-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000136'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0033-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000134'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0033-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000135'::uuid;
-- BRANCH permissions
UPDATE public.permission SET id = '00000000-0000-0004-0034-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000025'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0034-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000137'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0034-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000140'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0034-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000138'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0034-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000139'::uuid;
-- DRAFT permissions
UPDATE public.permission SET id = '00000000-0000-0004-0035-000000000006'::uuid WHERE id = '00000000-0000-0000-0004-000000000141'::uuid;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('e29fb86a-1780-4751-9799-0ec72bc6c923', 'Draft manage name', null, 'permissionName'),
                                                          ('e7f363b9-2f3e-453c-8fd9-dc29c0aa683c', 'Draft manage description', null, 'permissionDescription'),
                                                          ('7856222c-7e24-4f7d-9962-7ae67a7b05e1', 'Draft create name', null, 'permissionName'),
                                                          ('c3d0bbf2-eb05-4515-ab45-2ecf5e21c993', 'Draft create description', null, 'permissionDescription'),
                                                          ('185565a6-9727-4d08-b26d-eff24bf3b248', 'Draft create name', null, 'permissionName'),
                                                          ('54cf7275-615e-4608-8e63-d48c175d43a9', 'Draft create description', null, 'permissionDescription'),
                                                          ('66d51f57-78f7-4eca-aa01-fabcc048e327', 'Draft update name', null, 'permissionName'),
                                                          ('12d871d2-5c23-4b23-a139-a85e3e798f1f', 'Draft update description', null, 'permissionDescription'),
                                                          ('f01254bf-89a2-4e52-9e85-1205aa69bd29', 'Draft delete name', null, 'permissionName'),
                                                          ('c6fb326f-d2a5-4021-b22d-29cf1fa45204', 'Draft delete description', null, 'permissionDescription')
ON CONFLICT (id) DO UPDATE SET name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                          ('e29fb86a-1780-4751-9799-0ec72bc6c923', 'en', 'Draft manage', 0),
                                                          ('e7f363b9-2f3e-453c-8fd9-dc29c0aa683c', 'en', 'Draft manage', 0),
                                                          ('7856222c-7e24-4f7d-9962-7ae67a7b05e1', 'en', 'Draft create', 0),
                                                          ('c3d0bbf2-eb05-4515-ab45-2ecf5e21c993', 'en', 'Draft create', 0),
                                                          ('185565a6-9727-4d08-b26d-eff24bf3b248', 'en', 'Draft view', 0),
                                                          ('54cf7275-615e-4608-8e63-d48c175d43a9', 'en', 'Draft view', 0),
                                                          ('66d51f57-78f7-4eca-aa01-fabcc048e327', 'en', 'Draft update', 0),
                                                          ('12d871d2-5c23-4b23-a139-a85e3e798f1f', 'en', 'Draft update', 0),
                                                          ('f01254bf-89a2-4e52-9e85-1205aa69bd29', 'en', 'Draft delete', 0),
                                                          ('c6fb326f-d2a5-4021-b22d-29cf1fa45204', 'en', 'Draft delete', 0)
ON CONFLICT (i18n_id, locale) DO UPDATE SET translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0035-000000000001', 'DRAFT_MANAGE', '00000000-0000-0000-0005-000000000001', 'e29fb86a-1780-4751-9799-0ec72bc6c923', 'e7f363b9-2f3e-453c-8fd9-dc29c0aa683c'),
                                                                                                    ('00000000-0000-0004-0035-000000000002', 'DRAFT_CREATE', '00000000-0000-0000-0005-000000000001', '7856222c-7e24-4f7d-9962-7ae67a7b05e1', 'c3d0bbf2-eb05-4515-ab45-2ecf5e21c993'),
                                                                                                    ('00000000-0000-0004-0035-000000000003', 'DRAFT_VIEW', '00000000-0000-0000-0005-000000000001', '185565a6-9727-4d08-b26d-eff24bf3b248', '54cf7275-615e-4608-8e63-d48c175d43a9'),
                                                                                                    ('00000000-0000-0004-0035-000000000004', 'DRAFT_UPDATE', '00000000-0000-0000-0005-000000000001', '66d51f57-78f7-4eca-aa01-fabcc048e327', '12d871d2-5c23-4b23-a139-a85e3e798f1f'),
                                                                                                    ('00000000-0000-0004-0035-000000000005', 'DRAFT_DELETE', '00000000-0000-0000-0005-000000000001', 'f01254bf-89a2-4e52-9e85-1205aa69bd29', 'c6fb326f-d2a5-4021-b22d-29cf1fa45204')
ON CONFLICT (id) DO UPDATE SET key = excluded.key;
-- Domain Business Account permissions
UPDATE public.permission SET id = '00000000-0000-0004-0036-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000177'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0036-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000142'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0036-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000145'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0036-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000143'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0036-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000144'::uuid;
-- Domain USER permissions
UPDATE public.permission SET id = '00000000-0000-0004-0037-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000179'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0037-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000146'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0037-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000149'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0037-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000147'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0037-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000148'::uuid;
-- Business Account permissions
UPDATE public.permission SET id = '00000000-0000-0004-0038-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000030'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0038-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000150'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0038-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000153'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0038-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000151'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0038-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000152'::uuid;
-- Permission Grant Space Role permissions
UPDATE public.permission SET id = '00000000-0000-0004-0039-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000154'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0039-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000155'::uuid;
insert into public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc039', 'Space role create name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956739', 'Space role create description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc040', 'Space role update name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956740', 'Space role update description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc041', 'Space role delete name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956741', 'Space role delete description', null, 'permissionDescription')
on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
insert into public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc039', 'en', 'Space role create', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956739', 'en', 'Space role create', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc040', 'en', 'Space role update', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956740', 'en', 'Space role update', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc041', 'en', 'Space role delete', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956741', 'en', 'Space role delete', 0)
on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0039-000000000002', 'SPACE_ROLE_CREATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc039', 'ef4d65eb-594d-3b17-b5d3-66ab79956739'),
                                                                                                    ('00000000-0000-0004-0039-000000000004', 'SPACE_ROLE_UPDATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc040', 'ef4d65eb-594d-3b17-b5d3-66ab79956740'),
                                                                                                    ('00000000-0000-0004-0039-000000000005', 'SPACE_ROLE_DELETE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc041', 'ef4d65eb-594d-3b17-b5d3-66ab79956741')
on conflict (id) do update set key=excluded.key;
-- FEATURER permissions
UPDATE public.permission SET id = '00000000-0000-0004-0040-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000032'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0040-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000158'::uuid;
insert into public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc042', 'Featurer create name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956742', 'Featurer create description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc043', 'Featurer update name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956743', 'Featurer update description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc044', 'Featurer delete name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956744', 'Featurer delete description', null, 'permissionDescription')
on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
insert into public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc042', 'en', 'Featurer create', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956742', 'en', 'Featurer create', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc043', 'en', 'Featurer update', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956743', 'en', 'Featurer update', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc044', 'en', 'Featurer delete', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956744', 'en', 'Featurer delete', 0)
on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0040-000000000002', 'FEATURER_CREATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc042', 'ef4d65eb-594d-3b17-b5d3-66ab79956742'),
                                                                                                    ('00000000-0000-0004-0040-000000000004', 'FEATURER_UPDATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc043', 'ef4d65eb-594d-3b17-b5d3-66ab79956743'),
                                                                                                    ('00000000-0000-0004-0040-000000000005', 'FEATURER_DELETE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc044', 'ef4d65eb-594d-3b17-b5d3-66ab79956744')
on conflict (id) do update set key=excluded.key;
-- Tier permissions
UPDATE public.permission SET id = '00000000-0000-0004-0041-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000031'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0041-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000159'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0041-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000162'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0041-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000160'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0041-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000161'::uuid;
-- Face permissions
UPDATE public.permission SET id = '00000000-0000-0004-0042-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000180'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0042-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000163'::uuid;
insert into public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc045', 'Face create name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956745', 'Face create description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc046', 'Face update name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956746', 'Face update description', null, 'permissionDescription'),
                                                          ('a207a7b1-d16b-386a-83f5-cd0d9cffc047', 'Face delete name', null, 'permissionName'),
                                                          ('ef4d65eb-594d-3b17-b5d3-66ab79956747', 'Face delete description', null, 'permissionDescription')
on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
insert into public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc045', 'en', 'Face create', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956745', 'en', 'Face create', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc046', 'en', 'Face update', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956746', 'en', 'Face update', 0),
                                                                                      ('a207a7b1-d16b-386a-83f5-cd0d9cffc047', 'en', 'Face delete', 0),
                                                                                      ('ef4d65eb-594d-3b17-b5d3-66ab79956747', 'en', 'Face delete', 0)
on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0042-000000000002', 'FACE_CREATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc045', 'ef4d65eb-594d-3b17-b5d3-66ab79956745'),
                                                                                                    ('00000000-0000-0004-0042-000000000004', 'FACE_UPDATE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc046', 'ef4d65eb-594d-3b17-b5d3-66ab79956746'),
                                                                                                    ('00000000-0000-0004-0042-000000000005', 'FACE_DELETE', '00000000-0000-0000-0005-000000000001', 'a207a7b1-d16b-386a-83f5-cd0d9cffc047', 'ef4d65eb-594d-3b17-b5d3-66ab79956747')
on conflict (id) do update set key=excluded.key;
-- PIPELINE STEP permissions
UPDATE public.permission SET id = '00000000-0000-0004-0043-000000000001'::uuid WHERE id = '00000000-0000-0000-0004-000000000024'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0043-000000000002'::uuid WHERE id = '00000000-0000-0000-0004-000000000164'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0043-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000167'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0043-000000000004'::uuid WHERE id = '00000000-0000-0000-0004-000000000165'::uuid;
UPDATE public.permission SET id = '00000000-0000-0004-0043-000000000005'::uuid WHERE id = '00000000-0000-0000-0004-000000000166'::uuid;
-- History permissions
UPDATE public.permission SET id = '00000000-0000-0004-0044-000000000003'::uuid WHERE id = '00000000-0000-0000-0004-000000000111'::uuid;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
                                                          ('dbce948e-47a3-4e5b-a6ec-8c6e2a06f6f0', 'History manage name', null, 'permissionName'),
                                                          ('e9638f97-9dbf-4c07-a871-f5c57df87f69', 'History manage description', null, 'permissionDescription'),
                                                          ('2e9a1c12-4a3b-4e02-9c28-21f241377d33', 'History create name', null, 'permissionName'),
                                                          ('11d9c5b4-153e-4e57-99e6-35d5401c45a3', 'History create description', null, 'permissionDescription'),
                                                          ('d1a1799c-7984-4889-b3e7-34803f3c7ca0', 'History update name', null, 'permissionName'),
                                                          ('fbd8d3db-9c85-4dbd-80ea-44fa3e4e1ee7', 'History update description', null, 'permissionDescription'),
                                                          ('0b3c53e9-3a3e-48b6-81de-cfcd3ccf0f23', 'History delete name', null, 'permissionName'),
                                                          ('6a785344-0f28-4f5c-8ee8-eaf646efb6d9', 'History delete description', null, 'permissionDescription')
ON CONFLICT (id) DO UPDATE SET name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('dbce948e-47a3-4e5b-a6ec-8c6e2a06f6f0', 'en', 'History manage', 0),
                                                                                      ('e9638f97-9dbf-4c07-a871-f5c57df87f69', 'en', 'History manage', 0),
                                                                                      ('2e9a1c12-4a3b-4e02-9c28-21f241377d33', 'en', 'History create', 0),
                                                                                      ('11d9c5b4-153e-4e57-99e6-35d5401c45a3', 'en', 'History create', 0),
                                                                                      ('d1a1799c-7984-4889-b3e7-34803f3c7ca0', 'en', 'History update', 0),
                                                                                      ('fbd8d3db-9c85-4dbd-80ea-44fa3e4e1ee7', 'en', 'History update', 0),
                                                                                      ('0b3c53e9-3a3e-48b6-81de-cfcd3ccf0f23', 'en', 'History delete', 0),
                                                                                      ('6a785344-0f28-4f5c-8ee8-eaf646efb6d9', 'en', 'History delete', 0)
ON CONFLICT (i18n_id, locale) DO UPDATE SET translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                                                                                                    ('00000000-0000-0004-0044-000000000001', 'HISTORY_MANAGE', '00000000-0000-0000-0005-000000000001', 'dbce948e-47a3-4e5b-a6ec-8c6e2a06f6f0', 'e9638f97-9dbf-4c07-a871-f5c57df87f69'),
                                                                                                    ('00000000-0000-0004-0044-000000000002', 'HISTORY_CREATE', '00000000-0000-0000-0005-000000000001', '2e9a1c12-4a3b-4e02-9c28-21f241377d33', '11d9c5b4-153e-4e57-99e6-35d5401c45a3'),
                                                                                                    ('00000000-0000-0004-0044-000000000003', 'HISTORY_VIEW', '00000000-0000-0000-0005-000000000001', 'b18a2c65-b9d6-4e4d-8c5a-f1e0688a11bc', '96f06362-94ad-4c9f-b476-53e65a9470bc'),
                                                                                                    ('00000000-0000-0004-0044-000000000004', 'HISTORY_UPDATE', '00000000-0000-0000-0005-000000000001', 'd1a1799c-7984-4889-b3e7-34803f3c7ca0', 'fbd8d3db-9c85-4dbd-80ea-44fa3e4e1ee7'),
                                                                                                    ('00000000-0000-0004-0044-000000000005', 'HISTORY_DELETE', '00000000-0000-0000-0005-000000000001', '0b3c53e9-3a3e-48b6-81de-cfcd3ccf0f23', '6a785344-0f28-4f5c-8ee8-eaf646efb6d9')
ON CONFLICT (id) DO UPDATE SET key = excluded.key;
