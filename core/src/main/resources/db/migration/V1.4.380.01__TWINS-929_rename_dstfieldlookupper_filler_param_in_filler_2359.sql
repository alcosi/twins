-- Rename filler param key "dstFieldLookupper" -> "dstFieldLookuper" (typo fix) in
-- FillerForwardLinkToTwinFoundByHeadAndContextFieldDstLinkDst (featurer 2359).
-- The value is preserved; other hstore keys are untouched.
-- Scope: pipeline steps of this filler only.
-- Idempotent: the exist() guard makes a re-run a no-op once the old key is gone.
-- If both keys are somehow present, the old key's value wins (the config that was in use).
-- Use delete(hstore, text) and the || concat with a typed hstore(): the minus operator is
-- overloaded as hstore-hstore, so a bare text literal fails with "unexpected end of string".
UPDATE twin_factory_pipeline_step
SET filler_params = delete(filler_params, 'dstFieldLookupper')
    || hstore('dstFieldLookuper', filler_params -> 'dstFieldLookupper')
WHERE filler_featurer_id = 2359
  AND exist(filler_params, 'dstFieldLookupper');
