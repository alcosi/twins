INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('9a00b530-f140-434b-b5d5-6f7e94cf3fe2', '', null, 'permissionName', null),
    ('421a0c0b-2b03-4b04-9043-4b716c840e21', '', null, 'permissionDescription', null),
    ('a8dad29f-a03d-4340-ad7d-01be04d95e2e', '', null, 'permissionName', null),
    ('bf020189-183a-40b6-a362-eb0524a9782b', '', null, 'permissionDescription', null),
    ('28b1a0cd-7262-4c86-b78f-bbc448167d4b', '', null, 'permissionName', null),
    ('3ff4cf38-9c3c-4635-9aef-92b65b39265b', '', null, 'permissionDescription', null),
    ('a55ac638-1423-4cdc-b791-724cd5e5669f', '', null, 'permissionName', null),
    ('deeda670-7aa1-4357-80c9-bdbc8df67eef', '', null, 'permissionDescription', null),
    ('f0341c47-4f5c-4b3e-b7cc-99b2b57e167e', '', null, 'permissionName', null),
    ('e2591b48-7335-40b5-a378-9b6893c2d408', '', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation(i18n_id, locale, translation, usage_counter)
VALUES
    ('9a00b530-f140-434b-b5d5-6f7e94cf3fe2', 'en', 'Twin class dynamic marker manage permission', 0),
    ('421a0c0b-2b03-4b04-9043-4b716c840e21', 'en', 'Twin class dynamic marker manage permission', 0),
    ('a8dad29f-a03d-4340-ad7d-01be04d95e2e', 'en', 'Twin class dynamic marker create permission', 0),
    ('bf020189-183a-40b6-a362-eb0524a9782b', 'en', 'Twin class dynamic marker create permission', 0),
    ('28b1a0cd-7262-4c86-b78f-bbc448167d4b', 'en', 'Twin class dynamic marker view permission', 0),
    ('3ff4cf38-9c3c-4635-9aef-92b65b39265b', 'en', 'Twin class dynamic marker view permission', 0),
    ('a55ac638-1423-4cdc-b791-724cd5e5669f', 'en', 'Twin class dynamic marker update permission', 0),
    ('deeda670-7aa1-4357-80c9-bdbc8df67eef', 'en', 'Twin class dynamic marker update permission', 0),
    ('f0341c47-4f5c-4b3e-b7cc-99b2b57e167e', 'en', 'Twin class dynamic marker delete permission', 0),
    ('e2591b48-7335-40b5-a378-9b6893c2d408', 'en', 'Twin class dynamic marker delete permission', 0)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0052-000000000001', 'TWIN_CLASS_DYNAMIC_MARKER_MANAGE', '00000000-0000-0000-0005-000000000001', '9a00b530-f140-434b-b5d5-6f7e94cf3fe2', '421a0c0b-2b03-4b04-9043-4b716c840e21'),
    ('00000000-0000-0004-0052-000000000002', 'TWIN_CLASS_DYNAMIC_MARKER_CREATE', '00000000-0000-0000-0005-000000000001', 'a8dad29f-a03d-4340-ad7d-01be04d95e2e', 'bf020189-183a-40b6-a362-eb0524a9782b'),
    ('00000000-0000-0004-0052-000000000003', 'TWIN_CLASS_DYNAMIC_MARKER_VIEW', '00000000-0000-0000-0005-000000000001', '28b1a0cd-7262-4c86-b78f-bbc448167d4b', '3ff4cf38-9c3c-4635-9aef-92b65b39265b'),
    ('00000000-0000-0004-0052-000000000004', 'TWIN_CLASS_DYNAMIC_MARKER_UPDATE', '00000000-0000-0000-0005-000000000001', 'a55ac638-1423-4cdc-b791-724cd5e5669f', 'deeda670-7aa1-4357-80c9-bdbc8df67eef'),
    ('00000000-0000-0004-0052-000000000005', 'TWIN_CLASS_DYNAMIC_MARKER_DELETE', '00000000-0000-0000-0005-000000000001', 'f0341c47-4f5c-4b3e-b7cc-99b2b57e167e', 'e2591b48-7335-40b5-a378-9b6893c2d408')
ON CONFLICT DO NOTHING;



