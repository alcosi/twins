-- TWINS-727: user defined order (drag and drop) via lexicographic sort keys
-- twin.sort_order        - order of siblings within one head_twin_id
-- twin_link.sort_order   - order of src twins within one (dst_twin_id, link_id)
-- COLLATE "C" - byte-wise comparison is required by the key generator (ASCII alphabet)
ALTER TABLE twin ADD COLUMN IF NOT EXISTS sort_order varchar(255) COLLATE "C";
ALTER TABLE twin_link ADD COLUMN IF NOT EXISTS sort_order varchar(255) COLLATE "C";

-- sorting index (head_twin_id, sort_order) / (dst_twin_id, link_id, sort_order);
-- deliberately NOT unique: level renumbering (TwinSortOrderService) rewrites sibling keys in one
-- flush, a non-deferrable unique index fails on rotated keys mid-flush; writers of one level are
-- serialized by pg_advisory_xact_lock, duplicate keys are harmless (queries tie-break by createdAt/id)
CREATE INDEX IF NOT EXISTS twin_head_twin_id_sort_order_index ON twin (head_twin_id, sort_order);
CREATE INDEX IF NOT EXISTS twin_link_dst_twin_id_link_id_sort_order_index ON twin_link (dst_twin_id, link_id, sort_order);

-- base field seed (GLOBAL_ANCESTOR class) so clients can reference sort_order as a twin_class_field;
-- typer 1321 (BaseTextField), twin sorter 4107, initializer 5301 - same as other base fields
INSERT INTO twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, twin_sorter_featurer_id, field_initializer_featurer_id, view_permission_id, edit_permission_id, required)
VALUES ('00000000-0000-0000-0011-000000000018'::uuid, '00000000-0000-0000-0001-000000000004'::uuid, 'base_sort_order'::varchar(100), null::uuid, null::uuid, 1321::integer, null::hstore, 4107::integer, 5301::integer, null::uuid, null::uuid, false::boolean)
ON CONFLICT (id) DO NOTHING;
