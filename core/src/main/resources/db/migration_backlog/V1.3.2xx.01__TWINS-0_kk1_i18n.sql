INSERT INTO public.i18n_type VALUES ('dataListName', 'Data list name') on conflict(id) do nothing
INSERT INTO public.i18n_type VALUES ('dataListDescription', 'Data list description') on conflict(id) do nothing;
INSERT INTO public.i18n_type VALUES ('dataListAttributeName', 'Data list attribute name') on conflict(id) do nothing;