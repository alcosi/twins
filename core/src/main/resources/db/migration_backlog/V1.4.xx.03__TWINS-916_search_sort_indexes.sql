-- Sort-field indexes for new search APIs (featurer, featurer_type, projection_type_group, twin_attachment_restriction)
-- per docs/api_sorting_architecture.md "Indexes (mandatory)" + api_starter checklist ("indexes for sort fields")
-- TODO: replace TWINS-XXX in the filename with the actual ticket number.

-- featurer search (default sort: name; also sortable: description)
CREATE INDEX IF NOT EXISTS idx_featurer_name ON featurer(name);
CREATE INDEX IF NOT EXISTS idx_featurer_description ON featurer(description);

-- featurer_type search (default sort: name; also sortable: description)
CREATE INDEX IF NOT EXISTS idx_featurer_type_name ON featurer_type(name);
CREATE INDEX IF NOT EXISTS idx_featurer_type_description ON featurer_type(description);

-- twin_attachment_restriction search (sortable: min_count, max_count, file_size_mb_limit)
CREATE INDEX IF NOT EXISTS idx_twin_attachment_restriction_min_count ON twin_attachment_restriction(min_count);
CREATE INDEX IF NOT EXISTS idx_twin_attachment_restriction_max_count ON twin_attachment_restriction(max_count);
CREATE INDEX IF NOT EXISTS idx_twin_attachment_restriction_file_size_mb_limit ON twin_attachment_restriction(file_size_mb_limit);
