-- Collapse the assignee-from-linked-twin filler variants into the param-driven filler 2322
-- (the link-field value source became the optional fieldLookuper param, default fromContextFields).
-- Deleted fillers and their replacements:
--   2328 FillerBasicsAssigneeFromContextTwinFieldTwinAssignee -> 2322 + fieldLookuper=fromContextTwinDbFields
--   2343 FillerBasicsAssigneeFromOutputTwinFieldLink          -> 2322 + fieldLookuper=fromItemOutputUncommitedFields
-- 2343 is the only variant that did not extend 2322: its required param key twinClassFieldId is
-- renamed to 2322's linkField key (value preserved) — without the rename 2322 would reject every
-- migrated step with a missing non-optional param.
-- Steps already on 2322 keep their behaviour via the param default; an explicit fieldLookuper is
-- only backfilled when absent (NOT exist guard). Other hstore keys are preserved (|| concat keeps
-- existing keys). Idempotent: rows no longer match the old filler_featurer_id after the first run.
-- Use delete(hstore, text) for the key rename: the minus operator is overloaded as hstore-hstore,
-- so a bare text literal fails with "unexpected end of string" (same as in V1.4.xx.01).
UPDATE twin_factory_pipeline_step SET filler_params = filler_params || hstore('fieldLookuper', 'fromContextFields')                 WHERE filler_featurer_id = 2322 AND NOT exist(filler_params, 'fieldLookuper');
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2322, filler_params = filler_params || hstore('fieldLookuper', 'fromContextTwinDbFields')       WHERE filler_featurer_id = 2328;
UPDATE twin_factory_pipeline_step SET filler_featurer_id = 2322, filler_params = delete(filler_params, 'twinClassFieldId') || hstore('linkField', filler_params -> 'twinClassFieldId') || hstore('fieldLookuper', 'fromItemOutputUncommitedFields') WHERE filler_featurer_id = 2343;
