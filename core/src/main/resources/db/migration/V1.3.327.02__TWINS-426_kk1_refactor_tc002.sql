ALTER TABLE face_tc002_option
    DROP COLUMN IF EXISTS field_finder_featurer_id,
    DROP COLUMN IF EXISTS field_finder_params;

ALTER TABLE face_tc002_option
    ADD COLUMN IF NOT EXISTS twin_class_field_search_id UUID,
    ADD COLUMN IF NOT EXISTS label_i18n_id UUID REFERENCES i18n(id) ON DELETE SET NULL ON UPDATE CASCADE;