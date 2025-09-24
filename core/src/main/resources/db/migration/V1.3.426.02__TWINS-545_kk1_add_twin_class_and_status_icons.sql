ALTER TABLE twin_class
    ADD COLUMN IF NOT EXISTS icon_dark_resource_id  UUID REFERENCES resource (id),
    ADD COLUMN IF NOT EXISTS icon_light_resource_id UUID REFERENCES resource (id),
    DROP COLUMN IF EXISTS logo;

drop view if exists twin_status_lazy;

ALTER TABLE twin_status
    ADD COLUMN IF NOT EXISTS icon_dark_resource_id  UUID REFERENCES resource (id),
    ADD COLUMN IF NOT EXISTS icon_light_resource_id UUID REFERENCES resource (id),
    DROP COLUMN IF EXISTS logo;
