-- TWINS-932: error table rows + system i18n for DTO bean validation failures
-- handled by ValidationExceptionHandlingAdvice

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('00000000-0000-0000-0012-0000000011d2', null, null, 'error') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-0000000011d2', 'en', 'Some of the request fields are invalid', 0) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('00000000-0000-0000-0012-0000000011d3', null, null, 'error') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('00000000-0000-0000-0012-0000000011d3', 'en', 'Request body is malformed or unreadable', 0) on conflict on constraint i18n_translation_uq do nothing ;

INSERT INTO public.error (id, code_local, code_external, name, description, client_msg_i18n_id) VALUES ('9a7eed33-6e80-4484-af20-75a92557c5d7', 13701, 'null', 'VALIDATION_DTO_FAILED', 'dto bean validation failed', '00000000-0000-0000-0012-0000000011d2') on conflict on constraint error_pk do nothing ;
INSERT INTO public.error (id, code_local, code_external, name, description, client_msg_i18n_id) VALUES ('d4f30431-1c88-4ced-97f3-16d450c08c19', 13702, 'null', 'MALFORMED_REQUEST_BODY', 'request body is malformed', '00000000-0000-0000-0012-0000000011d3') on conflict on constraint error_pk do nothing ;
