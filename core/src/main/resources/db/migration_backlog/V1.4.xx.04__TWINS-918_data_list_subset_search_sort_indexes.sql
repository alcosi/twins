-- Sort-field indexes for data_list_subset search API (sortable: name, key; default sort: key)
-- per docs/api_sorting_architecture.md "Indexes (mandatory)" + api_starter checklist ("indexes for sort fields")
-- FK index idx_data_list_subset_data_list_id already exists (V1.4.97.01)
-- TODO: replace TWINS-XXX in the filename with the actual ticket number.

-- data_list_subset search (default sort: key; also sortable: name)
CREATE INDEX IF NOT EXISTS idx_data_list_subset_key ON data_list_subset(key);
CREATE INDEX IF NOT EXISTS idx_data_list_subset_name ON data_list_subset(name);
