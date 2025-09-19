ALTER TABLE face_tc001_option
ADD COLUMN IF NOT EXISTS twin_class_id UUID REFERENCES twin_class(id);