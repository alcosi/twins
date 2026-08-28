-- TWINS-875: new pointer featurers for twin->twin navigation (used by factory_pipeline_inject,
-- face widgets, projections, recompute listeners).
--   3107 PointerOnLinkedTwinHead   src -> linked twin (by link id) -> its head
--   3108 PointerOnHeadLinkedTwin   src -> head -> linked twin (by link id)
--   3109 PointerOnLinkedChained    ordered chain of link ids; HEAD_ID token = head hop
--   3110 PointerOnPointerChained   ordered chain of arbitrary twin_pointer_id
-- class/name/description are backfilled from @Featurer annotations by the startup sync.
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
    VALUES (3107::integer, 31::integer, '', '', '', DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
    VALUES (3108::integer, 31::integer, '', '', '', DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
    VALUES (3109::integer, 31::integer, '', '', '', DEFAULT) ON CONFLICT DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
    VALUES (3110::integer, 31::integer, '', '', '', DEFAULT) ON CONFLICT DO NOTHING;
