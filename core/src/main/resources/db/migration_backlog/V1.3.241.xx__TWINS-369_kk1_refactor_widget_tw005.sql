ALTER TABLE face_twidget_tw005 ADD COLUMN IF NOT EXISTS style_classes VARCHAR;

ALTER TABLE face_twidget_tw005 DROP COLUMN IF EXISTS style_attributes;

ALTER TABLE face_twidget_tw005_button ADD COLUMN IF NOT EXISTS style_classes VARCHAR;

ALTER TABLE face_twidget_tw005_button DROP COLUMN IF EXISTS style_attributes;