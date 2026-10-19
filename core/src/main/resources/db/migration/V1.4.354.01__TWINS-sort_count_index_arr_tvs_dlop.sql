-- Indexes for the search sort/group fields added together with the count APIs:
--   /private/action_restriction_reason/search|count/v1
--   /private/twin_validator_set/search|count/v1
--   /private/data_list_option_projection/search|count/v1
--
-- Direct sort columns + FK columns are indexed here. The DLOP FK group columns
-- (projection_type_id, src/dst_data_list_option_id, saved_by_user_id) are already
-- covered by existing FK indexes (V1.4.97.01, V1.3.503.01). Boolean (invert) is
-- intentionally skipped — low selectivity (seq scan + hash aggregate is faster).

-- action_restriction_reason: sort/group by type + FK indexes (CLAUDE.md: index every FK)
CREATE INDEX IF NOT EXISTS idx_action_restriction_reason_type
    ON action_restriction_reason (type);
CREATE INDEX IF NOT EXISTS idx_action_restriction_reason_domain_id
    ON action_restriction_reason (domain_id);
CREATE INDEX IF NOT EXISTS idx_action_restriction_reason_description_i18n_id
    ON action_restriction_reason (description_i18n_id);

-- twin_validator_set: sort by name, description
CREATE INDEX IF NOT EXISTS idx_twin_validator_set_name
    ON twin_validator_set (name);
CREATE INDEX IF NOT EXISTS idx_twin_validator_set_description
    ON twin_validator_set (description);

-- data_list_option_projection: sort by changed_at (default sort field)
CREATE INDEX IF NOT EXISTS idx_data_list_option_projection_changed_at
    ON data_list_option_projection (changed_at);

-- JOIN-target indexes for DLOP sort by related-entity field (savedByUserName,
-- projectionTypeName, srcDataListOptionName, dstDataListOptionName). Without these,
-- ORDER BY over a JOIN to a non-indexed column degrades to filesort O(N log N)
-- (~350ms@100k, ~4000ms@1M — see docs/api_sorting_architecture.md).
CREATE INDEX IF NOT EXISTS idx_user_name
    ON "user" (name);
CREATE INDEX IF NOT EXISTS idx_projection_type_name
    ON projection_type (name);
CREATE INDEX IF NOT EXISTS idx_data_list_option_option
    ON data_list_option (option);
