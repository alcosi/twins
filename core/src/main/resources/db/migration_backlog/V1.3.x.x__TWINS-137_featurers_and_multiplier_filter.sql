
CREATE TABLE IF NOT EXISTS public.twin_factory_multiplier_filter (
                                                                     id uuid NOT NULL,
                                                                     twin_factory_multiplier_id uuid,
                                                                     twin_factory_condition_set_id uuid,
                                                                     twin_factory_condition_invert boolean,
                                                                     active boolean,
                                                                     comment character varying(255)
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

create index if not exists twin_factory_multiplier_filter_condition_set_id_index on twin_factory_multiplier_filter (twin_factory_condition_set_id);
create index if not exists twin_factory_multiplier_filter_multiplier_id_index on twin_factory_multiplier_filter (twin_factory_multiplier_id);

-- conditioner for check output twin field equals value
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2419, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerOutputTwinFieldValueEquals', 'ConditionerContextTwinFieldValueEquals', '', false) on conflict (id) do nothing;

-- multiplier for get twins by link
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2208, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedByLink', 'MultiplierIsolatedByLink', 'Output list of twin relatives for each input. Output twin list will be loaded by link and filtered by statusIds', false) on conflict (id) do nothing;

-- filler of twin basic fields
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2327, 23, 'org.twins.core.featurer.factory.filler.FillerTwinBasicFieldsFromContextBasics', 'FillerTwinBasicFieldsFromContextBasics', '', false) on conflict (id) do nothing;

-- multiplier create with selected class
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2209, 22, 'org.twins.core.featurer.factory.multiplier.MultiplierIsolatedCreate', 'MultiplierIsolatedCreate', 'New output twin for each input. Output class in param.', false) on conflict (id) do nothing;

-- conditioner checks context twin basic field equals context basics
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2420, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerContextTwinBasicFieldValueEqualsContextBasics', 'ConditionerContextTwinBasicFieldValueEqualsContextBasics', '', false) on conflict (id) do nothing;
