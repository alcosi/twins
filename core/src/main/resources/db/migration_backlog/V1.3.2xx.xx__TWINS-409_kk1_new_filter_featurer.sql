INSERT INTO featurer_type (id, name) VALUES (36, 'Filter') on conflict (id) do nothing;

INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3601, 35, 'org.twins.core.featurer.fieldfilter.FieldFilterGiven', 'Field filter by given ids', '', false) on conflict (id) do nothing;

ALTER TABLE face_twidget_tw004
ADD COLUMN IF NOT EXISTS field_filter_featurer_id INTEGER REFERENCES featurer(id) ON DELETE CASCADE ON UPDATE CASCADE,
ADD COLUMN IF NOT EXISTS field_filter_params HSTORE;