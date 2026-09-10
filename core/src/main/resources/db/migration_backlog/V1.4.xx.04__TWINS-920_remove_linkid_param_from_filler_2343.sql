-- FillerBasicsAssigneeFromOutputTwinFieldLink (featurer id 2343) lost its "linkId" featurer param:
-- the filler no longer filters the field's links by link id (the field value already belongs to ONE
-- configured link), so a stored linkId makes the featurer complain about a wrong parameter count.
-- Drop the key from the stored hstore for this filler's pipeline steps only; other keys are untouched.
-- Idempotent: hstore key subtraction on a missing key is a no-op.
UPDATE twin_factory_pipeline_step
SET filler_params = filler_params - 'linkId'
WHERE filler_featurer_id = 2343
  AND filler_params ? 'linkId';
