INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5703, 57, '', 'Sequential source-to-linked search',
        'Runs the source saved search, then children as searches of twins linked from the source twins', false)
ON CONFLICT DO NOTHING;