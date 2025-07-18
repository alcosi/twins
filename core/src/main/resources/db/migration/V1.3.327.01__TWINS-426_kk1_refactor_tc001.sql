ALTER TABLE face_tc001
    DROP COLUMN IF EXISTS field_finder_featurer_id,
    DROP COLUMN IF EXISTS field_finder_params;

ALTER TABLE face_tc001
    ADD COLUMN IF NOT EXISTS twin_class_field_search_id UUID;