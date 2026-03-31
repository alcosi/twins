INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (1350::integer, 13::integer, 'org.twins.core.featurer.fieldtyper.FieldTyperDecimalIncrement'::varchar, 'Decimal Increment'::varchar, 'Decimal field with atomic increment/decrement support (+N/-N format)'::varchar(255), false::boolean)
on conflict do nothing;

-- Add job_twin_class_id column to twin_trigger
ALTER TABLE public.twin_trigger
ADD COLUMN job_twin_class_id UUID REFERENCES public.twin_class(id);

-- Add job_twin_id column to twin_trigger_task
ALTER TABLE public.twin_trigger_task
ADD COLUMN job_twin_id UUID REFERENCES public.twin(id);
