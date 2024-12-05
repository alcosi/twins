INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2428, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerMathCompareOutputTwinFieldValueAndContextTwinTwinFieldValue', 'ConditionerMathCompareOutputTwinFieldValueAndContextTwinTwinFieldValue', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2429, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerMathCompareChildrenTwinFieldValueWithParentTwinFieldValue', 'ConditionerMathCompareChildrenTwinFieldValueWithParentTwinFieldValue', '', false) on conflict (id) do nothing;
alter table public.twin_factory_multiplier_filter drop constraint if exists twin_factory_multiplier_filter_input_twin_class_id_fk;
alter table public.twin_factory_multiplier_filter add column if not exists input_twin_class_id uuid;
alter table public.twin_factory_multiplier_filter
    add constraint twin_factory_multiplier_filter_input_twin_class_id_fk
        foreign key (input_twin_class_id) references public.twin_class
            on update cascade on delete cascade;


ALTER TABLE public.twin_factory_multiplier_filter RENAME TO twin_factory_multiplier_filter_old;
CREATE TABLE public.twin_factory_multiplier_filter (
         id UUID PRIMARY KEY,
         input_twin_class_id UUID,
         twin_factory_multiplier_id UUID NOT NULL,
         twin_factory_condition_set_id UUID NOT NULL,
         twin_factory_condition_invert BOOLEAN NOT NULL,
         active BOOLEAN NOT NULL,
         comment VARCHAR(255),
         CONSTRAINT twin_factory_multiplier_filter_input_twin_class_id_fk FOREIGN KEY (input_twin_class_id)
             REFERENCES twin_class (id),
         CONSTRAINT twin_factory_multiplier_filter_twin_factory_multiplier_id_fk FOREIGN KEY (twin_factory_multiplier_id)
             REFERENCES twin_factory_multiplier (id),
         CONSTRAINT twin_factory_multiplier_filter_twin_factory_condition_set_id_fk FOREIGN KEY (twin_factory_condition_set_id)
             REFERENCES twin_factory_condition_set (id)
);

INSERT INTO public.twin_factory_multiplier_filter (
    id,
    twin_factory_multiplier_id,
    twin_factory_condition_set_id,
    twin_factory_condition_invert,
    active,
    comment
)
SELECT
    id,
    twin_factory_multiplier_id,
    twin_factory_condition_set_id,
    twin_factory_condition_invert,
    active,
    comment
FROM public.twin_factory_multiplier_filter_old;
DROP TABLE public.twin_factory_multiplier_filter_old;

CREATE INDEX twin_factory_multiplier_filter_condition_set_id_index
    ON public.twin_factory_multiplier_filter (twin_factory_condition_set_id);

CREATE INDEX twin_factory_multiplier_filter_multiplier_id_index
    ON public.twin_factory_multiplier_filter (twin_factory_multiplier_id);
