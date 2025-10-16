INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('a832f4d1-7c3a-4e95-bc62-91d8f3a2b7e1', '', null, 'permissionName', null),
    ('c5e9d2b8-1f47-4a63-9d0a-8b3c6e5f4a72', '', null, 'permissionDescription', null),
    ('e7a1c4f9-3b86-4d52-90e5-2c4a7b6d8f31', '', null, 'permissionName', null),
    ('f2b8d5c9-4e73-4a61-8c0d-9a3b6e7f5c24', '', null, 'permissionDescription', null),
    ('d3a6e8f1-5c92-4b74-9a0e-8d2c7b5f4e39', '', null, 'permissionName', null),
    ('b9c7e4d2-6a81-4f53-90b8-3c5d2a7e6f14', '', null, 'permissionDescription', null),
    ('e4f8c2a7-9d53-4b61-80c6-5a3b9e7d2f18', '', null, 'permissionName', null),
    ('c6a9d3e8-7b24-4f51-90a5-2d8c6b4e7f19', '', null, 'permissionDescription', null),
    ('f7e2c5a9-8d34-4b61-90c7-3a5b9e6d4f28', '', null, 'permissionName', null),
    ('d8b6e4c9-7a23-4f51-90d5-2c9a8b6e4f37', '', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation(i18n_id, locale, translation, usage_counter)
VALUES
    ('a832f4d1-7c3a-4e95-bc62-91d8f3a2b7e1', 'en', 'Twinflow factory manage permission', 0),
    ('c5e9d2b8-1f47-4a63-9d0a-8b3c6e5f4a72', 'en', 'twinflow factory manage permission', 0),
    ('e7a1c4f9-3b86-4d52-90e5-2c4a7b6d8f31', 'en', 'Twinflow factory create permission', 0),
    ('f2b8d5c9-4e73-4a61-8c0d-9a3b6e7f5c24', 'en', 'twinflow factory create permission', 0),
    ('d3a6e8f1-5c92-4b74-9a0e-8d2c7b5f4e39', 'en', 'Twinflow factory view permission', 0),
    ('b9c7e4d2-6a81-4f53-90b8-3c5d2a7e6f14', 'en', 'twinflow factory view permission', 0),
    ('e4f8c2a7-9d53-4b61-80c6-5a3b9e7d2f18', 'en', 'Twinflow factory update permission', 0),
    ('c6a9d3e8-7b24-4f51-90a5-2d8c6b4e7f19', 'en', 'twinflow factory update permission', 0),
    ('f7e2c5a9-8d34-4b61-90c7-3a5b9e6d4f28', 'en', 'Twinflow factory delete permission', 0),
    ('d8b6e4c9-7a23-4f51-90d5-2c9a8b6e4f37', 'en', 'twinflow factory delete permission', 0)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0048-000000000001', 'DOMAIN_SUBSCRIPTION_EVENT_MANAGE', '00000000-0000-0000-0005-000000000001', 'a832f4d1-7c3a-4e95-bc62-91d8f3a2b7e1', 'c5e9d2b8-1f47-4a63-9d0a-8b3c6e5f4a72'),
    ('00000000-0000-0004-0048-000000000002', 'DOMAIN_SUBSCRIPTION_EVENT_CREATE', '00000000-0000-0000-0005-000000000001', 'e7a1c4f9-3b86-4d52-90e5-2c4a7b6d8f31', 'f2b8d5c9-4e73-4a61-8c0d-9a3b6e7f5c24'),
    ('00000000-0000-0004-0048-000000000003', 'DOMAIN_SUBSCRIPTION_EVENT_VIEW', '00000000-0000-0000-0005-000000000001', 'd3a6e8f1-5c92-4b74-9a0e-8d2c7b5f4e39', 'b9c7e4d2-6a81-4f53-90b8-3c5d2a7e6f14'),
    ('00000000-0000-0004-0048-000000000004', 'DOMAIN_SUBSCRIPTION_EVENT_UPDATE', '00000000-0000-0000-0005-000000000001', 'e4f8c2a7-9d53-4b61-80c6-5a3b9e7d2f18', 'c6a9d3e8-7b24-4f51-90a5-2d8c6b4e7f19'),
    ('00000000-0000-0004-0048-000000000005', 'DOMAIN_SUBSCRIPTION_EVENT_DELETE', '00000000-0000-0000-0005-000000000001', 'f7e2c5a9-8d34-4b61-90c7-3a5b9e6d4f28', 'd8b6e4c9-7a23-4f51-90d5-2c9a8b6e4f37')
    ON CONFLICT DO NOTHING;