-- Add created_by_user_id and created_at fields to notification_schema
ALTER TABLE notification_schema
    ADD COLUMN IF NOT EXISTS created_by_user_id uuid;

ALTER TABLE notification_schema
    ADD COLUMN IF NOT EXISTS created_at timestamp DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE notification_schema
    ADD CONSTRAINT notification_schema_created_by_user_id_fk
        FOREIGN KEY (created_by_user_id) REFERENCES "user"(id)
        ON UPDATE CASCADE ON DELETE SET NULL;

-- Indexes for new columns
CREATE INDEX IF NOT EXISTS idx_notification_schema_created_by_user_id ON notification_schema(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_notification_schema_created_at ON notification_schema(created_at);

-- Add NOTIFICATION_SCHEMA_DESCRIPTION i18n type
INSERT INTO i18n_type (id, name) VALUES ('notificationSchemaDescription', 'notification schema description') ON CONFLICT ON CONSTRAINT i18n_type_pk DO NOTHING;

-- NOTIFICATION SCHEMA permissions
INSERT INTO i18n (id, name, key, i18n_type_id, domain_id)
VALUES
    ('019c6aca-8a8c-7efa-a06e-049b5f8144af', 'Notification schema manage name', null, 'permissionName', null),
    ('019c6acb-27ea-7022-b5a9-d155d1ae443b', 'Notification schema manage description', null, 'permissionDescription', null),
    ('019c6acc-42d7-7845-a454-58dc2a7eaef4', 'Notification schema create name', null, 'permissionName', null),
    ('019c6acd-86b6-79a3-81bc-68080e26aa35', 'Notification schema create description', null, 'permissionDescription', null),
    ('019c6ace-a697-7960-a47e-01d127c49b76', 'Notification schema view name', null, 'permissionName', null),
    ('019c6acf-c7e4-7d83-9078-9aeec1f41d37', 'Notification schema view description', null, 'permissionDescription', null),
    ('019c6ad0-eb2f-780c-8b0f-22cbf80f23e8', 'Notification schema update name', null, 'permissionName', null),
    ('019c6ad1-1f82-7eb5-89bb-5884d23239f9', 'Notification schema update description', null, 'permissionDescription', null),
    ('019c6ad2-4341-7dbd-92a5-e7fe73c857da', 'Notification schema delete name', null, 'permissionName', null),
    ('019c6ad3-657d-74eb-8363-82fd237255ec', 'Notification schema delete description', null, 'permissionDescription', null)
    ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES
    ('019c6aca-8a8c-7efa-a06e-049b5f8144af', 'en', 'Notification schema manage', DEFAULT),
    ('019c6acb-27ea-7022-b5a9-d155d1ae443b', 'en', 'Notification schema manage', DEFAULT),
    ('019c6acc-42d7-7845-a454-58dc2a7eaef4', 'en', 'Notification schema create', DEFAULT),
    ('019c6acd-86b6-79a3-81bc-68080e26aa35', 'en', 'Notification schema create', DEFAULT),
    ('019c6ace-a697-7960-a47e-01d127c49b76', 'en', 'Notification schema view', DEFAULT),
    ('019c6acf-c7e4-7d83-9078-9aeec1f41d37', 'en', 'Notification schema view', DEFAULT),
    ('019c6ad0-eb2f-780c-8b0f-22cbf80f23e8', 'en', 'Notification schema update', DEFAULT),
    ('019c6ad1-1f82-7eb5-89bb-5884d23239f9', 'en', 'Notification schema update', DEFAULT),
    ('019c6ad2-4341-7dbd-92a5-e7fe73c857da', 'en', 'Notification schema delete', DEFAULT),
    ('019c6ad3-657d-74eb-8363-82fd237255ec', 'en', 'Notification schema delete', DEFAULT)
    ON CONFLICT DO NOTHING;

INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES
    ('00000000-0000-0004-0057-000000000001', 'NOTIFICATION_SCHEMA_MANAGE', '00000000-0000-0000-0005-000000000001', '019c6aca-8a8c-7efa-a06e-049b5f8144af', '019c6acb-27ea-7022-b5a9-d155d1ae443b'),
    ('00000000-0000-0004-0057-000000000002', 'NOTIFICATION_SCHEMA_CREATE', '00000000-0000-0000-0005-000000000001', '019c6acc-42d7-7845-a454-58dc2a7eaef4', '019c6acd-86b6-79a3-81bc-68080e26aa35'),
    ('00000000-0000-0004-0057-000000000003', 'NOTIFICATION_SCHEMA_VIEW', '00000000-0000-0000-0005-000000000001', '019c6ace-a697-7960-a47e-01d127c49b76', '019c6acf-c7e4-7d83-9078-9aeec1f41d37'),
    ('00000000-0000-0004-0057-000000000004', 'NOTIFICATION_SCHEMA_UPDATE', '00000000-0000-0000-0005-000000000001', '019c6ad0-eb2f-780c-8b0f-22cbf80f23e8', '019c6ad1-1f82-7eb5-89bb-5884d23239f9'),
    ('00000000-0000-0004-0057-000000000005', 'NOTIFICATION_SCHEMA_DELETE', '00000000-0000-0000-0005-000000000001', '019c6ad2-4341-7dbd-92a5-e7fe73c857da', '019c6ad3-657d-74eb-8363-82fd237255ec')
    ON CONFLICT DO NOTHING;

INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000282'::uuid, '00000000-0000-0004-0057-000000000001'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000283'::uuid, '00000000-0000-0004-0057-000000000002'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000284'::uuid, '00000000-0000-0004-0057-000000000003'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000285'::uuid, '00000000-0000-0004-0057-000000000004'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000286'::uuid, '00000000-0000-0004-0057-000000000005'::uuid, '00000000-0000-0000-0006-000000000001'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
-- permissions for admin viewer
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000287'::uuid, '00000000-0000-0004-0057-000000000001'::uuid, '00000000-0000-0000-0006-000000000003'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
INSERT INTO permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000288'::uuid, '00000000-0000-0004-0057-000000000003'::uuid, '00000000-0000-0000-0006-000000000003'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, DEFAULT) on conflict do nothing;
