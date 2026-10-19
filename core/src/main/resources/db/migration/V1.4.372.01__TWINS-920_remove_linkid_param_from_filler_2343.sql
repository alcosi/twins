-- Remove obsolete filler param key "linkId" from FillerBasicsAssigneeFromOutputTwinFieldLink (featurer 2343).
-- The filler no longer filters links by this key; the field value already belongs to one configured link.
-- Cleanup only: runtime ignores extra stored params, so the new code works with or without this migration.
-- Scope: pipeline steps of this filler only. Other hstore keys are untouched.
-- Idempotent: delete() on a missing key is a no-op.
-- Use delete(hstore, text): the minus operator is overloaded as hstore-hstore, so a bare
-- 'linkId' literal is parsed as hstore and fails with "unexpected end of string".
UPDATE twin_factory_pipeline_step
SET filler_params = delete(filler_params, 'linkId')
WHERE filler_featurer_id = 2343
  AND exist(filler_params, 'linkId');
