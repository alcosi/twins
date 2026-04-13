alter table twinflow_transition_alias
    alter column domain_id drop not null;

-- Insert featurer for Decimal Increment
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (1350::integer, 13::integer, 'org.twins.core.featurer.fieldtyper.FieldTyperDecimalIncrement'::varchar, 'Decimal Increment'::varchar, 'Decimal field with atomic increment/decrement support (+N/-N format)'::varchar(255), false::boolean)
    on conflict do nothing;

-- Add job_twin_class_id column to twin_trigger table
ALTER TABLE twin_trigger
ADD COLUMN IF NOT EXISTS job_twin_class_id UUID REFERENCES twin_class(id);
