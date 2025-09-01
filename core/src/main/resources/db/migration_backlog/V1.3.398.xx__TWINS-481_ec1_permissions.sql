INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('2be232c6-d826-4ae7-9411-ea528c280439', '', null, 'permissionName', null),
    ('958f6316-e0af-4c30-95f5-621c0ec5d956', '', null, 'permissionDescription', null),
    ('c7a3e8b1-f492-4d8a-b6c3-9a1b5d2e8f7a', '', null, 'permissionName', null),
    ('d8b4f9c2-e583-5e9b-c7d4-0b2c6e3f8a9d', '', null, 'permissionDescription', null),
    ('e9c5d0e3-f674-6fac-d8e5-1c3d7f4g9b0e', '', null, 'permissionName', null),
    ('f0d6e1f4-g785-7gbd-e9f6-2d4e8g5h0c1f', '', null, 'permissionDescription', null),
    ('g1e7f2g5-h896-8hce-f0g7-3e5f9h6i1d2g', '', null, 'permissionName', null),
    ('h2f8g3h6-i9a7-9idf-g1h8-4f6g0i7j2e3h', '', null, 'permissionDescription', null),
    ('i3g9h4i7-j0b8-0jeg-h2i9-5g7h1j8k3f4i', '', null, 'permissionName', null),
    ('j4h0i5j8-k1c9-1kfh-i3j0-6h8i2k9l4g5j', '', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation(i18n_id, locale, translation, usage_counter)
VALUES
    ('2be232c6-d826-4ae7-9411-ea528c280439', 'en', 'Twinflow factory manage permission', 0),
    ('958f6316-e0af-4c30-95f5-621c0ec5d956', 'en', 'twinflow factory manage permission', 0),
    ('c7a3e8b1-f492-4d8a-b6c3-9a1b5d2e8f7a', 'en', 'Twinflow factory create permission', 0),
    ('d8b4f9c2-e583-5e9b-c7d4-0b2c6e3f8a9d', 'en', 'twinflow factory create permission', 0),
    ('e9c5d0e3-f674-6fac-d8e5-1c3d7f4g9b0e', 'en', 'Twinflow factory view permission', 0),
    ('f0d6e1f4-g785-7gbd-e9f6-2d4e8g5h0c1f', 'en', 'twinflow factory view permission', 0),
    ('g1e7f2g5-h896-8hce-f0g7-3e5f9h6i1d2g', 'en', 'Twinflow factory update permission', 0),
    ('h2f8g3h6-i9a7-9idf-g1h8-4f6g0i7j2e3h', 'en', 'twinflow factory update permission', 0),
    ('i3g9h4i7-j0b8-0jeg-h2i9-5g7h1j8k3f4i', 'en', 'Twinflow factory delete permission', 0),
    ('j4h0i5j8-k1c9-1kfh-i3j0-6h8i2k9l4g5j', 'en', 'twinflow factory delete permission', 0)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0045-000000000001', 'TWINFLOW_FACTORY_MANAGE', '00000000-0000-0000-0005-000000000001', '2be232c6-d826-4ae7-9411-ea528c280439', '958f6316-e0af-4c30-95f5-621c0ec5d956'),
    ('00000000-0000-0004-0045-000000000002', 'TWINFLOW_FACTORY_CREATE', '00000000-0000-0000-0005-000000000001', 'c7a3e8b1-f492-4d8a-b6c3-9a1b5d2e8f7a', 'd8b4f9c2-e583-5e9b-c7d4-0b2c6e3f8a9d'),
    ('00000000-0000-0004-0045-000000000003', 'TWINFLOW_FACTORY_VIEW', '00000000-0000-0000-0005-000000000001', 'e9c5d0e3-f674-6fac-d8e5-1c3d7f4g9b0e', 'f0d6e1f4-g785-7gbd-e9f6-2d4e8g5h0c1f'),
    ('00000000-0000-0004-0045-000000000004', 'TWINFLOW_FACTORY_UPDATE', '00000000-0000-0000-0005-000000000001', 'g1e7f2g5-h896-8hce-f0g7-3e5f9h6i1d2g', 'h2f8g3h6-i9a7-9idf-g1h8-4f6g0i7j2e3h'),
    ('00000000-0000-0004-0045-000000000005', 'TWINFLOW_FACTORY_DELETE', '00000000-0000-0000-0005-000000000001', 'i3g9h4i7-j0b8-0jeg-h2i9-5g7h1j8k3f4i', 'j4h0i5j8-k1c9-1kfh-i3j0-6h8i2k9l4g5j')
ON CONFLICT DO NOTHING;
