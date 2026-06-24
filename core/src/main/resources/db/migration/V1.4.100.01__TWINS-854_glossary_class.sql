-- TWINS-854: Glossary-as-Twins — bootstrap class schema.
-- Creates: TWINS_GLOSSARY TwinClass + 13 TwinClassFields + 2 TwinStatuses
--          (ACTUAL, DELETED) + 4 i18n rows (statuses) + 2 i18n rows (link names)
--          + 1 link type (GLOSSARY_SEE_ALSO) + 1 DataList (GLOSSARY_CATEGORY)
--          with 9 options.
-- Glossary Twins themselves are bootstrapped at app startup by GlossaryBootstrapService.
-- All UUIDs match constants in SystemEntityService (see ai/plans/glossary-as-twins.md §15.6).

-- 1. TwinClass: TWINS_GLOSSARY
--    UUID: SystemEntityService.TWIN_CLASS_TWINS_GLOSSARY
--    domain_id = null (system class)
INSERT INTO public.twin_class (
    id, domain_id, key, permission_schema_space, abstract,
    head_twin_class_id, extends_twin_class_id, name_i18n_id, description_i18n_id, logo,
    created_by_user_id, created_at, twin_class_owner_type_id,
    domain_alias_counter, marker_data_list_id, tag_data_list_id,
    twinflow_schema_space, twin_class_schema_space, alias_space,
    view_permission_id, head_hierarchy_tree, extends_hierarchy_tree,
    head_hunter_featurer_id, head_hunter_featurer_params,
    create_permission_id, edit_permission_id, delete_permission_id, page_face_id
) VALUES (
    '00000000-0000-0000-0001-000000000006'::uuid,
    null::uuid,
    'TWINS_GLOSSARY'::varchar,
    false::boolean, false::boolean,
    null::uuid, null::uuid, null::uuid, null::uuid, null::varchar,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    'system'::varchar,
    0::integer,
    null::uuid,
    '00000000-0000-0000-0020-000000000001'::uuid,
    false::boolean, false::boolean, false::boolean,
    null::uuid, null::ltree, null::ltree,
    null::integer, null::hstore,
    null::uuid, null::uuid, null::uuid, null::uuid
) ON CONFLICT (id) DO NOTHING;

-- 2. i18n entries — 4 rows for statuses + 2 rows for GLOSSARY_SEE_ALSO link names
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES
    ('00000000-0000-0000-0012-000000000039'::uuid, null, null, 'twinStatusName'::varchar,       null::uuid),
    ('00000000-0000-0000-0012-000000000040'::uuid, null, null, 'twinStatusDescription'::varchar, null::uuid),
    ('00000000-0000-0000-0012-000000000041'::uuid, null, null, 'twinStatusName'::varchar,       null::uuid),
    ('00000000-0000-0000-0012-000000000042'::uuid, null, null, 'twinStatusDescription'::varchar, null::uuid),
    ('00000000-0000-0000-0012-000000000043'::uuid, null, null, 'linkForwardName'::varchar,      null::uuid),
    ('00000000-0000-0000-0012-000000000044'::uuid, null, null, 'linkBackwardName'::varchar,     null::uuid)
ON CONFLICT (id) DO NOTHING;

-- 3. i18n English translations
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('00000000-0000-0000-0012-000000000039'::uuid, 'en', 'Actual',                                                                          0),
    ('00000000-0000-0000-0012-000000000040'::uuid, 'en', 'Glossary entry is in sync with its markdown source file',                              0),
    ('00000000-0000-0000-0012-000000000041'::uuid, 'en', 'Deleted',                                                                          0),
    ('00000000-0000-0000-0012-000000000042'::uuid, 'en', 'Source markdown file removed; Twin retained for referential integrity',                0),
    ('00000000-0000-0000-0012-000000000043'::uuid, 'en', 'See also',                                                                         0),
    ('00000000-0000-0000-0012-000000000044'::uuid, 'en', 'Referenced by',                                                                    0)
ON CONFLICT (i18n_id, locale) DO NOTHING;

-- 4. TwinStatus: ACTUAL + DELETED
INSERT INTO public.twin_status (id, twins_class_id, name_i18n_id, description_i18n_id, logo, background_color, key, font_color) VALUES
    ('00000000-0000-0000-0003-000000001001'::uuid,
     '00000000-0000-0000-0001-000000000006'::uuid,
     '00000000-0000-0000-0012-000000000039'::uuid,
     '00000000-0000-0000-0012-000000000040'::uuid,
     null::varchar, null::varchar, 'ACTUAL'::varchar, null::varchar),
    ('00000000-0000-0000-0003-000000001002'::uuid,
     '00000000-0000-0000-0001-000000000006'::uuid,
     '00000000-0000-0000-0012-000000000041'::uuid,
     '00000000-0000-0000-0012-000000000042'::uuid,
     null::varchar, null::varchar, 'DELETED'::varchar, null::varchar)
ON CONFLICT (id) DO NOTHING;

-- 5. TwinClassFields — 13 fields per plan §4.
--    Field typer featurer IDs:
--      1301 = FieldTyperTextField (indexed, short searchable)
--      1336 = FieldTyperTextNonIndexedField (long markdown bodies)
--      1306 = FieldTyperBooleanV1
--      1302 = FieldTyperTimestamp
--    name_i18n_id and description_i18n_id left null (field keys are self-describing).
--    NOTE: ## Summary section is stored on Twin.description (base field via GLOBAL_ANCESTOR
--    inheritance) — no TwinClassField for it.

-- 5a. Long-text section fields (non-indexed, featurer 1336)
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES
    ('00000000-0000-0000-0011-000000000050'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'purpose'::varchar,            null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000000051'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'fields'::varchar,             null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, true),
    ('00000000-0000-0000-0011-000000000052'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'relations_overview'::varchar, null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000000053'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'api'::varchar,                null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000000054'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'api_deprecated'::varchar,     null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000000055'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'examples'::varchar,           null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000000056'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'dev_notes'::varchar,          null::uuid, null::uuid, 1336, null::hstore, null::uuid, null::uuid, false)
ON CONFLICT (id) DO NOTHING;

-- 5b. Short indexed-text fields (featurer 1301)
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES
    ('00000000-0000-0000-0011-000000000057'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'jpa_class'::varchar,       null::uuid, null::uuid, 1301, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000000058'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'db_table'::varchar,        null::uuid, null::uuid, 1301, null::hstore, null::uuid, null::uuid, false),
    ('00000000-0000-0000-0011-000000000059'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'markdown_source'::varchar, null::uuid, null::uuid, 1301, null::hstore, null::uuid, null::uuid, true),
    ('00000000-0000-0000-0011-000000000060'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'markdown_hash'::varchar,   null::uuid, null::uuid, 1301, null::hstore, null::uuid, null::uuid, true)
ON CONFLICT (id) DO NOTHING;

-- 5c. Boolean + date fields
INSERT INTO public.twin_class_field (id, twin_class_id, key, name_i18n_id, description_i18n_id, field_typer_featurer_id, field_typer_params, view_permission_id, edit_permission_id, required) VALUES
    ('00000000-0000-0000-0011-000000000061'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'is_system'::varchar,     null::uuid, null::uuid, 1306, null::hstore, null::uuid, null::uuid, true),
    ('00000000-0000-0000-0011-000000000062'::uuid, '00000000-0000-0000-0001-000000000006'::uuid, 'actualized_at'::varchar, null::uuid, null::uuid, 1302, null::hstore, null::uuid, null::uuid, true)
ON CONFLICT (id) DO NOTHING;

-- 6. Link type: GLOSSARY_SEE_ALSO (glossary Twin → glossary Twin, many-to-many)
--    UUID: SystemEntityService.LINK_GLOSSARY_SEE_ALSO
INSERT INTO public.link (
    id, domain_id, src_twin_class_id, dst_twin_class_id,
    forward_name_i18n_id, backward_name_i18n_id, link_type_id,
    link_strength_id, created_by_user_id, created_at
) VALUES (
    '00000000-0000-0000-0019-000000000001'::uuid,
    null::uuid,
    '00000000-0000-0000-0001-000000000006'::uuid,
    '00000000-0000-0000-0001-000000000006'::uuid,
    '00000000-0000-0000-0012-000000000043'::uuid,
    '00000000-0000-0000-0012-000000000044'::uuid,
    'ManyToMany'::varchar,
    'OPTIONAL'::varchar,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- 7. DataList: GLOSSARY_CATEGORY (TwinTag source for categorizing glossary Twins)
INSERT INTO public.data_list (id, name, description, updated_at) VALUES
    ('00000000-0000-0000-0020-000000000001'::uuid, 'GLOSSARY_CATEGORY', 'Categories for glossary entries', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.data_list_option (id, data_list_id, option, data_list_option_status_id, "order") VALUES
    ('00000000-0000-0020-0001-000000000001'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'core',            'active', 1),
    ('00000000-0000-0020-0001-000000000002'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'workflow',        'active', 2),
    ('00000000-0000-0020-0001-000000000003'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'multi-tenancy',   'active', 3),
    ('00000000-0000-0020-0001-000000000004'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'permissions',     'active', 4),
    ('00000000-0000-0020-0001-000000000005'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'content',         'active', 5),
    ('00000000-0000-0020-0001-000000000006'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'cross-cutting',   'active', 6),
    ('00000000-0000-0020-0001-000000000007'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'fields',          'active', 7),
    ('00000000-0000-0020-0001-000000000008'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'validation',      'active', 8),
    ('00000000-0000-0020-0001-000000000009'::uuid, '00000000-0000-0000-0020-000000000001'::uuid, 'other',           'active', 9)
ON CONFLICT (id) DO NOTHING;

-- 8. Index FK columns (CLAUDE.md rule)
CREATE INDEX IF NOT EXISTS idx_twin_class_field_twin_class_id
    ON public.twin_class_field (twin_class_id);
CREATE INDEX IF NOT EXISTS idx_twin_status_twins_class_id
    ON public.twin_status (twins_class_id);
CREATE INDEX IF NOT EXISTS idx_link_src_twin_class_id
    ON public.link (src_twin_class_id);
CREATE INDEX IF NOT EXISTS idx_link_dst_twin_class_id
    ON public.link (dst_twin_class_id);
CREATE INDEX IF NOT EXISTS idx_data_list_option_data_list_id
    ON public.data_list_option (data_list_id);
