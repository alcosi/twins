INSERT INTO public.featurer_param_type (id, regexp, example, description) VALUES ('DOUBLE'::varchar(40), '^-?\d+(\.\d+)?$'::varchar(255), '108.84'::varchar(255), 'any number'::varchar(255)) ON CONFLICT (id) DO NOTHING;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description) VALUES (1317::integer, 13::integer, 'org.twins.core.featurer.fieldtyper.FieldTyperNumeric'::varchar, 'FieldTyperNumeric'::varchar, 'Numeric field'::varchar(255)) ON CONFLICT (id) DO NOTHING;