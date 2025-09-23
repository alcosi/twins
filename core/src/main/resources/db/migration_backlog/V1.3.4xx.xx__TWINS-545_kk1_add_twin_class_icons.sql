ALTER TABLE twin_class
ADD COLUMN IF NOT EXISTS icon_dark_resource_id UUID REFERENCES resource(id),
ADD COLUMN IF NOT EXISTS icon_light_resource_id UUID REFERENCES resource(id);
DROP COLUMN IF EXISTS logo;