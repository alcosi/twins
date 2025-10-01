INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('0f561cc4-6622-4aab-8206-b825a8f117f5', '', null, 'permissionName', null),
    ('8b12867b-9867-4cbd-823b-5ef5bf5a9bb5', '', null, 'permissionDescription', null)
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation(i18n_id, locale, translation, usage_counter)
VALUES
    ('0f561cc4-6622-4aab-8206-b825a8f117f5', 'en', 'Twin class field plug permission', 0),
    ('8b12867b-9867-4cbd-823b-5ef5bf5a9bb5', 'en', 'twin class field plug permission', 0)
ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0047-000000000001', 'TWIN_CLASS_FIELD_PLUG_MANAGE', '00000000-0000-0000-0005-000000000001', '0f561cc4-6622-4aab-8206-b825a8f117f5', '8b12867b-9867-4cbd-823b-5ef5bf5a9bb5')
ON CONFLICT DO NOTHING;
