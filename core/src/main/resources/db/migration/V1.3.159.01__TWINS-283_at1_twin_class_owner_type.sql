alter table public.twin_class_owner_type
    add if not exists name_i18n_id uuid;

alter table public.twin_class_owner_type
    add if not exists description_i18n_id uuid;

create table if not exists domain_type_twin_class_owner_type
(
    domain_type_id varchar not null
        constraint domain_type_twin_class_owner_type_domain_type_id_fk
            references domain_type,
    twin_class_owner_type_id varchar not null
        constraint domain_type_twin_class_owner_type_twin_class_owner_type_id_fk
            references twin_class_owner_type,
        constraint domain_type_twin_class_owner_type_pk
    primary key (domain_type_id, twin_class_owner_type_id)
);

INSERT INTO public.domain_type_twin_class_owner_type (domain_type_id, twin_class_owner_type_id) VALUES ('basic', 'businessAccount') on conflict on constraint domain_type_twin_class_owner_type_pk do nothing ;
INSERT INTO public.domain_type_twin_class_owner_type (domain_type_id, twin_class_owner_type_id) VALUES ('basic', 'user') on conflict on constraint domain_type_twin_class_owner_type_pk do nothing ;
INSERT INTO public.domain_type_twin_class_owner_type (domain_type_id, twin_class_owner_type_id) VALUES ('b2b', 'domain') on conflict on constraint domain_type_twin_class_owner_type_pk do nothing ;
INSERT INTO public.domain_type_twin_class_owner_type (domain_type_id, twin_class_owner_type_id) VALUES ('b2b', 'domainBusinessAccount') on conflict on constraint domain_type_twin_class_owner_type_pk do nothing ;
INSERT INTO public.domain_type_twin_class_owner_type (domain_type_id, twin_class_owner_type_id) VALUES ('b2b', 'domainUser') on conflict on constraint domain_type_twin_class_owner_type_pk do nothing ;

INSERT INTO public.i18n_type (id, name) VALUES ('twinClassOwnerTypeName', 'Twin class owner type name') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO public.i18n_type (id, name) VALUES ('twinClassOwnerTypeDescription', 'Twin class owner type description') on conflict on constraint i18n_type_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('5eb1e197-dad6-4be6-ae7b-d35ac39c3fee', null, null, 'twinClassOwnerTypeName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('c6f32c7c-c577-4318-80a8-5413fe8e4b5c', null, null, 'twinClassOwnerTypeName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('6a01716a-89ea-4bec-a903-ea6b30455579', null, null, 'twinClassOwnerTypeName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('c6a51f5c-175e-4786-bc79-c5eec034d218', null, null, 'twinClassOwnerTypeName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('0a2f2f42-6e38-4cfd-a438-21d151fdff5f', null, null, 'twinClassOwnerTypeName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('4c76435e-0d35-443d-812e-9bafa78c6f85', null, null, 'twinClassOwnerTypeName') on conflict on constraint i18n_pkey do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('792b6ccd-e9f2-4650-9fce-9c4e3709dd60', null, null, 'twinClassOwnerTypeDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('8ad43193-403d-44ab-88bc-5ae458b8b8af', null, null, 'twinClassOwnerTypeDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('7f09ccb5-cb5d-475f-b228-978191134c37', null, null, 'twinClassOwnerTypeDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('b41961bd-fb6d-4d08-b51b-e314ce48557f', null, null, 'twinClassOwnerTypeDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('cec891ef-c028-4e98-9ac7-0fb8226c4e3f', null, null, 'twinClassOwnerTypeDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('92587f99-1693-4370-835c-e7f7e9224353', null, null, 'twinClassOwnerTypeDescription') on conflict on constraint i18n_pkey do nothing ;

UPDATE public.twin_class_owner_type SET name_i18n_id = '0a2f2f42-6e38-4cfd-a438-21d151fdff5f'::uuid, description_i18n_id = 'cec891ef-c028-4e98-9ac7-0fb8226c4e3f'::uuid WHERE id LIKE 'businessAccount';
UPDATE public.twin_class_owner_type SET name_i18n_id = '5eb1e197-dad6-4be6-ae7b-d35ac39c3fee'::uuid, description_i18n_id = '792b6ccd-e9f2-4650-9fce-9c4e3709dd60'::uuid WHERE id LIKE 'domainUser';
UPDATE public.twin_class_owner_type SET name_i18n_id = '6a01716a-89ea-4bec-a903-ea6b30455579'::uuid, description_i18n_id = '7f09ccb5-cb5d-475f-b228-978191134c37'::uuid WHERE id LIKE 'domain';
UPDATE public.twin_class_owner_type SET name_i18n_id = '4c76435e-0d35-443d-812e-9bafa78c6f85'::uuid, description_i18n_id = '92587f99-1693-4370-835c-e7f7e9224353'::uuid WHERE id LIKE 'system';
UPDATE public.twin_class_owner_type SET name_i18n_id = 'c6f32c7c-c577-4318-80a8-5413fe8e4b5c'::uuid, description_i18n_id = '8ad43193-403d-44ab-88bc-5ae458b8b8af'::uuid WHERE id LIKE 'domainBusinessAccount';
UPDATE public.twin_class_owner_type SET name_i18n_id = 'c6a51f5c-175e-4786-bc79-c5eec034d218'::uuid, description_i18n_id = 'b41961bd-fb6d-4d08-b51b-e314ce48557f'::uuid WHERE id LIKE 'user';

INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('0a2f2f42-6e38-4cfd-a438-21d151fdff5f', 'en', 'Business account', 0) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('5eb1e197-dad6-4be6-ae7b-d35ac39c3fee', 'en', 'Domain user', 0) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('6a01716a-89ea-4bec-a903-ea6b30455579', 'en', 'Domain', 0) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('4c76435e-0d35-443d-812e-9bafa78c6f85', 'en', 'System', 0) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('c6f32c7c-c577-4318-80a8-5413fe8e4b5c', 'en', 'Domain business account', 0) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('c6a51f5c-175e-4786-bc79-c5eec034d218', 'en', 'User', 0) on conflict on constraint i18n_translation_uq do nothing ;

