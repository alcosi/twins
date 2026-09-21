-- FillerBasicsAssigneeFromOutputTwinFieldLink (featurer id 2343) lost its "linkId" featurer param:
-- the filler no longer filters the field's links by link id (the field value already belongs to ONE
-- configured link), so the stored key is obsolete. Cleanup only, NOT a functional prerequisite: the
-- featurer runtime ignores extra stored params (FeaturerService checks for MISSING required params only),
-- so the new code works with or without this migration.
-- Scope: this filler's pipeline steps only, other hstore keys are untouched.
-- Idempotent: hstore key subtraction on a missing key is a no-op.
-- Rollback: the dropped linkId uuids are not recoverable from the db — restore from a pre-migration
-- snapshot if ever needed.
UPDATE twin_factory_pipeline_step
SET filler_params = filler_params - 'linkId'
WHERE filler_featurer_id = 2343
  AND filler_params ? 'linkId';
