DELETE FROM twin_tag
WHERE ctid IN (
    SELECT ctid
    FROM (
             SELECT ctid, ROW_NUMBER() OVER (PARTITION BY twin_id, tag_data_list_option_id ORDER BY twin_id, tag_data_list_option_id) AS rn
             FROM twin_tag
         ) sub
    WHERE rn > 1
);
DELETE FROM twin_marker
WHERE ctid IN (
    SELECT ctid
    FROM (
             SELECT ctid, ROW_NUMBER() OVER (PARTITION BY twin_id, marker_data_list_option_id ORDER BY twin_id, marker_data_list_option_id) AS rn
             FROM twin_marker
         ) sub
    WHERE rn > 1
);
CREATE UNIQUE INDEX if not exists idx_twin_marker_unique ON twin_marker(twin_id, marker_data_list_option_id);
CREATE UNIQUE INDEX if not exists idx_twin_tag_unique ON twin_tag(twin_id, tag_data_list_option_id);
