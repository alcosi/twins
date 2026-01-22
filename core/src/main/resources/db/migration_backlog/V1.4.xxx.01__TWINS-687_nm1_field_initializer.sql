INSERT INTO featurer_type (id, name) VALUES (53, 'Field initializer') ON CONFLICT (id) DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5301, 53, '', '', '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5302, 53, '', '', '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5303, 53, '', '', '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5304, 53, '', '', '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5305, 53, '', '', '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5306, 53, '', '', '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5307, 53, '', '', '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5308, 53, '', '', '', false) ON CONFLICT (id) DO NOTHING;

ALTER TABLE IF EXISTS twin_class_field
    ADD COLUMN IF NOT EXISTS field_initializer_featurer_id int,
    ADD COLUMN IF NOT EXISTS field_initializer_params hstore;

--lists will get FieldInitializerListDefaultOrNull
UPDATE twin_class_field SET field_initializer_featurer_id = 5305 WHERE field_initializer_featurer_id IS NULL AND field_typer_featurer_id IN (1305, 1307, 1308, 1309);
--all others
UPDATE twin_class_field SET field_initializer_featurer_id = 5301 WHERE field_initializer_featurer_id IS NULL;

ALTER TABLE IF EXISTS twin_class_field ALTER COLUMN field_initializer_featurer_id SET NOT NULL;
