-- Collapse FillerForwardLinksFromContextTwinAll (2303) into FillerForwardLinksFromContextTwin (2305):
-- the linksIds param became optional — an empty/absent value means NO filtering, i.e. exactly the
-- former "All" behavior, so the migrated steps need no param value. A stray linksIds key (impossible
-- via the UI — the param was not declared on 2303) is stripped to preserve that behavior.
-- Idempotent: rows no longer match the old filler_featurer_id after the first run.
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2305, filler_params = filler_params - 'linksIds' WHERE filler_featurer_id = 2303;
