-- Migrate edit_permission_id and delete_permission_id from twin_class to twin_action_permission
-- These fields on twin_class are now deprecated in favor of TwinActionService-based permission checks

-- RESTRICTED_BY_PERMISSION reason id (seeded in V1.4.225.01)
-- '00000000-0000-0000-0000-000000000001'

-- Remove duplicates on (twin_class_id, twin_action_id): prefer deterministic v5 id, else keep smallest id
DELETE FROM twin_action_permission
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY twin_class_id, twin_action_id
                   ORDER BY
                       CASE WHEN id = uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, twin_class_id::text || ':' || twin_action_id)
                            THEN 0 ELSE 1 END,
                       id
               ) AS rn
        FROM twin_action_permission
    ) ranked
    WHERE rn > 1
);

-- Sync permission_id for rows that already exist (e.g. created before this migration)
UPDATE twin_action_permission tap
SET permission_id = tc.edit_permission_id
FROM twin_class tc
WHERE tap.twin_class_id = tc.id
  AND tap.twin_action_id = 'EDIT'
  AND tc.edit_permission_id IS NOT NULL
  AND tap.permission_id IS DISTINCT FROM tc.edit_permission_id;

UPDATE twin_action_permission tap
SET permission_id = tc.delete_permission_id
FROM twin_class tc
WHERE tap.twin_class_id = tc.id
  AND tap.twin_action_id = 'DELETE'
  AND tc.delete_permission_id IS NOT NULL
  AND tap.permission_id IS DISTINCT FROM tc.delete_permission_id;

-- Migrate edit_permission_id
INSERT INTO twin_action_permission (id, twin_class_id, twin_action_id, permission_id, action_restriction_reason_id)
SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, tc.id::text || ':EDIT'),
       tc.id, 'EDIT', tc.edit_permission_id, '00000000-0000-0000-0000-000000000001'
FROM twin_class tc
WHERE tc.edit_permission_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM twin_action_permission tap
      WHERE tap.twin_class_id = tc.id AND tap.twin_action_id = 'EDIT'
  )
ON CONFLICT (id) DO NOTHING;

-- Migrate delete_permission_id
INSERT INTO twin_action_permission (id, twin_class_id, twin_action_id, permission_id, action_restriction_reason_id)
SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, tc.id::text || ':DELETE'),
       tc.id, 'DELETE', tc.delete_permission_id, '00000000-0000-0000-0000-000000000001'
FROM twin_class tc
WHERE tc.delete_permission_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM twin_action_permission tap
      WHERE tap.twin_class_id = tc.id AND tap.twin_action_id = 'DELETE'
  )
ON CONFLICT (id) DO NOTHING;

drop index if exists twin_action_permission_twin_class_id_index;

create unique index if not exists twin_action_permission_twin_class_id_twin_action_id_index
    on twin_action_permission (twin_class_id, twin_action_id);
