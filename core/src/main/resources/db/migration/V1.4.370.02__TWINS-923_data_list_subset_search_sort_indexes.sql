-- Sort-field indexes for data_list_subset search API (sortable: key, name, dataListName; default sort: key)
-- per docs/api_sorting_architecture.md "Indexes (mandatory)" + api_starter checklist ("indexes for sort fields")
-- FK index idx_data_list_subset_data_list_id already exists (V1.4.97.01)
-- name sorts through i18n translations join (see V1.4.xx.05) — no btree index possible

-- data_list_subset search (default sort: key)
CREATE INDEX IF NOT EXISTS idx_data_list_subset_key ON data_list_subset(key);

-- uniqueness of (data_list_id, key): the last line of defence. The service validates duplicates in-batch and
-- against the db, but two concurrent creates can still race past the app-level check-then-act.
-- The plain idx_data_list_subset_key above stays — the composite below does not serve the global ORDER BY key.
CREATE UNIQUE INDEX IF NOT EXISTS ux_data_list_subset_data_list_id_key ON data_list_subset(data_list_id, key);
