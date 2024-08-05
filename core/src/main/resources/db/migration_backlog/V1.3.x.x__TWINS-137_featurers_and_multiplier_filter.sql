
CREATE TABLE IF NOT EXISTS public.twin_factory_multiplier_filter (
                                                                     id uuid NOT NULL,
                                                                     comment character varying(255),
                                                                     active boolean,
                                                                     twin_factory_condition_invert boolean,
                                                                     twin_factory_condition_set_id uuid,
                                                                     twin_factory_multiplier_id uuid
);

alter table public.twin_factory_multiplier_filter drop constraint if exists twin_factory_multiplier_filter_pkey;
alter table public.twin_factory_multiplier_filter drop constraint if exists twin_factory_multiplier_filter_twin_factory_condition_set_id_fk;
alter table public.twin_factory_multiplier_filter drop constraint if exists twin_factory_multiplier_filter_twin_factory_multiplier_id_fk;

ALTER TABLE ONLY public.twin_factory_multiplier_filter
    ADD CONSTRAINT twin_factory_multiplier_filter_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.twin_factory_multiplier_filter
    ADD CONSTRAINT twin_factory_multiplier_filter_twin_factory_condition_set_id_fk FOREIGN KEY (twin_factory_condition_set_id) REFERENCES public.twin_factory_condition_set(id);

ALTER TABLE ONLY public.twin_factory_multiplier_filter
    ADD CONSTRAINT twin_factory_multiplier_filter_twin_factory_multiplier_id_fk FOREIGN KEY (twin_factory_multiplier_id) REFERENCES public.twin_factory_multiplier(id);


-- conditioner for check output twin field equals value
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2419, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerOutputTwinFieldValueEquals', 'ConditionerContextTwinFieldValueEquals', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (2419, false, 1, 'value', 'value', '', 'STRING') on conflict (featurer_id, key) do nothing;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (2419, false, 1, 'twinClassFieldId', 'twinClassFieldId', '', 'UUID:TWINS_TWIN_CLASS_FIELD_ID') on conflict (featurer_id, key) do nothing;


-- multiplier for get twins by link
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2208, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedRelativesByLink', 'MultiplierIsolatedRelativesByLink', 'Output list of twin relatives for each input. Output twin list will be loaded by link and filtered by statusIds', false) on conflict (id) do nothing;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (2208, false, 1, 'linkId', 'linkId', 'Link from sought twin to factory input twin', 'UUID:TWINS_LINK_ID') on conflict (featurer_id, key) do nothing;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (2208, false, 1, 'statusIds', 'statusIds', 'Statuses of src(fwd) linked twin. If empty - twins with any status will be found', 'UUID_SET:TWINS_TWIN_STATUS_ID') on conflict (featurer_id, key) do nothing;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (2208, false, 1, 'excludeStatuses', 'excludeStatues', 'Exclude(true)/Include(false) child Twin.Status.IDs from query result', 'BOOLEAN') on conflict (featurer_id, key) do nothing;

-- filler of twin basic fields
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2327, 23, 'org.twins.core.featurer.factory.filler.FillerTwinBasicFieldsFromContextBasics', 'FillerTwinBasicFieldsFromContextBasics', '', false) on conflict (id) do nothing;

-- multiplier create with selected class
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2209, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedCreate', 'MultiplierIsolatedCreate', 'New output twin for each input. Output class in param.', false) on conflict (id) do nothing;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (2209, false, 1, 'twinClassId', 'twinClassId', '', 'UUID:TWINS_TWIN_CLASS_ID') on conflict (featurer_id, key) do nothing;

-- conditioner checks context twin basic field equals context basics
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2420, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextTwinBasicFieldValueEqualsContextBasics', 'ConditionerContextTwinBasicFieldValueEqualsContextBasics', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (2420, false, 1, 'fields', 'fields', 'List of basic fields to check(divider ,)', 'STRING') on conflict (featurer_id, key) do nothing;
