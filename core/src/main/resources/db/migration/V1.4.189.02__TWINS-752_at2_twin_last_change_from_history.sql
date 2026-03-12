-- Inserting values ​​for base fields
INSERT INTO twin_last_change (twin_id, twin_class_field_id, last_changed_at)
SELECT t.id, f.id, t.created_at
FROM twin t
         CROSS JOIN (
    VALUES
        ('00000000-0000-0000-0011-000000000003'::uuid),
        ('00000000-0000-0000-0011-000000000004'::uuid),
        ('00000000-0000-0000-0011-000000000005'::uuid),
        ('00000000-0000-0000-0011-000000000006'::uuid),
        ('00000000-0000-0000-0011-000000000007'::uuid),
        ('00000000-0000-0000-0011-000000000008'::uuid),
        ('00000000-0000-0000-0011-000000000009'::uuid),
        ('00000000-0000-0000-0011-000000000010'::uuid),
        ('00000000-0000-0000-0011-000000000011'::uuid)
) AS f(id)
    ON CONFLICT (twin_id, twin_class_field_id) DO UPDATE
SET last_changed_at = excluded.last_changed_at;


-- Refresh twin_last_change from history: for each twin and each tracked field type,
-- take the latest change date from history and update twin_last_change.
-- Only fields that have corresponding history_type_id are updated.

WITH last_changes AS (
    SELECT h.twin_id,
           CASE h.history_type_id
               WHEN 'nameChanged'        THEN '00000000-0000-0000-0011-000000000003'::uuid
               WHEN 'descriptionChanged' THEN '00000000-0000-0000-0011-000000000004'::uuid
               WHEN 'externalIdChanged'  THEN '00000000-0000-0000-0011-000000000005'::uuid
               WHEN 'assigneeChanged'    THEN '00000000-0000-0000-0011-000000000007'::uuid
               WHEN 'createdByChanged'   THEN '00000000-0000-0000-0011-000000000008'::uuid
               WHEN 'headChanged'        THEN '00000000-0000-0000-0011-000000000009'::uuid
               WHEN 'statusChanged'      THEN '00000000-0000-0000-0011-000000000010'::uuid
               END AS twin_class_field_id,
           MAX(h.created_at) AS last_changed_at
    FROM history h
    WHERE h.history_type_id IN (
                                'assigneeChanged',
                                'descriptionChanged',
                                'nameChanged',
                                'externalIdChanged',
                                'createdByChanged',
                                'headChanged',
                                'statusChanged'
        )
    GROUP BY h.twin_id, h.history_type_id
)
INSERT INTO twin_last_change (twin_id, twin_class_field_id, last_changed_at)
SELECT lc.twin_id, lc.twin_class_field_id, lc.last_changed_at
FROM last_changes lc
WHERE lc.twin_class_field_id IS NOT NULL
    ON CONFLICT (twin_id, twin_class_field_id) DO UPDATE
        SET last_changed_at = GREATEST(twin_last_change.last_changed_at, excluded.last_changed_at);
