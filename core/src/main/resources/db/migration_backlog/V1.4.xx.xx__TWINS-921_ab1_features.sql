-- TWINS-921: factory fillers for actual date/duration automation on status change.
--   2367 Field date current — set date field to now when empty
--   2368 Field date shift by duration — target = source ± (duration - 1) inclusive days when target empty
--   2369 Field duration between dates — duration = (end - start + 1) inclusive days when duration empty
-- LVL-9372: recomputers for field-edit date/duration recalculation (independent of status factories).
--   5503 Recomputer field date shift by duration — subscriber date = source ± (duration - 1)
--   5504 Recomputer field duration between dates — subscriber duration = (end - start + 1)
-- Featurer stub: class/name/description are filled from @Featurer at app startup.

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (2367, 23, '', '', '', false)
ON CONFLICT DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (2368, 23, '', '', '', false)
ON CONFLICT DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (2369, 23, '', '', '', false)
ON CONFLICT DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5503, 55, '', '', '', false)
ON CONFLICT DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5504, 55, '', '', '', false)
ON CONFLICT DO NOTHING;
