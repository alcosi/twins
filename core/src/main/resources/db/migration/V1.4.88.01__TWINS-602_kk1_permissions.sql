INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('d198a0eb-ec3c-43eb-987d-f6ae918c1d7d', '', null, 'permissionName', null),
    ('577a38d7-94a5-4a7a-a9c5-4259580faa2b', '', null, 'permissionDescription', null),
    ('3c58f48a-a9f2-4061-b738-e202b90ea614', '', null, 'permissionName', null),
    ('6d0b810a-d658-4a12-8dea-b894576b9df7', '', null, 'permissionDescription', null),
    ('38804f99-b259-4af7-a59e-bb9d04bf5e7a', '', null, 'permissionName', null),
    ('d6ef1d92-38b5-42ac-8291-ca8e54a2c1e0', '', null, 'permissionDescription', null),
    ('18c0ff98-2526-474f-bac5-fe525233417e', '', null, 'permissionName', null),
    ('de745075-5151-44b1-aeb6-b611982579b7', '', null, 'permissionDescription', null),
    ('b3ded4dd-262e-4836-b61d-1a50c34348b9', '', null, 'permissionName', null),
    ('07a8d112-dc78-40fe-b872-a0335dbddb2f', '', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation(i18n_id, locale, translation, usage_counter)
VALUES
    ('d198a0eb-ec3c-43eb-987d-f6ae918c1d7d', 'en', 'Twin class freeze manage permission', 0),
    ('577a38d7-94a5-4a7a-a9c5-4259580faa2b', 'en', 'Twin class freeze manage permission', 0),
    ('3c58f48a-a9f2-4061-b738-e202b90ea614', 'en', 'Twin class freeze create permission', 0),
    ('6d0b810a-d658-4a12-8dea-b894576b9df7', 'en', 'Twin class freeze create permission', 0),
    ('38804f99-b259-4af7-a59e-bb9d04bf5e7a', 'en', 'Twin class freeze view permission', 0),
    ('d6ef1d92-38b5-42ac-8291-ca8e54a2c1e0', 'en', 'Twin class freeze view permission', 0),
    ('18c0ff98-2526-474f-bac5-fe525233417e', 'en', 'Twin class freeze update permission', 0),
    ('de745075-5151-44b1-aeb6-b611982579b7', 'en', 'Twin class freeze update permission', 0),
    ('b3ded4dd-262e-4836-b61d-1a50c34348b9', 'en', 'Twin class freeze delete permission', 0),
    ('07a8d112-dc78-40fe-b872-a0335dbddb2f', 'en', 'Twin class freeze delete permission', 0)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0049-000000000001', 'TWIN_CLASS_FREEZE_MANAGE', '00000000-0000-0000-0005-000000000001', 'd198a0eb-ec3c-43eb-987d-f6ae918c1d7d', '577a38d7-94a5-4a7a-a9c5-4259580faa2b'),
    ('00000000-0000-0004-0049-000000000002', 'TWIN_CLASS_FREEZE_CREATE', '00000000-0000-0000-0005-000000000001', '3c58f48a-a9f2-4061-b738-e202b90ea614', '6d0b810a-d658-4a12-8dea-b894576b9df7'),
    ('00000000-0000-0004-0049-000000000003', 'TWIN_CLASS_FREEZE_VIEW', '00000000-0000-0000-0005-000000000001', '38804f99-b259-4af7-a59e-bb9d04bf5e7a', 'd6ef1d92-38b5-42ac-8291-ca8e54a2c1e0'),
    ('00000000-0000-0004-0049-000000000004', 'TWIN_CLASS_FREEZE_UPDATE', '00000000-0000-0000-0005-000000000001', '18c0ff98-2526-474f-bac5-fe525233417e', 'de745075-5151-44b1-aeb6-b611982579b7'),
    ('00000000-0000-0004-0049-000000000005', 'TWIN_CLASS_FREEZE_DELETE', '00000000-0000-0000-0005-000000000001', 'b3ded4dd-262e-4836-b61d-1a50c34348b9', '07a8d112-dc78-40fe-b872-a0335dbddb2f')
ON CONFLICT DO NOTHING;