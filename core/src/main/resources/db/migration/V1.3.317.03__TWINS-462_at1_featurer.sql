UPDATE featurer SET class = 'org.twins.core.featurer.fieldtyper.FieldTyperCountChildrenByStatusV1' WHERE id = 1314;
UPDATE featurer SET class = 'org.twins.core.featurer.fieldtyper.FieldTyperCountChildrenByStatusV2' WHERE id = 1315;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1333, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperCountChildrenTwinsByTwinClassV1', 'Calculate child twins by twin class id', '', false) on conflict do nothing;
