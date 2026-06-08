-- Migrate edit_permission_id and delete_permission_id from twin_class to twin_action_permission
-- These fields on twin_class are now deprecated in favor of TwinActionService-based permission checks

-- RESTRICTED_BY_PERMISSION reason id (seeded in V1.4.225.01)
-- '00000000-0000-0000-0000-000000000001'

-- Migrate edit_permission_id
INSERT INTO twin_action_permission (id, twin_class_id, twin_action_id, permission_id, action_restriction_reason_id)
SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, tc.id::text || ':EDIT'),
       tc.id, 'EDIT', tc.edit_permission_id, '00000000-0000-0000-0000-000000000001'
FROM twin_class tc
WHERE tc.edit_permission_id IS NOT NULL
ON CONFLICT (id) DO NOTHING;

-- Migrate delete_permission_id
INSERT INTO twin_action_permission (id, twin_class_id, twin_action_id, permission_id, action_restriction_reason_id)
SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, tc.id::text || ':DELETE'),
       tc.id, 'DELETE', tc.delete_permission_id, '00000000-0000-0000-0000-000000000001'
FROM twin_class tc
WHERE tc.delete_permission_id IS NOT NULL
ON CONFLICT (id) DO NOTHING;
