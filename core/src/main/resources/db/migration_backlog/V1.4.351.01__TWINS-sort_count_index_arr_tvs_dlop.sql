-- Indexes for the search sort/group fields added together with the count APIs:
--   /private/action_restriction_reason/search|count/v1
--   /private/twin_validator_set/search|count/v1
--   /private/data_list_option_projection/search|count/v1
--
-- Only direct entity sort columns are indexed here. FK group columns are already
-- covered by existing FK indexes (see V1.4.97.01, V1.3.503.01). Boolean (invert) and
-- i18n-translation sort targets are intentionally skipped — low selectivity / shared indexes.

-- action_restriction_reason: sort + group by type
CREATE INDEX IF NOT EXISTS idx_action_restriction_reason_type
    ON action_restriction_reason (type);

-- twin_validator_set: sort by name, description
CREATE INDEX IF NOT EXISTS idx_twin_validator_set_name
    ON twin_validator_set (name);
CREATE INDEX IF NOT EXISTS idx_twin_validator_set_description
    ON twin_validator_set (description);

-- data_list_option_projection: sort by changed_at (default sort field)
CREATE INDEX IF NOT EXISTS idx_data_list_option_projection_changed_at
    ON data_list_option_projection (changed_at);
