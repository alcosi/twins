UPDATE public.featurer_param SET key = 'minCount'::varchar(40), name = 'minCount'::varchar(40), description = 'Min count of attachments to field'::varchar(255), featurer_param_type_id = 'INT'::varchar(40) WHERE featurer_id = 1316::integer AND key LIKE 'multiple' ESCAPE '#';
INSERT INTO public.featurer_param (featurer_id, injectable, "order", key, name, description, featurer_param_type_id) VALUES (1316::integer, false::boolean, 1::integer, 'maxCount'::varchar(40), 'maxCount'::varchar(40), 'Max count of attachments to field'::varchar(255), 'INT'::varchar(40)) ON CONFLICT (featurer_id, key) DO NOTHING;
