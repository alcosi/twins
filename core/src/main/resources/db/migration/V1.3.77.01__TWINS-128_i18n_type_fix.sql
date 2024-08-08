INSERT INTO public.i18n_type (id, name) VALUES ('twinflowTransitionDescription'::varchar, 'Twinflow transition description'::varchar(255)) on conflict (id) do nothing;
