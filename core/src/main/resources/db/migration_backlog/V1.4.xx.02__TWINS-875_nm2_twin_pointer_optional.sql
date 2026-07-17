-- TWINS-875: add `optional` flag to twin_pointer to bound the blast radius of pointer
-- resolution failures (e.g. POINTER_NON_SINGLE when a twin has >1 forward link of the same type).
--   optional = true  -> resolution failure is swallowed (log.warn + cached null), so a single
--                       anomalous twin can no longer roll back the whole recompute batch.
--   optional = false (default) -> strict: resolution failure rethrows (current fail-fast behaviour).
ALTER TABLE twin_pointer
    ADD COLUMN IF NOT EXISTS optional boolean NOT NULL DEFAULT false;
