INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('d7c3a9b1-8e2f-4d14-9c7a-3f1a5b8c2d01', '', null, 'permissionName', null),
    ('a2b4c6d8-e0f2-4a1c-9b3d-5f7e9a1c3b02', '', null, 'permissionDescription', null),
    ('f5e7d9c1-b3a5-4c8e-9d2f-6a1b4c7e8d03', '', null, 'permissionName', null),
    ('c8a9b7d5-e3f1-4b2c-9a6d-7f5e3b1c4d04', '', null, 'permissionDescription', null),
    ('e1d3f5a7-b9c2-4d6e-8a4f-3b5c7d9e1f05', '', null, 'permissionName', null),
    ('b3d5f7a9-c1e4-4f2b-8d6c-5a9b3d7f1e06', '', null, 'permissionDescription', null),
    ('d9a1b3c5-e7f9-4d2b-8c6a-4f3b5d7e1f07', '', null, 'permissionName', null),
    ('a7c9e1d3-b5f7-4a2d-8e6c-3f9b1d5c7e08', '', null, 'permissionDescription', null),
    ('f1a3c5e7-d9b2-4f8d-ae6c-5b7d3f1a9e09', '', null, 'permissionName', null),
    ('c3e5a7b9-d1f2-4e8d-ab6c-7f5b9d1a3e10', '', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation(i18n_id, locale, translation, usage_counter)
VALUES
    ('2a4a8b59-3b71-4a9d-9e5e-fc2f1e2b9a11', 'en', 'Scheduler manage permission', 0),
    ('b6c2f17e-d4a3-42b2-9d2c-3f2e91a91a22', 'en', 'scheduler manage permission', 0),
    ('ce5f937a-3181-4e57-b1a4-847b9ce2a133', 'en', 'Scheduler create permission', 0),
    ('f12d7f6b-82d5-4c64-9f35-bb1b8a3b9a44', 'en', 'scheduler create permission', 0),
    ('0c71d8d9-9e2a-48e0-8b4c-3f559a4b1a55', 'en', 'Scheduler view permission', 0),
    ('4f38a1a4-8a64-48e2-9d8c-5f7a8a3b9a66', 'en', 'scheduler view permission', 0),
    ('ac9e3f4d-5a61-4e83-96ad-1f2b7e4f9a77', 'en', 'Scheduler update permission', 0),
    ('b7d9a3e1-24f6-4c7a-9bdf-93a4a7c6b788', 'en', 'scheduler update permission', 0),
    ('d8a3e7f4-1c8f-4b6e-987a-0b1f9d4e8a99', 'en', 'Scheduler delete permission', 0),
    ('e9f4a6b1-7d5c-4c8a-bf3d-7a9c1e8b9b00', 'en', 'scheduler delete permission', 0)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0051-000000000001', 'SCHEDULER_MANAGE', '00000000-0000-0000-0005-000000000001', 'd7c3a9b1-8e2f-4d14-9c7a-3f1a5b8c2d01', 'a2b4c6d8-e0f2-4a1c-9b3d-5f7e9a1c3b02'),
    ('00000000-0000-0004-0051-000000000002', 'SCHEDULER_CREATE', '00000000-0000-0000-0005-000000000001', 'f5e7d9c1-b3a5-4c8e-9d2f-6a1b4c7e8d03', 'c8a9b7d5-e3f1-4b2c-9a6d-7f5e3b1c4d04'),
    ('00000000-0000-0004-0051-000000000003', 'SCHEDULER_VIEW', '00000000-0000-0000-0005-000000000001', 'e1d3f5a7-b9c2-4d6e-8a4f-3b5c7d9e1f05', 'b3d5f7a9-c1e4-4f2b-8d6c-5a9b3d7f1e06'),
    ('00000000-0000-0004-0051-000000000004', 'SCHEDULER_UPDATE', '00000000-0000-0000-0005-000000000001', 'd9a1b3c5-e7f9-4d2b-8c6a-4f3b5d7e1f07', 'a7c9e1d3-b5f7-4a2d-8e6c-3f9b1d5c7e08'),
    ('00000000-0000-0004-0051-000000000005', 'SCHEDULER_DELETE', '00000000-0000-0000-0005-000000000001', 'f1a3c5e7-d9b2-4f8d-ae6c-5b7d3f1a9e09', 'c3e5a7b9-d1f2-4e8d-ab6c-7f5b9d1a3e10')
ON CONFLICT DO NOTHING;
