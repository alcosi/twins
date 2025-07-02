INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('0f15018f-c17a-4756-b742-6bc9ec8d38a1', null, null, 'twinStatusName', null) on conflict do nothing;
INSERT INTO public.i18n (id, name, key, i18n_type_id, domain_id) VALUES ('8c838b93-d866-45ae-b428-c7660a03c5ea', null, null, 'twinStatusDescription', null) on conflict do nothing;

INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('0f15018f-c17a-4756-b742-6bc9ec8d38a1', 'en', 'Sketch', 0) on conflict do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('8c838b93-d866-45ae-b428-c7660a03c5ea', 'en', '', 0) on conflict do nothing;

INSERT INTO public.twin_status (id, twins_class_id, name_i18n_id, description_i18n_id, logo, background_color, key, font_color) VALUES ('38120cb0-5611-4839-8b40-7c1f27142b8e', '00000000-0000-0000-0001-000000000004', '0f15018f-c17a-4756-b742-6bc9ec8d38a1', '8c838b93-d866-45ae-b428-c7660a03c5ea', null, '#000000', 'sketch', '#000000') on conflict do nothing;
