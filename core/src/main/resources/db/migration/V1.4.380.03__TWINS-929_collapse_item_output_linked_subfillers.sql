-- Collapse the lookuper-pinning filler subclasses of FillerFieldFromItemOutputLinked into the
-- merged param-driven filler 2335 (the id of the first subclass is REUSED by the merged class;
-- the fieldLookuper param is REQUIRED there — no natural default among the three sources).
-- Deleted fillers and their replacements:
--   2335 FillerFieldFromItemOutputLinkedTwinField          -> 2335 + fieldLookuper=fromItemOutputLinkedTwinFields
--   2336 FillerFieldFromItemOutputLinkedTwinHeadTwinField  -> 2335 + fieldLookuper=fromItemOutputLinkedTwinHeadTwinFields
--   2337 FillerFieldFromItemOutputHeadTwinLinkedTwinField  -> 2335 + fieldLookuper=fromItemOutputHeadTwinLinkedTwinFields
-- Other hstore keys are preserved (|| concat keeps existing keys). An explicit fieldLookuper
-- already stored on a migrated step is overwritten with the value the deleted filler implied.
-- Idempotent: rows no longer match the old filler_featurer_id after the first run.
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2335, filler_params = filler_params || hstore('fieldLookuper', 'fromItemOutputLinkedTwinFields')         WHERE filler_featurer_id = 2335 AND NOT exist(filler_params, 'fieldLookuper');
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2335, filler_params = filler_params || hstore('fieldLookuper', 'fromItemOutputLinkedTwinHeadTwinFields') WHERE filler_featurer_id = 2336;
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2335, filler_params = filler_params || hstore('fieldLookuper', 'fromItemOutputHeadTwinLinkedTwinFields') WHERE filler_featurer_id = 2337;
