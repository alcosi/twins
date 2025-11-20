INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('2a4a8b59-3b71-4a9d-9e5e-fc2f1e2b9a11', '', null, 'permissionName', null),
    ('b6c2f17e-d4a3-42b2-9d2c-3f2e91a91a22', '', null, 'permissionDescription', null),
    ('ce5f937a-3181-4e57-b1a4-847b9ce2a133', '', null, 'permissionName', null),
    ('f12d7f6b-82d5-4c64-9f35-bb1b8a3b9a44', '', null, 'permissionDescription', null),
    ('0c71d8d9-9e2a-48e0-8b4c-3f559a4b1a55', '', null, 'permissionName', null),
    ('4f38a1a4-8a64-48e2-9d8c-5f7a8a3b9a66', '', null, 'permissionDescription', null),
    ('ac9e3f4d-5a61-4e83-96ad-1f2b7e4f9a77', '', null, 'permissionName', null),
    ('b7d9a3e1-24f6-4c7a-9bdf-93a4a7c6b788', '', null, 'permissionDescription', null),
    ('d8a3e7f4-1c8f-4b6e-987a-0b1f9d4e8a99', '', null, 'permissionName', null),
    ('e9f4a6b1-7d5c-4c8a-bf3d-7a9c1e8b9b00', '', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation(i18n_id, locale, translation, usage_counter)
VALUES
    ('2a4a8b59-3b71-4a9d-9e5e-fc2f1e2b9a11', 'en', 'Twinflow factory manage permission', 0),
    ('b6c2f17e-d4a3-42b2-9d2c-3f2e91a91a22', 'en', 'twinflow factory manage permission', 0),
    ('ce5f937a-3181-4e57-b1a4-847b9ce2a133', 'en', 'Twinflow factory create permission', 0),
    ('f12d7f6b-82d5-4c64-9f35-bb1b8a3b9a44', 'en', 'twinflow factory create permission', 0),
    ('0c71d8d9-9e2a-48e0-8b4c-3f559a4b1a55', 'en', 'Twinflow factory view permission', 0),
    ('4f38a1a4-8a64-48e2-9d8c-5f7a8a3b9a66', 'en', 'twinflow factory view permission', 0),
    ('ac9e3f4d-5a61-4e83-96ad-1f2b7e4f9a77', 'en', 'Twinflow factory update permission', 0),
    ('b7d9a3e1-24f6-4c7a-9bdf-93a4a7c6b788', 'en', 'twinflow factory update permission', 0),
    ('d8a3e7f4-1c8f-4b6e-987a-0b1f9d4e8a99', 'en', 'Twinflow factory delete permission', 0),
    ('e9f4a6b1-7d5c-4c8a-bf3d-7a9c1e8b9b00', 'en', 'twinflow factory delete permission', 0)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0048-000000000001', 'TWINFLOW_FACTORY_MANAGE', '00000000-0000-0000-0005-000000000001', '2a4a8b59-3b71-4a9d-9e5e-fc2f1e2b9a11', 'b6c2f17e-d4a3-42b2-9d2c-3f2e91a91a22'),
    ('00000000-0000-0004-0048-000000000002', 'TWINFLOW_FACTORY_CREATE', '00000000-0000-0000-0005-000000000001', 'ce5f937a-3181-4e57-b1a4-847b9ce2a133', 'f12d7f6b-82d5-4c64-9f35-bb1b8a3b9a44'),
    ('00000000-0000-0004-0048-000000000003', 'TWINFLOW_FACTORY_VIEW', '00000000-0000-0000-0005-000000000001', '0c71d8d9-9e2a-48e0-8b4c-3f559a4b1a55', '4f38a1a4-8a64-48e2-9d8c-5f7a8a3b9a66'),
    ('00000000-0000-0004-0048-000000000004', 'TWINFLOW_FACTORY_UPDATE', '00000000-0000-0000-0005-000000000001', 'ac9e3f4d-5a61-4e83-96ad-1f2b7e4f9a77', 'b7d9a3e1-24f6-4c7a-9bdf-93a4a7c6b788'),
    ('00000000-0000-0004-0048-000000000005', 'TWINFLOW_FACTORY_DELETE', '00000000-0000-0000-0005-000000000001', 'd8a3e7f4-1c8f-4b6e-987a-0b1f9d4e8a99', 'e9f4a6b1-7d5c-4c8a-bf3d-7a9c1e8b9b00')
ON CONFLICT DO NOTHING;
