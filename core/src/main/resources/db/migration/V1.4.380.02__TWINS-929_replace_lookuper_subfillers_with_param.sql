-- Replace lookuper-pinning filler subclasses with the "fieldLookuper" step param of their parent
-- filler (FillerFieldLookup now resolves the value source from the optional fieldLookuper param,
-- falling back to the parent's default). Deleted fillers and their replacements:
--   2312 FillerFieldFromContextField            -> 2323 + fieldLookuper=fromContextFields
--   2311 FillerFieldFromContextTwinDbField      -> 2323 + fieldLookuper=fromContextTwinDbFields
--   2365 FillerFieldFromContextTwinField        -> 2323 + fieldLookuper=fromContextTwinFields
--   2363 FillerFieldFromItemField               -> 2323 + fieldLookuper=fromItemOutputFields
--   2315 FillerBasicsAssigneeFromContextField   -> 2324 + fieldLookuper=fromContextFields
--   2314 FillerBasicsAssigneeFromContextTwinField -> 2324 + fieldLookuper=fromContextTwinDbFields
--   2352 FillerBasicsAssigneeFromItemOutput     -> 2324 + fieldLookuper=fromItemOutputFields
-- Other hstore keys are preserved (|| concat keeps existing keys). An explicit fieldLookuper
-- already stored on a migrated step is overwritten with the value the deleted filler implied.
-- Idempotent: rows no longer match the old filler_featurer_id after the first run.
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2323, filler_params = filler_params || hstore('fieldLookuper', 'fromContextFields')         WHERE filler_featurer_id = 2312;
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2323, filler_params = filler_params || hstore('fieldLookuper', 'fromContextTwinDbFields') WHERE filler_featurer_id = 2311;
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2323, filler_params = filler_params || hstore('fieldLookuper', 'fromContextTwinFields')   WHERE filler_featurer_id = 2365;
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2323, filler_params = filler_params || hstore('fieldLookuper', 'fromItemOutputFields')    WHERE filler_featurer_id = 2363;
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2324, filler_params = filler_params || hstore('fieldLookuper', 'fromContextFields')       WHERE filler_featurer_id = 2315;
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2324, filler_params = filler_params || hstore('fieldLookuper', 'fromContextTwinDbFields') WHERE filler_featurer_id = 2314;
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2324, filler_params = filler_params || hstore('fieldLookuper', 'fromItemOutputFields')    WHERE filler_featurer_id = 2352;
