-- Register sorter featurers (stub 4101, simple 4102, boolean 4103)
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (4101, 41, '', '', '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (4102, 41, '', '', '', false) ON CONFLICT (id) DO NOTHING;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (4103, 41, '', '', '', false) ON CONFLICT (id) DO NOTHING;

ALTER TABLE IF EXISTS twin_class_field
    ADD COLUMN IF NOT EXISTS twin_sorter_featurer_id int,
    ADD COLUMN IF NOT EXISTS twin_sorter_params hstore;

-- Auto-assign sorter featurer per field type
-- Text (1301), Date (1302), Numeric (1317) -> Simple fields sorter 4102
UPDATE twin_class_field
SET twin_sorter_featurer_id = 4102
WHERE twin_sorter_featurer_id IS NULL
  AND field_typer_featurer_id IN (1301, 1302, 1317);

-- Boolean (1306) -> Boolean fields sorter 4103
UPDATE twin_class_field
SET twin_sorter_featurer_id = 4103
WHERE twin_sorter_featurer_id IS NULL
  AND field_typer_featurer_id IN (1306);

-- All other field types -> Stub sorter 4101
UPDATE twin_class_field
SET twin_sorter_featurer_id = 4101
WHERE twin_sorter_featurer_id IS NULL;

CREATE TABLE IF NOT EXISTS twin_search_sort
(
    id UUID PRIMARY KEY,
    twin_search_id UUID NOT NULL,
    "order" INT,
    twin_class_field__id UUID,
    direction TEXT,
    CONSTRAINT twin_search_sort_twin_search_id_fk FOREIGN KEY (twin_search_id) REFERENCES twin_search (id) ON DELETE CASCADE,
    CONSTRAINT twin_search_sort_tcf_id_fk FOREIGN KEY (twin_class_field__id) REFERENCES twin_class_field (id)
);

CREATE INDEX IF NOT EXISTS twin_search_sort_search_id_idx ON twin_search_sort (twin_search_id);
CREATE INDEX IF NOT EXISTS twin_search_sort_order_idx ON twin_search_sort ("order");
CREATE INDEX IF NOT EXISTS twin_search_sort_tcf_id_idx ON twin_search_sort (twin_class_field__id);

alter table public.twin_search drop column if exists twin_sorter_featurer_id;
alter table public.twin_search drop column if exists twin_sorter_params;
