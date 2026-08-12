ALTER TABLE data_list ADD COLUMN IF NOT EXISTS created_by_user_id UUID;
CREATE INDEX IF NOT EXISTS idx_data_list_created_by_user_id ON data_list(created_by_user_id);

-- Backfill legacy data_list rows created before this column existed.
-- The real creator is unknown, so assign the system user (SystemIds.User.SYSTEM).
-- Without this, count-by-createdByUserId would return a single large NULL group.
UPDATE data_list SET created_by_user_id = '00000000-0000-0000-0000-000000000000' WHERE created_by_user_id IS NULL;


-- Indexes backing the sort/count API (TWINS-912).
-- PostgreSQL does NOT auto-create indexes on FK columns (unlike MySQL), so each
-- FK used as a GroupField (GROUP BY) or SortField (ORDER BY) needs an explicit index.
-- Without these, GROUP BY falls back to a seq scan + sort and ORDER BY to a filesort.
--
-- See ai/plans/SORTING-ALL-SEARCH-API.md "Индексы БД" — the plan requires an index
-- per SortField/GroupField; these 5 domains were migrated without index migrations.

-- twin_trigger_task: GroupField (businessAccountId, previousTwinStatusId) + default SortField (createdAt).
-- High-churn OLTP table — these were missing and are the most impactful.
CREATE INDEX IF NOT EXISTS idx_twin_trigger_task_business_account_id
    ON twin_trigger_task (business_account_id);
CREATE INDEX IF NOT EXISTS idx_twin_trigger_task_previous_twin_status_id
    ON twin_trigger_task (previous_twin_status_id);
CREATE INDEX IF NOT EXISTS idx_twin_trigger_task_created_at
    ON twin_trigger_task (created_at);

-- twin_trigger: GroupField jobTwinClassId. The FK was added in V1.4.225.02 without an index.
CREATE INDEX IF NOT EXISTS idx_twin_trigger_job_twin_class_id
    ON twin_trigger (job_twin_class_id);

-- twinflow_factory: GroupField twinflowId. Only covered as the 2nd column of a composite
-- UNIQUE (twin_factory_launcher_id, twinflow_id), which cannot serve a lookup/group by
-- twinflow_id alone — a standalone index is needed.
CREATE INDEX IF NOT EXISTS idx_twinflow_factory_twinflow_id
    ON twinflow_factory (twinflow_id);

