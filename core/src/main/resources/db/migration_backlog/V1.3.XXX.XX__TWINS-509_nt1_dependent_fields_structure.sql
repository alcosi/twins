
CREATE TABLE IF NOT EXISTS twin_class_field_rule (
    id UUID PRIMARY KEY,
    twin_class_field_id   UUID NOT NULL REFERENCES twin_class_field (id) ON DELETE CASCADE,
    overwritten_value     VARCHAR NULL,
    overwritten_required  BOOLEAN NULL,
    field_overwriter_featurer_id int NOT NULL REFERENCES featurer(id) ON UPDATE CASCADE,
    field_overwriter_params hstore NULL,
    rule_priority INT NULL);

CREATE TABLE IF NOT EXISTS twin_class_field_condition (
    id UUID PRIMARY KEY,
    twin_class_field_rule_id UUID NOT NULL REFERENCES twin_class_field_rule(id) ON DELETE CASCADE,
    base_twin_class_field_id UUID NOT NULL REFERENCES twin_class_field(id) ON DELETE CASCADE,
    condition_order INT NULL,
    group_no INT NULL,
    condition_evaluator_featurer_id int NOT NULL REFERENCES featurer(id) ON UPDATE CASCADE,
    condition_evaluator_params hstore NULL);

CREATE UNIQUE INDEX IF NOT EXISTS uq_twin_class_field_rule_index
    ON twin_class_field_rule (
    twin_class_field_id,
    overwritten_value,
    overwritten_required,
    field_overwriter_featurer_id,
    field_overwriter_params
    );

INSERT INTO i18n (id,"name","key",i18n_type_id,domain_id) VALUES
                                                                     ('abc12875-47a3-4e5b-a6ec-8c6e2a06f6f0'::uuid,'Twin class field rule manage',NULL,'permissionName',NULL),
                                                                     ('abc12875-9dbf-4c07-a871-f5c57df87f69'::uuid,'Twin class field rule manage',NULL,'permissionDescription',NULL),
                                                                     ('abc12875-4a3b-4e02-9c28-21f241377d33'::uuid,'Twin class field rule create',NULL,'permissionName',NULL),
                                                                     ('abc12875-153e-4e57-99e6-35d5401c45a3'::uuid,'Twin class field rule create',NULL,'permissionDescription',NULL),
                                                                     ('abc12875-7984-4889-b3e7-34803f3c7ca0'::uuid,'Twin class field rule update',NULL,'permissionName',NULL),
                                                                     ('abc12875-9c85-4dbd-80ea-44fa3e4e1ee7'::uuid,'Twin class field rule update',NULL,'permissionDescription',NULL),
                                                                     ('fbc12875-7984-4889-b3e7-34803f3c7ca0'::uuid,'Twin class field rule view',NULL,'permissionName',NULL),
                                                                     ('fbc12875-9c85-4dbd-80ea-44fa3e4e1ee7'::uuid,'Twin class field rule view',NULL,'permissionDescription',NULL),
                                                                     ('abc12875-3a3e-48b6-81de-cfcd3ccf0f23'::uuid,'Twin class field rule delete',NULL,'permissionName',NULL),
                                                                     ('abc12875-0f28-4f5c-8ee8-eaf646efb6d9'::uuid,'Twin class field rule delete',NULL,'permissionDescription',NULL)
on conflict (id) do nothing;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                                                                                      ('abc12875-47a3-4e5b-a6ec-8c6e2a06f6f0', 'en', 'Twin class field rule manage', 0),
                                                                                      ('abc12875-9dbf-4c07-a871-f5c57df87f69', 'en', 'Twin class field rule manage', 0),
                                                                                      ('abc12875-4a3b-4e02-9c28-21f241377d33', 'en', 'Twin class field rule create', 0),
                                                                                      ('abc12875-153e-4e57-99e6-35d5401c45a3', 'en', 'Twin class field rule create', 0),
                                                                                      ('abc12875-7984-4889-b3e7-34803f3c7ca0', 'en', 'Twin class field rule update', 0),
                                                                                      ('abc12875-9c85-4dbd-80ea-44fa3e4e1ee7', 'en', 'Twin class field rule update', 0),
                                                                                      ('fbc12875-7984-4889-b3e7-34803f3c7ca0', 'en', 'Twin class field rule view', 0),
                                                                                      ('fbc12875-9c85-4dbd-80ea-44fa3e4e1ee7', 'en', 'Twin class field rule view', 0),
                                                                                      ('abc12875-3a3e-48b6-81de-cfcd3ccf0f23', 'en', 'Twin class field rule delete', 0),
                                                                                      ('abc12875-0f28-4f5c-8ee8-eaf646efb6d9', 'en', 'Twin class field rule delete', 0)
    ON CONFLICT (i18n_id, locale) DO UPDATE SET translation = excluded.translation, usage_counter = excluded.usage_counter;
INSERT INTO permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
        ('00000000-0000-0004-0047-000000000001', 'TWIN_CLASS_FIELD_RULE_MANAGE', '00000000-0000-0000-0005-000000000001', 'abc12875-47a3-4e5b-a6ec-8c6e2a06f6f0', 'abc12875-9dbf-4c07-a871-f5c57df87f69'),
        ('00000000-0000-0004-0047-000000000002', 'TWIN_CLASS_FIELD_RULE_CREATE', '00000000-0000-0000-0005-000000000001', 'abc12875-4a3b-4e02-9c28-21f241377d33', 'abc12875-153e-4e57-99e6-35d5401c45a3'),
        ('00000000-0000-0004-0047-000000000003', 'TWIN_CLASS_FIELD_RULE_VIEW', '00000000-0000-0000-0005-000000000001', 'fbc12875-7984-4889-b3e7-34803f3c7ca0', 'fbc12875-9c85-4dbd-80ea-44fa3e4e1ee7'),
        ('00000000-0000-0004-0047-000000000004', 'TWIN_CLASS_FIELD_RULE_UPDATE', '00000000-0000-0000-0005-000000000001', 'abc12875-7984-4889-b3e7-34803f3c7ca0', 'abc12875-9c85-4dbd-80ea-44fa3e4e1ee7'),
        ('00000000-0000-0004-0047-000000000005', 'TWIN_CLASS_FIELD_RULE_DELETE', '00000000-0000-0000-0005-000000000001', 'abc12875-3a3e-48b6-81de-cfcd3ccf0f23', 'abc12875-0f28-4f5c-8ee8-eaf646efb6d9')
    on conflict (id) do nothing;

INSERT INTO featurer_type (id, name, description)
VALUES (45, 'ConditionEvaluator', '')
on conflict (id) do nothing;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
 VALUES (4501, 45, 'org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluatorValue', '', 'Condition Evaluator Value', false) on conflict do nothing;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (4502, 45, 'org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluatorParam', '', 'Condition Evaluator Param', false) on conflict do nothing;


INSERT INTO featurer_type (id, name, description)
VALUES (46, 'FieldOverwriter', '')
    on conflict (id) do nothing;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (4601, 46, 'org.twins.core.featurer.fieldrule.fieldoverwriter.FieldParamOverwriterStub', '', 'FieldOverwriterStub', false) on conflict do nothing;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (4602, 46, 'org.twins.core.featurer.fieldrule.fieldoverwriter.FieldParamOverwriterSelect', '', 'FieldOverwriterSelect', false) on conflict do nothing;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (4603, 46, 'org.twins.core.featurer.fieldrule.fieldoverwriter.FieldParamOverwriterText', '', 'FieldOverwriterText', false) on conflict do nothing;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (4604, 46, 'org.twins.core.featurer.fieldrule.fieldoverwriter.FieldParamOverwriterNumeric', '', 'FieldOverwriterNumeric', false) on conflict do nothing;
