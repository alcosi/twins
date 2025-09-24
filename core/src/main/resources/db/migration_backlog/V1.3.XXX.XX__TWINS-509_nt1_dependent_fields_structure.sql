create table if not exists twin_class_field_condition_operator_type
(   id varchar(20) not null,
    constraint twin_class_field_condition_operator_type_pk primary key
    );

insert into twin_class_field_condition_operator_type (id)
values ('eq'),
       ('neq'),
       ('lt'),
       ('gt'),
       ('contains') on conflict do nothing;

create table if not exists twin_class_field_element_type
(   id varchar(20) not null,
    constraint twin_class_field_element_type_pk primary key
    );

insert into twin_class_field_element_type (id)
values ('value'),
       ('param') on conflict do nothing;

CREATE TABLE IF NOT EXISTS twin_class_field_rule (
    id UUID PRIMARY KEY,
    dependent_twin_class_field_id UUID NOT NULL REFERENCES twin_class_field(id) ON DELETE CASCADE,
    target_twin_class_field_element_type_id varchar(20)  not null default 'value'
    constraint twin_class_field_element_type_id_fk
    references twin_class_field_element_type
    on update cascade on delete restrict,
    target_param_key VARCHAR NULL,
    dependent_overwritten_value VARCHAR NULL,
    dependent_overwritten_datalist_id UUID NULL REFERENCES datalist(id) ON DELETE SET NULL,
    rule_priority INT NULL);

CREATE TABLE IF NOT EXISTS twin_class_field_condition (
    id UUID PRIMARY KEY,
    twin_class_field_rule_id UUID NOT NULL REFERENCES twin_class_field_rule(id) ON DELETE CASCADE,
    base_twin_class_field_id UUID NOT NULL REFERENCES twin_class_field(id) ON DELETE CASCADE,
    condition_order INT NULL,
    group_no INT NULL,
    twin_class_field_condition_operator_type_id varchar(20)  not null
    constraint twin_class_field_condition_operator_type_id_fk
    references twin_class_field_condition_operator_type
    on update cascade on delete restrict,
    condition_evaluator_featurer_id int NULL REFERENCES featurer(id) ON DELETE SET NULL,
    condition_evaluator_params hstore NULL);


/* 1. Rules that work with a PARAM → target_param_key participates
       in the uniqueness check                                     */
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS
    ux_twin_class_field_rule_param
    ON  twin_class_field_rule (
    dependent_twin_class_field_id,
    target_twin_class_field_element_type_id,
    target_param_key,
    dependent_overwritten_value,
    dependent_overwritten_datalist_id
    )
    WHERE target_twin_class_field_element_type_id = 'param';


/* 2. Rules that work with VALUE / other element types → target_param_key
       is ignored (can stay NULL or anything)                            */
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS
    ux_twin_class_field_rule_nonparam
    ON  twin_class_field_rule (
    dependent_twin_class_field_id,
    target_twin_class_field_element_type_id,
    dependent_overwritten_value,
    dependent_overwritten_datalist_id
    )
    WHERE target_twin_class_field_element_type_id <> 'param';

INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
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
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                ('00000000-0000-0004-0045-000000000001', 'TWIN_CLASS_FIELD_RULE_MANAGE', '00000000-0000-0000-0005-000000000001', 'abc12875-47a3-4e5b-a6ec-8c6e2a06f6f0', 'abc12875-9dbf-4c07-a871-f5c57df87f69'),
                ('00000000-0000-0004-0045-000000000002', 'TWIN_CLASS_FIELD_RULE_CREATE', '00000000-0000-0000-0005-000000000001', 'abc12875-4a3b-4e02-9c28-21f241377d33', 'abc12875-153e-4e57-99e6-35d5401c45a3'),
                ('00000000-0000-0004-0045-000000000003', 'TWIN_CLASS_FIELD_RULE_VIEW', '00000000-0000-0000-0005-000000000001', 'fbc12875-7984-4889-b3e7-34803f3c7ca0', 'fbc12875-9c85-4dbd-80ea-44fa3e4e1ee7'),
                ('00000000-0000-0004-0045-000000000002', 'TWIN_CLASS_FIELD_RULE_UPDATE', '00000000-0000-0000-0005-000000000001', 'abc12875-7984-4889-b3e7-34803f3c7ca0', 'abc12875-9c85-4dbd-80ea-44fa3e4e1ee7'),
                ('00000000-0000-0004-0045-000000000003', 'TWIN_CLASS_FIELD_RULE_DELETE', '00000000-0000-0000-0005-000000000001', 'abc12875-3a3e-48b6-81de-cfcd3ccf0f23', 'abc12875-0f28-4f5c-8ee8-eaf646efb6d9');

INSERT INTO public.featurer_type (id, name, description)
VALUES (45, 'ConditionEvaluator', '')
on conflict (id) do nothing;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
 VALUES (4501, 45, 'org.twins.core.featurer.conditionevaluator.ConditionEvaluatorBasic', '', 'Condition Evaluator Basic', false) on conflict do nothing;
