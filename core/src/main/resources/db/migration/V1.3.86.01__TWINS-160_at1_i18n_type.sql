-- creating i18n_type for errors
INSERT INTO public.i18n_type (id, name) VALUES ('error', 'Error') on conflict on constraint i18n_type_pk do nothing ;