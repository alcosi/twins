INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2333, 23, 'org.twins.core.featurer.factory.filler.FillerFieldUserFromContextTwinBasicField', 'FillerFieldUserFromContextTwinBasicField', 'Fill the user field with assignee-or-creator of context twin', false) on conflict (id) do nothing;