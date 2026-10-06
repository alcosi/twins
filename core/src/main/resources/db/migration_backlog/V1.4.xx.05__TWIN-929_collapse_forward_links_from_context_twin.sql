-- Collapse FillerForwardLinksFromContextTwinAll (2303) into FillerForwardLinksFromContextTwin (2305):
-- the linksIds param became optional — an empty/absent value means NO filtering, i.e. exactly the
-- former "All" behavior, so the migrated steps need no param value. A stray linksIds key (impossible
-- via the UI — the param was not declared on 2303) is stripped to preserve that behavior.
-- Idempotent: rows no longer match the old filler_featurer_id after the first run.
-- Use delete(hstore, text): the minus operator is overloaded as hstore-hstore, so a bare
-- 'linksIds' literal is parsed as hstore and fails with "unexpected end of string"
-- (same as in V1.4.372.01 and V1.4.xx.01).
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2305, filler_params = delete(filler_params, 'linksIds') WHERE filler_featurer_id = 2303;
