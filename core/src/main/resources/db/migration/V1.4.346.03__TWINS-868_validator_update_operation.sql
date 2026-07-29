-- TWINS-868: register new TwinValidator featurer "Twin update operation" (id 1624, type 16)
-- Validates the twin is being updated (not created) via the TwinEntity.createElseUpdate flag.
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1624::integer, 16::integer, '', '', '', DEFAULT) on conflict do nothing;
