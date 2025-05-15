--Skipping DENY_ALL
--Skipping TWIN_CLASS_MANAGE
--Skipping TRANSITION_MANAGE
--Skipping LINK_MANAGE
--Skipping DOMAIN_TWINS_VIEW_ALL
--Skipping DOMAIN_MANAGE
--Skipping TWIN_CLASS_FIELD_MANAGE
--Skipping TWIN_STATUS_MANAGE
--Skipping TWIN_MANAGE
--Skipping COMMENT_MANAGE
--Skipping ATTACHMENT_MANAGE
--Skipping USER_MANAGE
--Skipping USER_GROUP_MANAGE
--Skipping DATA_LIST_MANAGE
--Skipping DATA_LIST_OPTION_MANAGE
--Skipping DATA_LIST_SUBSET_MANAGE
--Skipping PERMISSION_MANAGE
--Skipping PERMISSION_GROUP_MANAGE
--Skipping PERMISSION_SCHEMA_MANAGE
--Skipping FACTORY_MANAGE
--Skipping MULTIPLIER_MANAGE
--Skipping MULTIPLIER_PARAM_MANAGE
--Skipping PIPELINE_MANAGE
--Skipping PIPELINE_STEP_MANAGE
--Skipping BRANCH_MANAGE
--Skipping ERASER_MANAGE
--Skipping CONDITION_SET_MANAGE
--Skipping TWINFLOW_MANAGE
--Skipping TWINFLOW_SCHEMA_MANAGE
--Skipping BUSINESS_ACCOUNT_MANAGE
--Skipping TIER_MANAGE
--Skipping FEATURER_MANAGE
--Skipping DOMAIN_TWINS_CREATE_ANY


--Start TWIN_CLASS_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('5a455b95-95ec-34e9-acf8-d668c0014ded', 'Twin class create name', null, 'permissionName'),
       ('61afee4c-08b1-39d3-8bd3-7df286a08905', 'Twin class create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('5a455b95-95ec-34e9-acf8-d668c0014ded', 'en', 'Twin class create', 0),
       ('61afee4c-08b1-39d3-8bd3-7df286a08905', 'en', 'Twin class create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000034', 'TWIN_CLASS_CREATE', '00000000-0000-0000-0005-000000000001',
        '5a455b95-95ec-34e9-acf8-d668c0014ded', '61afee4c-08b1-39d3-8bd3-7df286a08905')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000034', '00000000-0000-0000-0004-000000000034',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_CLASS_CREATE


--Start TWIN_CLASS_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('6ff6c224-1d8c-3dd4-8b93-2019043470dd', 'Twin class update name', null, 'permissionName'),
       ('16312093-5591-3806-a017-415bc398691c', 'Twin class update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('6ff6c224-1d8c-3dd4-8b93-2019043470dd', 'en', 'Twin class update', 0),
       ('16312093-5591-3806-a017-415bc398691c', 'en', 'Twin class update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000035', 'TWIN_CLASS_UPDATE', '00000000-0000-0000-0005-000000000001',
        '6ff6c224-1d8c-3dd4-8b93-2019043470dd', '16312093-5591-3806-a017-415bc398691c')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000035', '00000000-0000-0000-0004-000000000035',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_CLASS_UPDATE


--Start TWIN_CLASS_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ebcfac33-bf4d-3591-9682-bcf1a34d79fe', 'Twin class delete name', null, 'permissionName'),
       ('b43111eb-8677-3e33-8d11-0fbb0e2afbe1', 'Twin class delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ebcfac33-bf4d-3591-9682-bcf1a34d79fe', 'en', 'Twin class delete', 0),
       ('b43111eb-8677-3e33-8d11-0fbb0e2afbe1', 'en', 'Twin class delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000036', 'TWIN_CLASS_DELETE', '00000000-0000-0000-0005-000000000001',
        'ebcfac33-bf4d-3591-9682-bcf1a34d79fe', 'b43111eb-8677-3e33-8d11-0fbb0e2afbe1')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000036', '00000000-0000-0000-0004-000000000036',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_CLASS_DELETE


--Start TWIN_CLASS_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('675d9db8-4c76-3eae-aa38-27d6961a6b80', 'Twin class view name', null, 'permissionName'),
       ('2a31726e-144d-333d-949c-d5469a2e193a', 'Twin class view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('675d9db8-4c76-3eae-aa38-27d6961a6b80', 'en', 'Twin class view', 0),
       ('2a31726e-144d-333d-949c-d5469a2e193a', 'en', 'Twin class view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000037', 'TWIN_CLASS_VIEW', '00000000-0000-0000-0005-000000000001',
        '675d9db8-4c76-3eae-aa38-27d6961a6b80', '2a31726e-144d-333d-949c-d5469a2e193a')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000037', '00000000-0000-0000-0004-000000000037',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_CLASS_VIEW


--Start TRANSITION_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('c244c094-16f1-3635-b9b7-decaede14238', 'Transition view name', null, 'permissionName'),
       ('0f0af10b-8257-33a4-8acc-514fc2ee7dd5', 'Transition view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('c244c094-16f1-3635-b9b7-decaede14238', 'en', 'Transition view', 0),
       ('0f0af10b-8257-33a4-8acc-514fc2ee7dd5', 'en', 'Transition view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000038', 'TRANSITION_VIEW', '00000000-0000-0000-0005-000000000001',
        'c244c094-16f1-3635-b9b7-decaede14238', '0f0af10b-8257-33a4-8acc-514fc2ee7dd5')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000038', '00000000-0000-0000-0004-000000000038',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TRANSITION_VIEW


--Start TRANSITION_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('dfe4b58f-843e-35b5-a0b8-fab1d1f3c213', 'Transition update name', null, 'permissionName'),
       ('cf8ff152-a813-38a8-8e4f-9b9707ccddf4', 'Transition update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('dfe4b58f-843e-35b5-a0b8-fab1d1f3c213', 'en', 'Transition update', 0),
       ('cf8ff152-a813-38a8-8e4f-9b9707ccddf4', 'en', 'Transition update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000039', 'TRANSITION_UPDATE', '00000000-0000-0000-0005-000000000001',
        'dfe4b58f-843e-35b5-a0b8-fab1d1f3c213', 'cf8ff152-a813-38a8-8e4f-9b9707ccddf4')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000039', '00000000-0000-0000-0004-000000000039',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TRANSITION_UPDATE


--Start LINK_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('cea8bdd8-d02f-3bf6-874f-c7175253bbed', 'Link create name', null, 'permissionName'),
       ('d583e5a8-6401-30e2-8f6f-dfb51bd37867', 'Link create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('cea8bdd8-d02f-3bf6-874f-c7175253bbed', 'en', 'Link create', 0),
       ('d583e5a8-6401-30e2-8f6f-dfb51bd37867', 'en', 'Link create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000040', 'LINK_CREATE', '00000000-0000-0000-0005-000000000001',
        'cea8bdd8-d02f-3bf6-874f-c7175253bbed', 'd583e5a8-6401-30e2-8f6f-dfb51bd37867')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000040', '00000000-0000-0000-0004-000000000040',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End LINK_CREATE


--Start LINK_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('5c621362-d052-3bf4-9d76-51a81e46817a', 'Link update name', null, 'permissionName'),
       ('9d418793-d55a-3e5a-8ca2-975172e786c2', 'Link update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('5c621362-d052-3bf4-9d76-51a81e46817a', 'en', 'Link update', 0),
       ('9d418793-d55a-3e5a-8ca2-975172e786c2', 'en', 'Link update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000041', 'LINK_UPDATE', '00000000-0000-0000-0005-000000000001',
        '5c621362-d052-3bf4-9d76-51a81e46817a', '9d418793-d55a-3e5a-8ca2-975172e786c2')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000041', '00000000-0000-0000-0004-000000000041',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End LINK_UPDATE


--Start LINK_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('11b8bf7b-0446-3b8c-946c-6840b71eb467', 'Link delete name', null, 'permissionName'),
       ('eda1718d-be84-3f24-9a6f-a4742e4b99b0', 'Link delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('11b8bf7b-0446-3b8c-946c-6840b71eb467', 'en', 'Link delete', 0),
       ('eda1718d-be84-3f24-9a6f-a4742e4b99b0', 'en', 'Link delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000042', 'LINK_DELETE', '00000000-0000-0000-0005-000000000001',
        '11b8bf7b-0446-3b8c-946c-6840b71eb467', 'eda1718d-be84-3f24-9a6f-a4742e4b99b0')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000042', '00000000-0000-0000-0004-000000000042',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End LINK_DELETE


--Start LINK_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('15cdc3ae-3956-39fc-aad5-7b4bc10982f5', 'Link view name', null, 'permissionName'),
       ('62092088-5c1d-3cd7-8d43-173d6f9baf8b', 'Link view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('15cdc3ae-3956-39fc-aad5-7b4bc10982f5', 'en', 'Link view', 0),
       ('62092088-5c1d-3cd7-8d43-173d6f9baf8b', 'en', 'Link view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000043', 'LINK_VIEW', '00000000-0000-0000-0005-000000000001',
        '15cdc3ae-3956-39fc-aad5-7b4bc10982f5', '62092088-5c1d-3cd7-8d43-173d6f9baf8b')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000043', '00000000-0000-0000-0004-000000000043',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End LINK_VIEW


--Start DOMAIN_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ac5ab813-cc45-3d17-830b-a7673ecf460f', 'Domain create name', null, 'permissionName'),
       ('f6064eb0-aa03-3a81-81b4-ff233bc04f1d', 'Domain create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ac5ab813-cc45-3d17-830b-a7673ecf460f', 'en', 'Domain create', 0),
       ('f6064eb0-aa03-3a81-81b4-ff233bc04f1d', 'en', 'Domain create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000044', 'DOMAIN_CREATE', '00000000-0000-0000-0005-000000000001',
        'ac5ab813-cc45-3d17-830b-a7673ecf460f', 'f6064eb0-aa03-3a81-81b4-ff233bc04f1d')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000044', '00000000-0000-0000-0004-000000000044',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_CREATE


--Start DOMAIN_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('a62c71b4-b680-3639-a8c3-c67dfd9e4c9e', 'Domain update name', null, 'permissionName'),
       ('43795d48-1c31-357a-9d7a-7a83bc25d1ba', 'Domain update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('a62c71b4-b680-3639-a8c3-c67dfd9e4c9e', 'en', 'Domain update', 0),
       ('43795d48-1c31-357a-9d7a-7a83bc25d1ba', 'en', 'Domain update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000045', 'DOMAIN_UPDATE', '00000000-0000-0000-0005-000000000001',
        'a62c71b4-b680-3639-a8c3-c67dfd9e4c9e', '43795d48-1c31-357a-9d7a-7a83bc25d1ba')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000045', '00000000-0000-0000-0004-000000000045',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_UPDATE


--Start DOMAIN_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('f8578afa-05aa-3e53-8eed-bcacc4df2678', 'Domain delete name', null, 'permissionName'),
       ('982697a2-7065-3eac-8058-f59639e7ecbd', 'Domain delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('f8578afa-05aa-3e53-8eed-bcacc4df2678', 'en', 'Domain delete', 0),
       ('982697a2-7065-3eac-8058-f59639e7ecbd', 'en', 'Domain delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000046', 'DOMAIN_DELETE', '00000000-0000-0000-0005-000000000001',
        'f8578afa-05aa-3e53-8eed-bcacc4df2678', '982697a2-7065-3eac-8058-f59639e7ecbd')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000046', '00000000-0000-0000-0004-000000000046',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_DELETE


--Start DOMAIN_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('8add0d32-6d7c-373c-a645-351e4f4e5306', 'Domain view name', null, 'permissionName'),
       ('4592e85c-a99e-3f3c-b5c3-3d8dc14f9f00', 'Domain view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('8add0d32-6d7c-373c-a645-351e4f4e5306', 'en', 'Domain view', 0),
       ('4592e85c-a99e-3f3c-b5c3-3d8dc14f9f00', 'en', 'Domain view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000047', 'DOMAIN_VIEW', '00000000-0000-0000-0005-000000000001',
        '8add0d32-6d7c-373c-a645-351e4f4e5306', '4592e85c-a99e-3f3c-b5c3-3d8dc14f9f00')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000047', '00000000-0000-0000-0004-000000000047',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_VIEW


--Start TWIN_STATUS_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('7e87bd24-c919-3ca7-b962-22546da3889d', 'Twin status create name', null, 'permissionName'),
       ('82b1fcc3-dfba-3d65-9e7d-a420e3f3b208', 'Twin status create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('7e87bd24-c919-3ca7-b962-22546da3889d', 'en', 'Twin status create', 0),
       ('82b1fcc3-dfba-3d65-9e7d-a420e3f3b208', 'en', 'Twin status create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000048', 'TWIN_STATUS_CREATE', '00000000-0000-0000-0005-000000000001',
        '7e87bd24-c919-3ca7-b962-22546da3889d', '82b1fcc3-dfba-3d65-9e7d-a420e3f3b208')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000048', '00000000-0000-0000-0004-000000000048',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_STATUS_CREATE


--Start TWIN_STATUS_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('689abb6b-4c1d-3d29-962b-a8c13fa4aa16', 'Twin status update name', null, 'permissionName'),
       ('3c3ced89-899a-34ad-8262-3b7d2dc25a82', 'Twin status update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('689abb6b-4c1d-3d29-962b-a8c13fa4aa16', 'en', 'Twin status update', 0),
       ('3c3ced89-899a-34ad-8262-3b7d2dc25a82', 'en', 'Twin status update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000049', 'TWIN_STATUS_UPDATE', '00000000-0000-0000-0005-000000000001',
        '689abb6b-4c1d-3d29-962b-a8c13fa4aa16', '3c3ced89-899a-34ad-8262-3b7d2dc25a82')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000049', '00000000-0000-0000-0004-000000000049',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_STATUS_UPDATE


--Start TWIN_STATUS_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('61f5a1e9-135e-3fb3-aa22-3f6fa143b617', 'Twin status delete name', null, 'permissionName'),
       ('eed87f94-8fc5-3628-b9b7-a1c07a98b5f7', 'Twin status delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('61f5a1e9-135e-3fb3-aa22-3f6fa143b617', 'en', 'Twin status delete', 0),
       ('eed87f94-8fc5-3628-b9b7-a1c07a98b5f7', 'en', 'Twin status delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000050', 'TWIN_STATUS_DELETE', '00000000-0000-0000-0005-000000000001',
        '61f5a1e9-135e-3fb3-aa22-3f6fa143b617', 'eed87f94-8fc5-3628-b9b7-a1c07a98b5f7')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000050', '00000000-0000-0000-0004-000000000050',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_STATUS_DELETE


--Start TWIN_STATUS_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ad71b012-ab8c-3d43-a242-56aa4c0103dc', 'Twin status view name', null, 'permissionName'),
       ('38927b9f-6e0f-3877-ad27-1731e27b7c63', 'Twin status view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ad71b012-ab8c-3d43-a242-56aa4c0103dc', 'en', 'Twin status view', 0),
       ('38927b9f-6e0f-3877-ad27-1731e27b7c63', 'en', 'Twin status view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000051', 'TWIN_STATUS_VIEW', '00000000-0000-0000-0005-000000000001',
        'ad71b012-ab8c-3d43-a242-56aa4c0103dc', '38927b9f-6e0f-3877-ad27-1731e27b7c63')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000051', '00000000-0000-0000-0004-000000000051',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_STATUS_VIEW


--Start TWIN_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('f00faeb1-c30e-3d53-95f9-7f3a5a49795b', 'Twin create name', null, 'permissionName'),
       ('b87788cf-fcd0-3b8e-a4a6-c03b4b2b0e90', 'Twin create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('f00faeb1-c30e-3d53-95f9-7f3a5a49795b', 'en', 'Twin create', 0),
       ('b87788cf-fcd0-3b8e-a4a6-c03b4b2b0e90', 'en', 'Twin create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000052', 'TWIN_CREATE', '00000000-0000-0000-0005-000000000001',
        'f00faeb1-c30e-3d53-95f9-7f3a5a49795b', 'b87788cf-fcd0-3b8e-a4a6-c03b4b2b0e90')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000052', '00000000-0000-0000-0004-000000000052',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_CREATE


--Start TWIN_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('684344cd-0cca-3241-83fa-64aa0ffac58d', 'Twin update name', null, 'permissionName'),
       ('d8b5d1a6-836f-311d-93c0-0d731639ae0d', 'Twin update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('684344cd-0cca-3241-83fa-64aa0ffac58d', 'en', 'Twin update', 0),
       ('d8b5d1a6-836f-311d-93c0-0d731639ae0d', 'en', 'Twin update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000053', 'TWIN_UPDATE', '00000000-0000-0000-0005-000000000001',
        '684344cd-0cca-3241-83fa-64aa0ffac58d', 'd8b5d1a6-836f-311d-93c0-0d731639ae0d')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000053', '00000000-0000-0000-0004-000000000053',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_UPDATE


--Start TWIN_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('a9fe137c-21d9-3450-826e-f051bb118427', 'Twin delete name', null, 'permissionName'),
       ('0c981bbc-1217-30c5-8f2a-a99d417de978', 'Twin delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('a9fe137c-21d9-3450-826e-f051bb118427', 'en', 'Twin delete', 0),
       ('0c981bbc-1217-30c5-8f2a-a99d417de978', 'en', 'Twin delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000054', 'TWIN_DELETE', '00000000-0000-0000-0005-000000000001',
        'a9fe137c-21d9-3450-826e-f051bb118427', '0c981bbc-1217-30c5-8f2a-a99d417de978')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000054', '00000000-0000-0000-0004-000000000054',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_DELETE


--Start TWIN_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('406c6e16-f6ca-3365-b3df-6b7829073952', 'Twin view name', null, 'permissionName'),
       ('ffc76128-49cd-3a37-846d-5e26a1777bf1', 'Twin view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('406c6e16-f6ca-3365-b3df-6b7829073952', 'en', 'Twin view', 0),
       ('ffc76128-49cd-3a37-846d-5e26a1777bf1', 'en', 'Twin view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000055', 'TWIN_VIEW', '00000000-0000-0000-0005-000000000001',
        '406c6e16-f6ca-3365-b3df-6b7829073952', 'ffc76128-49cd-3a37-846d-5e26a1777bf1')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000055', '00000000-0000-0000-0004-000000000055',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TWIN_VIEW


--Start COMMENT_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('9d50fb53-74ef-33f8-8cbb-900d5f409192', 'Comment create name', null, 'permissionName'),
       ('c63a69e0-6354-3072-a959-5e80bd98d668', 'Comment create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('9d50fb53-74ef-33f8-8cbb-900d5f409192', 'en', 'Comment create', 0),
       ('c63a69e0-6354-3072-a959-5e80bd98d668', 'en', 'Comment create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000056', 'COMMENT_CREATE', '00000000-0000-0000-0005-000000000001',
        '9d50fb53-74ef-33f8-8cbb-900d5f409192', 'c63a69e0-6354-3072-a959-5e80bd98d668')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000056', '00000000-0000-0000-0004-000000000056',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End COMMENT_CREATE


--Start COMMENT_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('c1854723-a195-3126-9857-16c5f189f437', 'Comment update name', null, 'permissionName'),
       ('51287330-664a-3152-b265-2357d89d4096', 'Comment update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('c1854723-a195-3126-9857-16c5f189f437', 'en', 'Comment update', 0),
       ('51287330-664a-3152-b265-2357d89d4096', 'en', 'Comment update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000057', 'COMMENT_UPDATE', '00000000-0000-0000-0005-000000000001',
        'c1854723-a195-3126-9857-16c5f189f437', '51287330-664a-3152-b265-2357d89d4096')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000057', '00000000-0000-0000-0004-000000000057',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End COMMENT_UPDATE


--Start COMMENT_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('0904ad05-8e13-3331-b761-e5d048ea5565', 'Comment delete name', null, 'permissionName'),
       ('90a1cbd8-5e0a-38a7-a711-7f8ecd41684c', 'Comment delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('0904ad05-8e13-3331-b761-e5d048ea5565', 'en', 'Comment delete', 0),
       ('90a1cbd8-5e0a-38a7-a711-7f8ecd41684c', 'en', 'Comment delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000058', 'COMMENT_DELETE', '00000000-0000-0000-0005-000000000001',
        '0904ad05-8e13-3331-b761-e5d048ea5565', '90a1cbd8-5e0a-38a7-a711-7f8ecd41684c')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000058', '00000000-0000-0000-0004-000000000058',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End COMMENT_DELETE


--Start COMMENT_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('7c865508-10ab-330a-b3c4-d8ac30fe06ab', 'Comment view name', null, 'permissionName'),
       ('372d13aa-d191-337d-9c5d-8bd97cd05000', 'Comment view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('7c865508-10ab-330a-b3c4-d8ac30fe06ab', 'en', 'Comment view', 0),
       ('372d13aa-d191-337d-9c5d-8bd97cd05000', 'en', 'Comment view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000059', 'COMMENT_VIEW', '00000000-0000-0000-0005-000000000001',
        '7c865508-10ab-330a-b3c4-d8ac30fe06ab', '372d13aa-d191-337d-9c5d-8bd97cd05000')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000059', '00000000-0000-0000-0004-000000000059',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End COMMENT_VIEW


--Start ATTACHMENT_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('5c00aec1-4bc3-3816-80e0-18c1407388b6', 'Attachment create name', null, 'permissionName'),
       ('77bbf9c4-1b78-36dd-93c5-eb6fe307f4a2', 'Attachment create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('5c00aec1-4bc3-3816-80e0-18c1407388b6', 'en', 'Attachment create', 0),
       ('77bbf9c4-1b78-36dd-93c5-eb6fe307f4a2', 'en', 'Attachment create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000060', 'ATTACHMENT_CREATE', '00000000-0000-0000-0005-000000000001',
        '5c00aec1-4bc3-3816-80e0-18c1407388b6', '77bbf9c4-1b78-36dd-93c5-eb6fe307f4a2')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000060', '00000000-0000-0000-0004-000000000060',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End ATTACHMENT_CREATE


--Start ATTACHMENT_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('2e32e8c0-aca7-39a2-8fad-732161d3e2c0', 'Attachment update name', null, 'permissionName'),
       ('ff6f4bbd-3dc7-3f10-8c42-8443c61a9e3d', 'Attachment update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('2e32e8c0-aca7-39a2-8fad-732161d3e2c0', 'en', 'Attachment update', 0),
       ('ff6f4bbd-3dc7-3f10-8c42-8443c61a9e3d', 'en', 'Attachment update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000061', 'ATTACHMENT_UPDATE', '00000000-0000-0000-0005-000000000001',
        '2e32e8c0-aca7-39a2-8fad-732161d3e2c0', 'ff6f4bbd-3dc7-3f10-8c42-8443c61a9e3d')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000061', '00000000-0000-0000-0004-000000000061',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End ATTACHMENT_UPDATE


--Start ATTACHMENT_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('a6c6cfed-7bb2-3c91-a21a-3ac064d6a5cb', 'Attachment delete name', null, 'permissionName'),
       ('d9ff8cf0-8bf7-3ad0-94b2-ee9126031b30', 'Attachment delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('a6c6cfed-7bb2-3c91-a21a-3ac064d6a5cb', 'en', 'Attachment delete', 0),
       ('d9ff8cf0-8bf7-3ad0-94b2-ee9126031b30', 'en', 'Attachment delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000062', 'ATTACHMENT_DELETE', '00000000-0000-0000-0005-000000000001',
        'a6c6cfed-7bb2-3c91-a21a-3ac064d6a5cb', 'd9ff8cf0-8bf7-3ad0-94b2-ee9126031b30')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000062', '00000000-0000-0000-0004-000000000062',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End ATTACHMENT_DELETE


--Start ATTACHMENT_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('00fbd297-d552-34c5-995b-0cc3532ede53', 'Attachment view name', null, 'permissionName'),
       ('cc7408a7-09dc-35aa-9722-dfcbe63b24ec', 'Attachment view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('00fbd297-d552-34c5-995b-0cc3532ede53', 'en', 'Attachment view', 0),
       ('cc7408a7-09dc-35aa-9722-dfcbe63b24ec', 'en', 'Attachment view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000063', 'ATTACHMENT_VIEW', '00000000-0000-0000-0005-000000000001',
        '00fbd297-d552-34c5-995b-0cc3532ede53', 'cc7408a7-09dc-35aa-9722-dfcbe63b24ec')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000063', '00000000-0000-0000-0004-000000000063',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End ATTACHMENT_VIEW


--Start USER_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('69178769-56b3-3b10-8911-ed26df8f8773', 'User create name', null, 'permissionName'),
       ('37afb413-3b1d-3805-be4b-d785a00cd8eb', 'User create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('69178769-56b3-3b10-8911-ed26df8f8773', 'en', 'User create', 0),
       ('37afb413-3b1d-3805-be4b-d785a00cd8eb', 'en', 'User create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000064', 'USER_CREATE', '00000000-0000-0000-0005-000000000001',
        '69178769-56b3-3b10-8911-ed26df8f8773', '37afb413-3b1d-3805-be4b-d785a00cd8eb')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000064', '00000000-0000-0000-0004-000000000064',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End USER_CREATE


--Start USER_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('8993e076-f4f5-358b-b2e1-bc85e1d8d6c1', 'User update name', null, 'permissionName'),
       ('965c7010-8a26-3fe1-906e-68e75e1852af', 'User update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('8993e076-f4f5-358b-b2e1-bc85e1d8d6c1', 'en', 'User update', 0),
       ('965c7010-8a26-3fe1-906e-68e75e1852af', 'en', 'User update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000065', 'USER_UPDATE', '00000000-0000-0000-0005-000000000001',
        '8993e076-f4f5-358b-b2e1-bc85e1d8d6c1', '965c7010-8a26-3fe1-906e-68e75e1852af')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000065', '00000000-0000-0000-0004-000000000065',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End USER_UPDATE


--Start USER_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ccd6d050-506d-3aaa-b314-4226010497dc', 'User delete name', null, 'permissionName'),
       ('9e300742-5466-39d6-8c3b-0f10d2604672', 'User delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ccd6d050-506d-3aaa-b314-4226010497dc', 'en', 'User delete', 0),
       ('9e300742-5466-39d6-8c3b-0f10d2604672', 'en', 'User delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000066', 'USER_DELETE', '00000000-0000-0000-0005-000000000001',
        'ccd6d050-506d-3aaa-b314-4226010497dc', '9e300742-5466-39d6-8c3b-0f10d2604672')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000066', '00000000-0000-0000-0004-000000000066',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End USER_DELETE


--Start USER_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('980f0e22-603b-3d45-87a8-4df4113f3905', 'User view name', null, 'permissionName'),
       ('70c2e551-5163-3725-aacf-855b192a6b10', 'User view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('980f0e22-603b-3d45-87a8-4df4113f3905', 'en', 'User view', 0),
       ('70c2e551-5163-3725-aacf-855b192a6b10', 'en', 'User view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000067', 'USER_VIEW', '00000000-0000-0000-0005-000000000001',
        '980f0e22-603b-3d45-87a8-4df4113f3905', '70c2e551-5163-3725-aacf-855b192a6b10')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000067', '00000000-0000-0000-0004-000000000067',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End USER_VIEW


--Start USER_GROUP_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('5681d503-c719-3b36-858f-627c08b4d5c6', 'User group create name', null, 'permissionName'),
       ('98b4a8ac-b994-3fc1-9a58-54eafc049e2e', 'User group create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('5681d503-c719-3b36-858f-627c08b4d5c6', 'en', 'User group create', 0),
       ('98b4a8ac-b994-3fc1-9a58-54eafc049e2e', 'en', 'User group create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000068', 'USER_GROUP_CREATE', '00000000-0000-0000-0005-000000000001',
        '5681d503-c719-3b36-858f-627c08b4d5c6', '98b4a8ac-b994-3fc1-9a58-54eafc049e2e')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000068', '00000000-0000-0000-0004-000000000068',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End USER_GROUP_CREATE


--Start USER_GROUP_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ab07515e-89a8-34ef-8aab-366f46bdb933', 'User group update name', null, 'permissionName'),
       ('ba2ac225-e27f-3d24-b7e7-4e02902a25d6', 'User group update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ab07515e-89a8-34ef-8aab-366f46bdb933', 'en', 'User group update', 0),
       ('ba2ac225-e27f-3d24-b7e7-4e02902a25d6', 'en', 'User group update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000069', 'USER_GROUP_UPDATE', '00000000-0000-0000-0005-000000000001',
        'ab07515e-89a8-34ef-8aab-366f46bdb933', 'ba2ac225-e27f-3d24-b7e7-4e02902a25d6')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000069', '00000000-0000-0000-0004-000000000069',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End USER_GROUP_UPDATE


--Start USER_GROUP_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('4a6c01f9-f7cd-3516-aeb1-0d093869acac', 'User group delete name', null, 'permissionName'),
       ('d5f5e692-08b4-3bb7-972e-eb29855ed7e4', 'User group delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('4a6c01f9-f7cd-3516-aeb1-0d093869acac', 'en', 'User group delete', 0),
       ('d5f5e692-08b4-3bb7-972e-eb29855ed7e4', 'en', 'User group delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000070', 'USER_GROUP_DELETE', '00000000-0000-0000-0005-000000000001',
        '4a6c01f9-f7cd-3516-aeb1-0d093869acac', 'd5f5e692-08b4-3bb7-972e-eb29855ed7e4')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000070', '00000000-0000-0000-0004-000000000070',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End USER_GROUP_DELETE


--Start USER_GROUP_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('3f68d014-c225-3f08-b202-2b3f009e8834', 'User group view name', null, 'permissionName'),
       ('c1cb9648-e7d3-3598-a7bb-24379a0ebac5', 'User group view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('3f68d014-c225-3f08-b202-2b3f009e8834', 'en', 'User group view', 0),
       ('c1cb9648-e7d3-3598-a7bb-24379a0ebac5', 'en', 'User group view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000071', 'USER_GROUP_VIEW', '00000000-0000-0000-0005-000000000001',
        '3f68d014-c225-3f08-b202-2b3f009e8834', 'c1cb9648-e7d3-3598-a7bb-24379a0ebac5')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000071', '00000000-0000-0000-0004-000000000071',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End USER_GROUP_VIEW


--Start DATA_LIST_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('0bea6d1a-9977-365b-810c-0fc2d5d78ea8', 'Data list create name', null, 'permissionName'),
       ('8aaddb24-a340-38d9-98b0-8f6a675b7a49', 'Data list create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('0bea6d1a-9977-365b-810c-0fc2d5d78ea8', 'en', 'Data list create', 0),
       ('8aaddb24-a340-38d9-98b0-8f6a675b7a49', 'en', 'Data list create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000072', 'DATA_LIST_CREATE', '00000000-0000-0000-0005-000000000001',
        '0bea6d1a-9977-365b-810c-0fc2d5d78ea8', '8aaddb24-a340-38d9-98b0-8f6a675b7a49')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000072', '00000000-0000-0000-0004-000000000072',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_CREATE


--Start DATA_LIST_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('11f8bb7b-9f61-3cae-a706-36fb2a3fb2ee', 'Data list update name', null, 'permissionName'),
       ('d9a56bed-9d22-3480-b875-5c6ab6cb6c26', 'Data list update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('11f8bb7b-9f61-3cae-a706-36fb2a3fb2ee', 'en', 'Data list update', 0),
       ('d9a56bed-9d22-3480-b875-5c6ab6cb6c26', 'en', 'Data list update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000073', 'DATA_LIST_UPDATE', '00000000-0000-0000-0005-000000000001',
        '11f8bb7b-9f61-3cae-a706-36fb2a3fb2ee', 'd9a56bed-9d22-3480-b875-5c6ab6cb6c26')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000073', '00000000-0000-0000-0004-000000000073',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_UPDATE


--Start DATA_LIST_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('78ef7413-d7e8-359c-9f16-2a5bb2e7e7bf', 'Data list delete name', null, 'permissionName'),
       ('b12ead46-bdc7-34c0-8ce7-873b4d0c9e45', 'Data list delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('78ef7413-d7e8-359c-9f16-2a5bb2e7e7bf', 'en', 'Data list delete', 0),
       ('b12ead46-bdc7-34c0-8ce7-873b4d0c9e45', 'en', 'Data list delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000074', 'DATA_LIST_DELETE', '00000000-0000-0000-0005-000000000001',
        '78ef7413-d7e8-359c-9f16-2a5bb2e7e7bf', 'b12ead46-bdc7-34c0-8ce7-873b4d0c9e45')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000074', '00000000-0000-0000-0004-000000000074',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_DELETE


--Start DATA_LIST_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('eb7b59bd-69ad-3165-979b-58328a5176c1', 'Data list view name', null, 'permissionName'),
       ('7162f1ed-2759-3fb6-bea9-74b8dde547da', 'Data list view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('eb7b59bd-69ad-3165-979b-58328a5176c1', 'en', 'Data list view', 0),
       ('7162f1ed-2759-3fb6-bea9-74b8dde547da', 'en', 'Data list view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000075', 'DATA_LIST_VIEW', '00000000-0000-0000-0005-000000000001',
        'eb7b59bd-69ad-3165-979b-58328a5176c1', '7162f1ed-2759-3fb6-bea9-74b8dde547da')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000075', '00000000-0000-0000-0004-000000000075',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_VIEW


--Start DATA_LIST_OPTION_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('87b66961-da18-3b35-9779-e7700474fae9', 'Data list option create name', null, 'permissionName'),
       ('ec043fc7-41f8-369a-af53-6050e5582e6b', 'Data list option create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('87b66961-da18-3b35-9779-e7700474fae9', 'en', 'Data list option create', 0),
       ('ec043fc7-41f8-369a-af53-6050e5582e6b', 'en', 'Data list option create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000076', 'DATA_LIST_OPTION_CREATE', '00000000-0000-0000-0005-000000000001',
        '87b66961-da18-3b35-9779-e7700474fae9', 'ec043fc7-41f8-369a-af53-6050e5582e6b')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000076', '00000000-0000-0000-0004-000000000076',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_OPTION_CREATE


--Start DATA_LIST_OPTION_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('525b44ce-0144-3b37-924a-845175bd1e27', 'Data list option update name', null, 'permissionName'),
       ('fdd50068-3145-3afa-bf23-b62a13dcdeef', 'Data list option update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('525b44ce-0144-3b37-924a-845175bd1e27', 'en', 'Data list option update', 0),
       ('fdd50068-3145-3afa-bf23-b62a13dcdeef', 'en', 'Data list option update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000077', 'DATA_LIST_OPTION_UPDATE', '00000000-0000-0000-0005-000000000001',
        '525b44ce-0144-3b37-924a-845175bd1e27', 'fdd50068-3145-3afa-bf23-b62a13dcdeef')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000077', '00000000-0000-0000-0004-000000000077',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_OPTION_UPDATE


--Start DATA_LIST_OPTION_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('4355e764-c6ad-3917-bc45-5b5cecef5e9f', 'Data list option delete name', null, 'permissionName'),
       ('6f9cbf73-e70c-30dc-b118-0ced77efde28', 'Data list option delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('4355e764-c6ad-3917-bc45-5b5cecef5e9f', 'en', 'Data list option delete', 0),
       ('6f9cbf73-e70c-30dc-b118-0ced77efde28', 'en', 'Data list option delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000078', 'DATA_LIST_OPTION_DELETE', '00000000-0000-0000-0005-000000000001',
        '4355e764-c6ad-3917-bc45-5b5cecef5e9f', '6f9cbf73-e70c-30dc-b118-0ced77efde28')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000078', '00000000-0000-0000-0004-000000000078',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_OPTION_DELETE


--Start DATA_LIST_OPTION_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('e6cddd28-b115-3e8e-9245-2eeaf17539f1', 'Data list option view name', null, 'permissionName'),
       ('30efecf2-7587-3997-b62d-12a4b5658770', 'Data list option view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('e6cddd28-b115-3e8e-9245-2eeaf17539f1', 'en', 'Data list option view', 0),
       ('30efecf2-7587-3997-b62d-12a4b5658770', 'en', 'Data list option view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000079', 'DATA_LIST_OPTION_VIEW', '00000000-0000-0000-0005-000000000001',
        'e6cddd28-b115-3e8e-9245-2eeaf17539f1', '30efecf2-7587-3997-b62d-12a4b5658770')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000079', '00000000-0000-0000-0004-000000000079',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_OPTION_VIEW


--Start DATA_LIST_SUBSET_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ac81eb4e-397a-3872-988c-97c39a0e853b', 'Data list subset create name', null, 'permissionName'),
       ('523a089d-8d3c-3c65-92b5-c30fd3b6ba62', 'Data list subset create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ac81eb4e-397a-3872-988c-97c39a0e853b', 'en', 'Data list subset create', 0),
       ('523a089d-8d3c-3c65-92b5-c30fd3b6ba62', 'en', 'Data list subset create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000080', 'DATA_LIST_SUBSET_CREATE', '00000000-0000-0000-0005-000000000001',
        'ac81eb4e-397a-3872-988c-97c39a0e853b', '523a089d-8d3c-3c65-92b5-c30fd3b6ba62')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000080', '00000000-0000-0000-0004-000000000080',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_SUBSET_CREATE


--Start DATA_LIST_SUBSET_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('3835a787-c48a-30b1-ada2-56fffadb6443', 'Data list subset update name', null, 'permissionName'),
       ('25871d29-3438-34db-b953-ade635ef58b6', 'Data list subset update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('3835a787-c48a-30b1-ada2-56fffadb6443', 'en', 'Data list subset update', 0),
       ('25871d29-3438-34db-b953-ade635ef58b6', 'en', 'Data list subset update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000081', 'DATA_LIST_SUBSET_UPDATE', '00000000-0000-0000-0005-000000000001',
        '3835a787-c48a-30b1-ada2-56fffadb6443', '25871d29-3438-34db-b953-ade635ef58b6')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000081', '00000000-0000-0000-0004-000000000081',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_SUBSET_UPDATE


--Start DATA_LIST_SUBSET_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('a36ac489-311e-355e-b6c8-b6a3839f200f', 'Data list subset delete name', null, 'permissionName'),
       ('15b9f74c-e099-3955-97e3-ae87d2968046', 'Data list subset delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('a36ac489-311e-355e-b6c8-b6a3839f200f', 'en', 'Data list subset delete', 0),
       ('15b9f74c-e099-3955-97e3-ae87d2968046', 'en', 'Data list subset delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000082', 'DATA_LIST_SUBSET_DELETE', '00000000-0000-0000-0005-000000000001',
        'a36ac489-311e-355e-b6c8-b6a3839f200f', '15b9f74c-e099-3955-97e3-ae87d2968046')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000082', '00000000-0000-0000-0004-000000000082',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_SUBSET_DELETE


--Start DATA_LIST_SUBSET_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('7f92343a-0c45-3739-b6a7-b0f1be6e14ee', 'Data list subset view name', null, 'permissionName'),
       ('b60f0dce-7160-34cc-9766-0e4f7330736c', 'Data list subset view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('7f92343a-0c45-3739-b6a7-b0f1be6e14ee', 'en', 'Data list subset view', 0),
       ('b60f0dce-7160-34cc-9766-0e4f7330736c', 'en', 'Data list subset view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000083', 'DATA_LIST_SUBSET_VIEW', '00000000-0000-0000-0005-000000000001',
        '7f92343a-0c45-3739-b6a7-b0f1be6e14ee', 'b60f0dce-7160-34cc-9766-0e4f7330736c')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000083', '00000000-0000-0000-0004-000000000083',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DATA_LIST_SUBSET_VIEW


--Start PERMISSION_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('2313861e-1d8b-36a8-b7a6-9d1153ba8d37', 'Permission create name', null, 'permissionName'),
       ('61360bbf-9edc-36e6-83bf-670cf143b475', 'Permission create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('2313861e-1d8b-36a8-b7a6-9d1153ba8d37', 'en', 'Permission create', 0),
       ('61360bbf-9edc-36e6-83bf-670cf143b475', 'en', 'Permission create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000084', 'PERMISSION_CREATE', '00000000-0000-0000-0005-000000000001',
        '2313861e-1d8b-36a8-b7a6-9d1153ba8d37', '61360bbf-9edc-36e6-83bf-670cf143b475')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000084', '00000000-0000-0000-0004-000000000084',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_CREATE


--Start PERMISSION_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ea1842a7-c9d1-31bd-b7e2-ce1869fb5e58', 'Permission update name', null, 'permissionName'),
       ('7dca4011-00c5-3bd5-b6dc-8f1cd88cc73a', 'Permission update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ea1842a7-c9d1-31bd-b7e2-ce1869fb5e58', 'en', 'Permission update', 0),
       ('7dca4011-00c5-3bd5-b6dc-8f1cd88cc73a', 'en', 'Permission update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000085', 'PERMISSION_UPDATE', '00000000-0000-0000-0005-000000000001',
        'ea1842a7-c9d1-31bd-b7e2-ce1869fb5e58', '7dca4011-00c5-3bd5-b6dc-8f1cd88cc73a')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000085', '00000000-0000-0000-0004-000000000085',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_UPDATE


--Start PERMISSION_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('0a6b4049-4797-3d0a-a8d2-6ef599b5e38c', 'Permission delete name', null, 'permissionName'),
       ('db7dfc72-7814-3ca5-8160-17909c136946', 'Permission delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('0a6b4049-4797-3d0a-a8d2-6ef599b5e38c', 'en', 'Permission delete', 0),
       ('db7dfc72-7814-3ca5-8160-17909c136946', 'en', 'Permission delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000086', 'PERMISSION_DELETE', '00000000-0000-0000-0005-000000000001',
        '0a6b4049-4797-3d0a-a8d2-6ef599b5e38c', 'db7dfc72-7814-3ca5-8160-17909c136946')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000086', '00000000-0000-0000-0004-000000000086',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_DELETE


--Start PERMISSION_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('e85b230f-9019-3756-a81f-d8bc0c83a5b3', 'Permission view name', null, 'permissionName'),
       ('a1bf7523-8ead-3df3-8586-2e5d64f027b2', 'Permission view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('e85b230f-9019-3756-a81f-d8bc0c83a5b3', 'en', 'Permission view', 0),
       ('a1bf7523-8ead-3df3-8586-2e5d64f027b2', 'en', 'Permission view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000087', 'PERMISSION_VIEW', '00000000-0000-0000-0005-000000000001',
        'e85b230f-9019-3756-a81f-d8bc0c83a5b3', 'a1bf7523-8ead-3df3-8586-2e5d64f027b2')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000087', '00000000-0000-0000-0004-000000000087',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_VIEW


--Start PERMISSION_GRANT_ASSIGNEE_PROPAGATION_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('18e4d0aa-4e35-3172-8a1e-af0647401116', 'Permission grant assignee propagation create name', null,
        'permissionName'),
       ('2a3087a6-1acc-3437-a39b-b65aef1dcb63', 'Permission grant assignee propagation create description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('18e4d0aa-4e35-3172-8a1e-af0647401116', 'en', 'Permission grant assignee propagation create', 0),
       ('2a3087a6-1acc-3437-a39b-b65aef1dcb63', 'en', 'Permission grant assignee propagation create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000088', 'PERMISSION_GRANT_ASSIGNEE_PROPAGATION_CREATE',
        '00000000-0000-0000-0005-000000000001', '18e4d0aa-4e35-3172-8a1e-af0647401116',
        '2a3087a6-1acc-3437-a39b-b65aef1dcb63')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000088', '00000000-0000-0000-0004-000000000088',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_ASSIGNEE_PROPAGATION_CREATE


--Start PERMISSION_GRANT_ASSIGNEE_PROPAGATION_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('cb3e8e13-f42d-31aa-8fbd-61ad7e28a7f3', 'Permission grant assignee propagation update name', null,
        'permissionName'),
       ('269bc523-ee5b-3ced-80c3-11f0bd13ac65', 'Permission grant assignee propagation update description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('cb3e8e13-f42d-31aa-8fbd-61ad7e28a7f3', 'en', 'Permission grant assignee propagation update', 0),
       ('269bc523-ee5b-3ced-80c3-11f0bd13ac65', 'en', 'Permission grant assignee propagation update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000089', 'PERMISSION_GRANT_ASSIGNEE_PROPAGATION_UPDATE',
        '00000000-0000-0000-0005-000000000001', 'cb3e8e13-f42d-31aa-8fbd-61ad7e28a7f3',
        '269bc523-ee5b-3ced-80c3-11f0bd13ac65')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000089', '00000000-0000-0000-0004-000000000089',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_ASSIGNEE_PROPAGATION_UPDATE


--Start PERMISSION_GRANT_ASSIGNEE_PROPAGATION_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('c3f0bc1b-2bc4-369a-b43a-9eb72b994fab', 'Permission grant assignee propagation delete name', null,
        'permissionName'),
       ('46834627-29b9-3891-91d3-98beb33d4dcf', 'Permission grant assignee propagation delete description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('c3f0bc1b-2bc4-369a-b43a-9eb72b994fab', 'en', 'Permission grant assignee propagation delete', 0),
       ('46834627-29b9-3891-91d3-98beb33d4dcf', 'en', 'Permission grant assignee propagation delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000090', 'PERMISSION_GRANT_ASSIGNEE_PROPAGATION_DELETE',
        '00000000-0000-0000-0005-000000000001', 'c3f0bc1b-2bc4-369a-b43a-9eb72b994fab',
        '46834627-29b9-3891-91d3-98beb33d4dcf')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000090', '00000000-0000-0000-0004-000000000090',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_ASSIGNEE_PROPAGATION_DELETE


--Start PERMISSION_GRANT_ASSIGNEE_PROPAGATION_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('0b32902d-6f8f-3303-9472-f216e784a7b2', 'Permission grant assignee propagation view name', null,
        'permissionName'),
       ('f64a1dcc-a5f0-3977-8a3c-66a33d201e61', 'Permission grant assignee propagation view description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('0b32902d-6f8f-3303-9472-f216e784a7b2', 'en', 'Permission grant assignee propagation view', 0),
       ('f64a1dcc-a5f0-3977-8a3c-66a33d201e61', 'en', 'Permission grant assignee propagation view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000091', 'PERMISSION_GRANT_ASSIGNEE_PROPAGATION_VIEW',
        '00000000-0000-0000-0005-000000000001', '0b32902d-6f8f-3303-9472-f216e784a7b2',
        'f64a1dcc-a5f0-3977-8a3c-66a33d201e61')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000091', '00000000-0000-0000-0004-000000000091',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_ASSIGNEE_PROPAGATION_VIEW


--Start PERMISSION_GRANT_SPACE_ROLE_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('aac05a6b-129d-382d-8c99-eac6b8d6acaa', 'Permission grant space role create name', null, 'permissionName'),
       ('a102f00c-7621-3b12-a4cf-6c01b4d08ea0', 'Permission grant space role create description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('aac05a6b-129d-382d-8c99-eac6b8d6acaa', 'en', 'Permission grant space role create', 0),
       ('a102f00c-7621-3b12-a4cf-6c01b4d08ea0', 'en', 'Permission grant space role create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000092', 'PERMISSION_GRANT_SPACE_ROLE_CREATE',
        '00000000-0000-0000-0005-000000000001', 'aac05a6b-129d-382d-8c99-eac6b8d6acaa',
        'a102f00c-7621-3b12-a4cf-6c01b4d08ea0')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000092', '00000000-0000-0000-0004-000000000092',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_SPACE_ROLE_CREATE


--Start PERMISSION_GRANT_SPACE_ROLE_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('7b5eb7f8-c7d7-397e-b5fa-f75d891966c5', 'Permission grant space role update name', null, 'permissionName'),
       ('77684f2f-602e-38bf-8f3f-36a683aa77c9', 'Permission grant space role update description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('7b5eb7f8-c7d7-397e-b5fa-f75d891966c5', 'en', 'Permission grant space role update', 0),
       ('77684f2f-602e-38bf-8f3f-36a683aa77c9', 'en', 'Permission grant space role update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000093', 'PERMISSION_GRANT_SPACE_ROLE_UPDATE',
        '00000000-0000-0000-0005-000000000001', '7b5eb7f8-c7d7-397e-b5fa-f75d891966c5',
        '77684f2f-602e-38bf-8f3f-36a683aa77c9')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000093', '00000000-0000-0000-0004-000000000093',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_SPACE_ROLE_UPDATE


--Start PERMISSION_GRANT_SPACE_ROLE_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('d6e5119b-6da9-31be-8e4d-4e74cd980a3b', 'Permission grant space role delete name', null, 'permissionName'),
       ('59ed7fc7-0c88-3887-a29d-4a324002d24f', 'Permission grant space role delete description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('d6e5119b-6da9-31be-8e4d-4e74cd980a3b', 'en', 'Permission grant space role delete', 0),
       ('59ed7fc7-0c88-3887-a29d-4a324002d24f', 'en', 'Permission grant space role delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000094', 'PERMISSION_GRANT_SPACE_ROLE_DELETE',
        '00000000-0000-0000-0005-000000000001', 'd6e5119b-6da9-31be-8e4d-4e74cd980a3b',
        '59ed7fc7-0c88-3887-a29d-4a324002d24f')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000094', '00000000-0000-0000-0004-000000000094',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_SPACE_ROLE_DELETE


--Start PERMISSION_GRANT_SPACE_ROLE_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('05ea4627-3408-3c54-8b1a-9b2e9678f90c', 'Permission grant space role view name', null, 'permissionName'),
       ('e35c9ddb-d91e-3314-8f08-2f42c8bc2f68', 'Permission grant space role view description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('05ea4627-3408-3c54-8b1a-9b2e9678f90c', 'en', 'Permission grant space role view', 0),
       ('e35c9ddb-d91e-3314-8f08-2f42c8bc2f68', 'en', 'Permission grant space role view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000095', 'PERMISSION_GRANT_SPACE_ROLE_VIEW',
        '00000000-0000-0000-0005-000000000001', '05ea4627-3408-3c54-8b1a-9b2e9678f90c',
        'e35c9ddb-d91e-3314-8f08-2f42c8bc2f68')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000095', '00000000-0000-0000-0004-000000000095',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_SPACE_ROLE_VIEW


--Start PERMISSION_GRANT_TWIN_ROLE_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('3c5d9196-6a02-3b0a-8106-b4ca9b8787f8', 'Permission grant twin role create name', null, 'permissionName'),
       ('2d5176e9-3bcf-3789-85e9-5bb6568258ea', 'Permission grant twin role create description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('3c5d9196-6a02-3b0a-8106-b4ca9b8787f8', 'en', 'Permission grant twin role create', 0),
       ('2d5176e9-3bcf-3789-85e9-5bb6568258ea', 'en', 'Permission grant twin role create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000096', 'PERMISSION_GRANT_TWIN_ROLE_CREATE',
        '00000000-0000-0000-0005-000000000001', '3c5d9196-6a02-3b0a-8106-b4ca9b8787f8',
        '2d5176e9-3bcf-3789-85e9-5bb6568258ea')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000096', '00000000-0000-0000-0004-000000000096',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_TWIN_ROLE_CREATE


--Start PERMISSION_GRANT_TWIN_ROLE_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('1a213d38-7d91-3edd-b094-2569a5d7ea14', 'Permission grant twin role update name', null, 'permissionName'),
       ('cb871e25-975d-3817-afd7-601388af4a29', 'Permission grant twin role update description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('1a213d38-7d91-3edd-b094-2569a5d7ea14', 'en', 'Permission grant twin role update', 0),
       ('cb871e25-975d-3817-afd7-601388af4a29', 'en', 'Permission grant twin role update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000097', 'PERMISSION_GRANT_TWIN_ROLE_UPDATE',
        '00000000-0000-0000-0005-000000000001', '1a213d38-7d91-3edd-b094-2569a5d7ea14',
        'cb871e25-975d-3817-afd7-601388af4a29')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000097', '00000000-0000-0000-0004-000000000097',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_TWIN_ROLE_UPDATE


--Start PERMISSION_GRANT_TWIN_ROLE_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('d67cfa3a-7868-3c04-b52d-c8c072b5e33b', 'Permission grant twin role delete name', null, 'permissionName'),
       ('55f62253-493b-37b5-bce2-a518b08dead6', 'Permission grant twin role delete description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('d67cfa3a-7868-3c04-b52d-c8c072b5e33b', 'en', 'Permission grant twin role delete', 0),
       ('55f62253-493b-37b5-bce2-a518b08dead6', 'en', 'Permission grant twin role delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000098', 'PERMISSION_GRANT_TWIN_ROLE_DELETE',
        '00000000-0000-0000-0005-000000000001', 'd67cfa3a-7868-3c04-b52d-c8c072b5e33b',
        '55f62253-493b-37b5-bce2-a518b08dead6')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000098', '00000000-0000-0000-0004-000000000098',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_TWIN_ROLE_DELETE


--Start PERMISSION_GRANT_TWIN_ROLE_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('71ec387e-3802-37b4-8f52-ac0426adc75e', 'Permission grant twin role view name', null, 'permissionName'),
       ('41786333-633b-3822-815f-ed82ed01ebec', 'Permission grant twin role view description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('71ec387e-3802-37b4-8f52-ac0426adc75e', 'en', 'Permission grant twin role view', 0),
       ('41786333-633b-3822-815f-ed82ed01ebec', 'en', 'Permission grant twin role view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000099', 'PERMISSION_GRANT_TWIN_ROLE_VIEW',
        '00000000-0000-0000-0005-000000000001', '71ec387e-3802-37b4-8f52-ac0426adc75e',
        '41786333-633b-3822-815f-ed82ed01ebec')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000099', '00000000-0000-0000-0004-000000000099',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_TWIN_ROLE_VIEW


--Start PERMISSION_GRANT_USER_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('3352a544-fadc-3d7b-835e-0ba726d85ec1', 'Permission grant user create name', null, 'permissionName'),
       ('8031d86a-e6fd-3348-92d9-46587994d7f5', 'Permission grant user create description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('3352a544-fadc-3d7b-835e-0ba726d85ec1', 'en', 'Permission grant user create', 0),
       ('8031d86a-e6fd-3348-92d9-46587994d7f5', 'en', 'Permission grant user create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000100', 'PERMISSION_GRANT_USER_CREATE', '00000000-0000-0000-0005-000000000001',
        '3352a544-fadc-3d7b-835e-0ba726d85ec1', '8031d86a-e6fd-3348-92d9-46587994d7f5')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000100', '00000000-0000-0000-0004-000000000100',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_USER_CREATE


--Start PERMISSION_GRANT_USER_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ec8d5d2c-3f12-390d-a507-2e2e9dd86ad5', 'Permission grant user update name', null, 'permissionName'),
       ('80bb11db-0dd2-39df-8bef-e0294c58f70a', 'Permission grant user update description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ec8d5d2c-3f12-390d-a507-2e2e9dd86ad5', 'en', 'Permission grant user update', 0),
       ('80bb11db-0dd2-39df-8bef-e0294c58f70a', 'en', 'Permission grant user update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000101', 'PERMISSION_GRANT_USER_UPDATE', '00000000-0000-0000-0005-000000000001',
        'ec8d5d2c-3f12-390d-a507-2e2e9dd86ad5', '80bb11db-0dd2-39df-8bef-e0294c58f70a')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000101', '00000000-0000-0000-0004-000000000101',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_USER_UPDATE


--Start PERMISSION_GRANT_USER_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('426439e8-ce04-3ee8-b118-7f572c4807e4', 'Permission grant user delete name', null, 'permissionName'),
       ('979646d5-7038-30ef-af78-5c25504e0739', 'Permission grant user delete description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('426439e8-ce04-3ee8-b118-7f572c4807e4', 'en', 'Permission grant user delete', 0),
       ('979646d5-7038-30ef-af78-5c25504e0739', 'en', 'Permission grant user delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000102', 'PERMISSION_GRANT_USER_DELETE', '00000000-0000-0000-0005-000000000001',
        '426439e8-ce04-3ee8-b118-7f572c4807e4', '979646d5-7038-30ef-af78-5c25504e0739')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000102', '00000000-0000-0000-0004-000000000102',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_USER_DELETE


--Start PERMISSION_GRANT_USER_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('94aa10b6-83c6-30ac-bb6b-6362809066c2', 'Permission grant user view name', null, 'permissionName'),
       ('324750ce-ea64-3b95-a881-e31d388cf4a0', 'Permission grant user view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('94aa10b6-83c6-30ac-bb6b-6362809066c2', 'en', 'Permission grant user view', 0),
       ('324750ce-ea64-3b95-a881-e31d388cf4a0', 'en', 'Permission grant user view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000103', 'PERMISSION_GRANT_USER_VIEW', '00000000-0000-0000-0005-000000000001',
        '94aa10b6-83c6-30ac-bb6b-6362809066c2', '324750ce-ea64-3b95-a881-e31d388cf4a0')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000103', '00000000-0000-0000-0004-000000000103',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_USER_VIEW


--Start PERMISSION_GRANT_USER_GROUP_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('f8d6357f-c111-30d7-9ced-1561a30f3ac0', 'Permission grant user group create name', null, 'permissionName'),
       ('94910134-bd47-39c8-a71d-137bcc3ea827', 'Permission grant user group create description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('f8d6357f-c111-30d7-9ced-1561a30f3ac0', 'en', 'Permission grant user group create', 0),
       ('94910134-bd47-39c8-a71d-137bcc3ea827', 'en', 'Permission grant user group create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000104', 'PERMISSION_GRANT_USER_GROUP_CREATE',
        '00000000-0000-0000-0005-000000000001', 'f8d6357f-c111-30d7-9ced-1561a30f3ac0',
        '94910134-bd47-39c8-a71d-137bcc3ea827')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000104', '00000000-0000-0000-0004-000000000104',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_USER_GROUP_CREATE


--Start PERMISSION_GRANT_USER_GROUP_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('48a67e62-dd13-3dd0-b1b3-8d51d3fbd524', 'Permission grant user group update name', null, 'permissionName'),
       ('662b853c-cf06-3c9f-8ca4-e95642616d75', 'Permission grant user group update description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('48a67e62-dd13-3dd0-b1b3-8d51d3fbd524', 'en', 'Permission grant user group update', 0),
       ('662b853c-cf06-3c9f-8ca4-e95642616d75', 'en', 'Permission grant user group update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000105', 'PERMISSION_GRANT_USER_GROUP_UPDATE',
        '00000000-0000-0000-0005-000000000001', '48a67e62-dd13-3dd0-b1b3-8d51d3fbd524',
        '662b853c-cf06-3c9f-8ca4-e95642616d75')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000105', '00000000-0000-0000-0004-000000000105',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_USER_GROUP_UPDATE


--Start PERMISSION_GRANT_USER_GROUP_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('dad860fa-4018-307d-b2fd-bf4927517fc8', 'Permission grant user group delete name', null, 'permissionName'),
       ('c093a5ba-9839-3679-99fd-749eb272bf52', 'Permission grant user group delete description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('dad860fa-4018-307d-b2fd-bf4927517fc8', 'en', 'Permission grant user group delete', 0),
       ('c093a5ba-9839-3679-99fd-749eb272bf52', 'en', 'Permission grant user group delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000106', 'PERMISSION_GRANT_USER_GROUP_DELETE',
        '00000000-0000-0000-0005-000000000001', 'dad860fa-4018-307d-b2fd-bf4927517fc8',
        'c093a5ba-9839-3679-99fd-749eb272bf52')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000106', '00000000-0000-0000-0004-000000000106',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_USER_GROUP_DELETE


--Start PERMISSION_GRANT_USER_GROUP_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('70a76266-0581-3b5c-8a31-ad7c78f850fb', 'Permission grant user group view name', null, 'permissionName'),
       ('02cd70d8-e4b2-3813-8fe0-5ebb0dd1ff42', 'Permission grant user group view description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('70a76266-0581-3b5c-8a31-ad7c78f850fb', 'en', 'Permission grant user group view', 0),
       ('02cd70d8-e4b2-3813-8fe0-5ebb0dd1ff42', 'en', 'Permission grant user group view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000107', 'PERMISSION_GRANT_USER_GROUP_VIEW',
        '00000000-0000-0000-0005-000000000001', '70a76266-0581-3b5c-8a31-ad7c78f850fb',
        '02cd70d8-e4b2-3813-8fe0-5ebb0dd1ff42')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000107', '00000000-0000-0000-0004-000000000107',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GRANT_USER_GROUP_VIEW


--Start PERMISSION_GROUP_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('f2660528-7154-3215-ad4a-8b6bfc64fb2f', 'Permission group view name', null, 'permissionName'),
       ('4d9e54b5-6103-3ff2-ba17-467c4b011d99', 'Permission group view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('f2660528-7154-3215-ad4a-8b6bfc64fb2f', 'en', 'Permission group view', 0),
       ('4d9e54b5-6103-3ff2-ba17-467c4b011d99', 'en', 'Permission group view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000108', 'PERMISSION_GROUP_VIEW', '00000000-0000-0000-0005-000000000001',
        'f2660528-7154-3215-ad4a-8b6bfc64fb2f', '4d9e54b5-6103-3ff2-ba17-467c4b011d99')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000108', '00000000-0000-0000-0004-000000000108',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_GROUP_VIEW


--Start PERMISSION_SCHEMA_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('d5680b76-9cbb-3519-b1e0-92c8cd2adf91', 'Permission schema view name', null, 'permissionName'),
       ('09b61a05-db3a-3685-acf2-975614fbfc1a', 'Permission schema view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('d5680b76-9cbb-3519-b1e0-92c8cd2adf91', 'en', 'Permission schema view', 0),
       ('09b61a05-db3a-3685-acf2-975614fbfc1a', 'en', 'Permission schema view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000109', 'PERMISSION_SCHEMA_VIEW', '00000000-0000-0000-0005-000000000001',
        'd5680b76-9cbb-3519-b1e0-92c8cd2adf91', '09b61a05-db3a-3685-acf2-975614fbfc1a')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000109', '00000000-0000-0000-0004-000000000109',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PERMISSION_SCHEMA_VIEW


--Start USER_PERMISSION_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('9e816377-cb9a-33df-bcc7-a2d1f620a4b4', 'User permission view name', null, 'permissionName'),
       ('555fbc9f-91d8-3191-b2ff-f041c57b03b5', 'User permission view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('9e816377-cb9a-33df-bcc7-a2d1f620a4b4', 'en', 'User permission view', 0),
       ('555fbc9f-91d8-3191-b2ff-f041c57b03b5', 'en', 'User permission view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000110', 'USER_PERMISSION_VIEW', '00000000-0000-0000-0005-000000000001',
        '9e816377-cb9a-33df-bcc7-a2d1f620a4b4', '555fbc9f-91d8-3191-b2ff-f041c57b03b5')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000110', '00000000-0000-0000-0004-000000000110',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End USER_PERMISSION_VIEW


--Start HISTORY_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('32f71a30-4950-3dc0-9bb2-67aa11f6aa6a', 'History view name', null, 'permissionName'),
       ('da133bd0-07ef-397b-beab-35c2c87ea3d4', 'History view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('32f71a30-4950-3dc0-9bb2-67aa11f6aa6a', 'en', 'History view', 0),
       ('da133bd0-07ef-397b-beab-35c2c87ea3d4', 'en', 'History view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000111', 'HISTORY_VIEW', '00000000-0000-0000-0005-000000000001',
        '32f71a30-4950-3dc0-9bb2-67aa11f6aa6a', 'da133bd0-07ef-397b-beab-35c2c87ea3d4')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000111', '00000000-0000-0000-0004-000000000111',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End HISTORY_VIEW


--Start I18N_MANAGE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('06103191-90bc-3c8a-9f7d-179d7e50aea6', 'I18n manage name', null, 'permissionName'),
       ('3f1209e7-9074-3058-93d9-f6f8d402dfb1', 'I18n manage description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('06103191-90bc-3c8a-9f7d-179d7e50aea6', 'en', 'I18n manage', 0),
       ('3f1209e7-9074-3058-93d9-f6f8d402dfb1', 'en', 'I18n manage', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000112', 'I18N_MANAGE', '00000000-0000-0000-0005-000000000001',
        '06103191-90bc-3c8a-9f7d-179d7e50aea6', '3f1209e7-9074-3058-93d9-f6f8d402dfb1')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000112', '00000000-0000-0000-0004-000000000112',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End I18N_MANAGE


--Start I18N_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('75da91f2-9c1c-3ad8-b567-00accfcebbcd', 'I18n create name', null, 'permissionName'),
       ('88fc89c8-5a0a-379e-8317-2bc3ff74c23c', 'I18n create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('75da91f2-9c1c-3ad8-b567-00accfcebbcd', 'en', 'I18n create', 0),
       ('88fc89c8-5a0a-379e-8317-2bc3ff74c23c', 'en', 'I18n create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000113', 'I18N_CREATE', '00000000-0000-0000-0005-000000000001',
        '75da91f2-9c1c-3ad8-b567-00accfcebbcd', '88fc89c8-5a0a-379e-8317-2bc3ff74c23c')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000113', '00000000-0000-0000-0004-000000000113',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End I18N_CREATE


--Start I18N_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('61fda451-4df0-33ff-b897-75d8c97e6ac8', 'I18n update name', null, 'permissionName'),
       ('5ae2b05e-9102-3cff-9608-af29fece4ee9', 'I18n update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('61fda451-4df0-33ff-b897-75d8c97e6ac8', 'en', 'I18n update', 0),
       ('5ae2b05e-9102-3cff-9608-af29fece4ee9', 'en', 'I18n update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000114', 'I18N_UPDATE', '00000000-0000-0000-0005-000000000001',
        '61fda451-4df0-33ff-b897-75d8c97e6ac8', '5ae2b05e-9102-3cff-9608-af29fece4ee9')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000114', '00000000-0000-0000-0004-000000000114',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End I18N_UPDATE


--Start I18N_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('19459d28-7bc6-3557-9545-a7530bf36a1e', 'I18n delete name', null, 'permissionName'),
       ('bc10c88a-c54b-3424-8bdd-0b83a40d2f84', 'I18n delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('19459d28-7bc6-3557-9545-a7530bf36a1e', 'en', 'I18n delete', 0),
       ('bc10c88a-c54b-3424-8bdd-0b83a40d2f84', 'en', 'I18n delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000115', 'I18N_DELETE', '00000000-0000-0000-0005-000000000001',
        '19459d28-7bc6-3557-9545-a7530bf36a1e', 'bc10c88a-c54b-3424-8bdd-0b83a40d2f84')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000115', '00000000-0000-0000-0004-000000000115',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End I18N_DELETE


--Start I18N_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('9c8eff2a-e694-3894-b8fc-94325ce6cdd7', 'I18n view name', null, 'permissionName'),
       ('e909260d-20d1-3fce-a3cd-ccd0e40e926d', 'I18n view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('9c8eff2a-e694-3894-b8fc-94325ce6cdd7', 'en', 'I18n view', 0),
       ('e909260d-20d1-3fce-a3cd-ccd0e40e926d', 'en', 'I18n view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000116', 'I18N_VIEW', '00000000-0000-0000-0005-000000000001',
        '9c8eff2a-e694-3894-b8fc-94325ce6cdd7', 'e909260d-20d1-3fce-a3cd-ccd0e40e926d')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000116', '00000000-0000-0000-0004-000000000116',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End I18N_VIEW


--Start ERASER_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('b2520aa0-c6b0-3912-add4-82e1e118fb78', 'Eraser create name', null, 'permissionName'),
       ('f93828f0-af4d-36c6-9b81-e8664437944b', 'Eraser create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('b2520aa0-c6b0-3912-add4-82e1e118fb78', 'en', 'Eraser create', 0),
       ('f93828f0-af4d-36c6-9b81-e8664437944b', 'en', 'Eraser create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000117', 'ERASER_CREATE', '00000000-0000-0000-0005-000000000001',
        'b2520aa0-c6b0-3912-add4-82e1e118fb78', 'f93828f0-af4d-36c6-9b81-e8664437944b')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000117', '00000000-0000-0000-0004-000000000117',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End ERASER_CREATE


--Start ERASER_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('68b25da9-0409-370e-9309-8878720f2347', 'Eraser update name', null, 'permissionName'),
       ('2d6af204-bbc1-33da-bdc3-5eae0d181d7c', 'Eraser update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('68b25da9-0409-370e-9309-8878720f2347', 'en', 'Eraser update', 0),
       ('2d6af204-bbc1-33da-bdc3-5eae0d181d7c', 'en', 'Eraser update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000118', 'ERASER_UPDATE', '00000000-0000-0000-0005-000000000001',
        '68b25da9-0409-370e-9309-8878720f2347', '2d6af204-bbc1-33da-bdc3-5eae0d181d7c')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000118', '00000000-0000-0000-0004-000000000118',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End ERASER_UPDATE


--Start ERASER_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('d9241b70-9799-3e77-b091-4b39c9a364bd', 'Eraser delete name', null, 'permissionName'),
       ('2a592699-c4c6-3f87-af01-a60b5d076c83', 'Eraser delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('d9241b70-9799-3e77-b091-4b39c9a364bd', 'en', 'Eraser delete', 0),
       ('2a592699-c4c6-3f87-af01-a60b5d076c83', 'en', 'Eraser delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000119', 'ERASER_DELETE', '00000000-0000-0000-0005-000000000001',
        'd9241b70-9799-3e77-b091-4b39c9a364bd', '2a592699-c4c6-3f87-af01-a60b5d076c83')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000119', '00000000-0000-0000-0004-000000000119',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End ERASER_DELETE


--Start ERASER_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('8603f803-ba33-37bd-bd7b-2e5892931e76', 'Eraser view name', null, 'permissionName'),
       ('fe6aee79-9592-3d38-8fdb-d31f00d31e64', 'Eraser view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('8603f803-ba33-37bd-bd7b-2e5892931e76', 'en', 'Eraser view', 0),
       ('fe6aee79-9592-3d38-8fdb-d31f00d31e64', 'en', 'Eraser view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000120', 'ERASER_VIEW', '00000000-0000-0000-0005-000000000001',
        '8603f803-ba33-37bd-bd7b-2e5892931e76', 'fe6aee79-9592-3d38-8fdb-d31f00d31e64')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000120', '00000000-0000-0000-0004-000000000120',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End ERASER_VIEW


--Start FACTORY_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ea1cb8ea-c730-3b1f-87de-85b9ef2ee3d8', 'Factory create name', null, 'permissionName'),
       ('c17c14ca-cf2c-34dc-b413-ccb8e5fd969e', 'Factory create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ea1cb8ea-c730-3b1f-87de-85b9ef2ee3d8', 'en', 'Factory create', 0),
       ('c17c14ca-cf2c-34dc-b413-ccb8e5fd969e', 'en', 'Factory create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000121', 'FACTORY_CREATE', '00000000-0000-0000-0005-000000000001',
        'ea1cb8ea-c730-3b1f-87de-85b9ef2ee3d8', 'c17c14ca-cf2c-34dc-b413-ccb8e5fd969e')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000121', '00000000-0000-0000-0004-000000000121',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End FACTORY_CREATE


--Start FACTORY_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('081c5264-498f-3472-b5f6-47153bfc8613', 'Factory update name', null, 'permissionName'),
       ('dfb8e8c9-99d5-39ed-a04a-729636ef008f', 'Factory update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('081c5264-498f-3472-b5f6-47153bfc8613', 'en', 'Factory update', 0),
       ('dfb8e8c9-99d5-39ed-a04a-729636ef008f', 'en', 'Factory update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000122', 'FACTORY_UPDATE', '00000000-0000-0000-0005-000000000001',
        '081c5264-498f-3472-b5f6-47153bfc8613', 'dfb8e8c9-99d5-39ed-a04a-729636ef008f')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000122', '00000000-0000-0000-0004-000000000122',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End FACTORY_UPDATE


--Start FACTORY_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('05d3bd3f-88b4-3de3-b001-d7a0b51842a4', 'Factory delete name', null, 'permissionName'),
       ('7d621bcd-bbf3-3af4-8357-9f31c0f7292c', 'Factory delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('05d3bd3f-88b4-3de3-b001-d7a0b51842a4', 'en', 'Factory delete', 0),
       ('7d621bcd-bbf3-3af4-8357-9f31c0f7292c', 'en', 'Factory delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000123', 'FACTORY_DELETE', '00000000-0000-0000-0005-000000000001',
        '05d3bd3f-88b4-3de3-b001-d7a0b51842a4', '7d621bcd-bbf3-3af4-8357-9f31c0f7292c')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000123', '00000000-0000-0000-0004-000000000123',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End FACTORY_DELETE


--Start FACTORY_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('4c481536-f455-369c-aeb3-05e088790037', 'Factory view name', null, 'permissionName'),
       ('00765779-26a1-30ef-880d-2f4368848d24', 'Factory view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('4c481536-f455-369c-aeb3-05e088790037', 'en', 'Factory view', 0),
       ('00765779-26a1-30ef-880d-2f4368848d24', 'en', 'Factory view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000124', 'FACTORY_VIEW', '00000000-0000-0000-0005-000000000001',
        '4c481536-f455-369c-aeb3-05e088790037', '00765779-26a1-30ef-880d-2f4368848d24')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000124', '00000000-0000-0000-0004-000000000124',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End FACTORY_VIEW


--Start MULTIPLIER_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('cde3bf56-b012-33ce-8e21-4f75a65d0ac2', 'Multiplier create name', null, 'permissionName'),
       ('7963c0c1-440c-36ae-9cf5-4ceaced355a3', 'Multiplier create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('cde3bf56-b012-33ce-8e21-4f75a65d0ac2', 'en', 'Multiplier create', 0),
       ('7963c0c1-440c-36ae-9cf5-4ceaced355a3', 'en', 'Multiplier create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000125', 'MULTIPLIER_CREATE', '00000000-0000-0000-0005-000000000001',
        'cde3bf56-b012-33ce-8e21-4f75a65d0ac2', '7963c0c1-440c-36ae-9cf5-4ceaced355a3')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000125', '00000000-0000-0000-0004-000000000125',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End MULTIPLIER_CREATE


--Start MULTIPLIER_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('5e063882-68e2-372b-94b4-c3dda0e49929', 'Multiplier update name', null, 'permissionName'),
       ('8473d39a-31a2-3dfd-87ba-40a9983dce7d', 'Multiplier update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('5e063882-68e2-372b-94b4-c3dda0e49929', 'en', 'Multiplier update', 0),
       ('8473d39a-31a2-3dfd-87ba-40a9983dce7d', 'en', 'Multiplier update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000126', 'MULTIPLIER_UPDATE', '00000000-0000-0000-0005-000000000001',
        '5e063882-68e2-372b-94b4-c3dda0e49929', '8473d39a-31a2-3dfd-87ba-40a9983dce7d')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000126', '00000000-0000-0000-0004-000000000126',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End MULTIPLIER_UPDATE


--Start MULTIPLIER_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ac7ae2d1-1421-3b26-80e3-6509cc55f8be', 'Multiplier delete name', null, 'permissionName'),
       ('604d430b-e9e7-301b-a7d8-6dcff4d394e0', 'Multiplier delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ac7ae2d1-1421-3b26-80e3-6509cc55f8be', 'en', 'Multiplier delete', 0),
       ('604d430b-e9e7-301b-a7d8-6dcff4d394e0', 'en', 'Multiplier delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000127', 'MULTIPLIER_DELETE', '00000000-0000-0000-0005-000000000001',
        'ac7ae2d1-1421-3b26-80e3-6509cc55f8be', '604d430b-e9e7-301b-a7d8-6dcff4d394e0')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000127', '00000000-0000-0000-0004-000000000127',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End MULTIPLIER_DELETE


--Start MULTIPLIER_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('f86f495e-6295-35fe-b460-152d9272203a', 'Multiplier view name', null, 'permissionName'),
       ('4a420499-78d6-390b-817f-5a794547979d', 'Multiplier view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('f86f495e-6295-35fe-b460-152d9272203a', 'en', 'Multiplier view', 0),
       ('4a420499-78d6-390b-817f-5a794547979d', 'en', 'Multiplier view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000128', 'MULTIPLIER_VIEW', '00000000-0000-0000-0005-000000000001',
        'f86f495e-6295-35fe-b460-152d9272203a', '4a420499-78d6-390b-817f-5a794547979d')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000128', '00000000-0000-0000-0004-000000000128',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End MULTIPLIER_VIEW


--Start PIPELINE_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('0cbdd29a-134e-3acd-928f-6db2766093fb', 'Pipeline create name', null, 'permissionName'),
       ('247e7ff6-9856-37c7-9014-b0633aff7e0a', 'Pipeline create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('0cbdd29a-134e-3acd-928f-6db2766093fb', 'en', 'Pipeline create', 0),
       ('247e7ff6-9856-37c7-9014-b0633aff7e0a', 'en', 'Pipeline create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000129', 'PIPELINE_CREATE', '00000000-0000-0000-0005-000000000001',
        '0cbdd29a-134e-3acd-928f-6db2766093fb', '247e7ff6-9856-37c7-9014-b0633aff7e0a')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000129', '00000000-0000-0000-0004-000000000129',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PIPELINE_CREATE


--Start PIPELINE_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('466d30c3-979c-3788-b4dd-6d786e7037d9', 'Pipeline update name', null, 'permissionName'),
       ('08e61fd4-d280-355b-9236-88caead0888f', 'Pipeline update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('466d30c3-979c-3788-b4dd-6d786e7037d9', 'en', 'Pipeline update', 0),
       ('08e61fd4-d280-355b-9236-88caead0888f', 'en', 'Pipeline update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000130', 'PIPELINE_UPDATE', '00000000-0000-0000-0005-000000000001',
        '466d30c3-979c-3788-b4dd-6d786e7037d9', '08e61fd4-d280-355b-9236-88caead0888f')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000130', '00000000-0000-0000-0004-000000000130',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PIPELINE_UPDATE


--Start PIPELINE_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ab69cfdd-d52b-3eb1-8db4-5d6678f7b49e', 'Pipeline delete name', null, 'permissionName'),
       ('2b9f8b03-3377-32cf-853c-2c078b818d78', 'Pipeline delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ab69cfdd-d52b-3eb1-8db4-5d6678f7b49e', 'en', 'Pipeline delete', 0),
       ('2b9f8b03-3377-32cf-853c-2c078b818d78', 'en', 'Pipeline delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000131', 'PIPELINE_DELETE', '00000000-0000-0000-0005-000000000001',
        'ab69cfdd-d52b-3eb1-8db4-5d6678f7b49e', '2b9f8b03-3377-32cf-853c-2c078b818d78')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000131', '00000000-0000-0000-0004-000000000131',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PIPELINE_DELETE


--Start PIPELINE_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('1ed5ba42-9043-36bd-8d26-98dfd52749c5', 'Pipeline view name', null, 'permissionName'),
       ('b254c58e-3771-3dc4-b6fc-87ec6b56a828', 'Pipeline view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('1ed5ba42-9043-36bd-8d26-98dfd52749c5', 'en', 'Pipeline view', 0),
       ('b254c58e-3771-3dc4-b6fc-87ec6b56a828', 'en', 'Pipeline view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000132', 'PIPELINE_VIEW', '00000000-0000-0000-0005-000000000001',
        '1ed5ba42-9043-36bd-8d26-98dfd52749c5', 'b254c58e-3771-3dc4-b6fc-87ec6b56a828')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000132', '00000000-0000-0000-0004-000000000132',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PIPELINE_VIEW


--Start CONDITION_SET_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('42a82605-d9c1-368e-9d63-ca214b4dc77d', 'Condition set create name', null, 'permissionName'),
       ('818158bb-4854-3b22-a3e1-90b563e89d9d', 'Condition set create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('42a82605-d9c1-368e-9d63-ca214b4dc77d', 'en', 'Condition set create', 0),
       ('818158bb-4854-3b22-a3e1-90b563e89d9d', 'en', 'Condition set create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000133', 'CONDITION_SET_CREATE', '00000000-0000-0000-0005-000000000001',
        '42a82605-d9c1-368e-9d63-ca214b4dc77d', '818158bb-4854-3b22-a3e1-90b563e89d9d')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000133', '00000000-0000-0000-0004-000000000133',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End CONDITION_SET_CREATE


--Start CONDITION_SET_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('0d77e4fa-70e3-33be-b10f-811e75f71abe', 'Condition set update name', null, 'permissionName'),
       ('6efb0dad-2016-3638-a84d-35cd9a2939b7', 'Condition set update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('0d77e4fa-70e3-33be-b10f-811e75f71abe', 'en', 'Condition set update', 0),
       ('6efb0dad-2016-3638-a84d-35cd9a2939b7', 'en', 'Condition set update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000134', 'CONDITION_SET_UPDATE', '00000000-0000-0000-0005-000000000001',
        '0d77e4fa-70e3-33be-b10f-811e75f71abe', '6efb0dad-2016-3638-a84d-35cd9a2939b7')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000134', '00000000-0000-0000-0004-000000000134',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End CONDITION_SET_UPDATE


--Start CONDITION_SET_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('bd9980f9-0ec7-30a7-a086-a626c539c18f', 'Condition set delete name', null, 'permissionName'),
       ('da374c1c-3b45-3937-b5cf-79de31f6ba68', 'Condition set delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('bd9980f9-0ec7-30a7-a086-a626c539c18f', 'en', 'Condition set delete', 0),
       ('da374c1c-3b45-3937-b5cf-79de31f6ba68', 'en', 'Condition set delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000135', 'CONDITION_SET_DELETE', '00000000-0000-0000-0005-000000000001',
        'bd9980f9-0ec7-30a7-a086-a626c539c18f', 'da374c1c-3b45-3937-b5cf-79de31f6ba68')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000135', '00000000-0000-0000-0004-000000000135',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End CONDITION_SET_DELETE


--Start CONDITION_SET_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('f201283b-b905-36e6-a93b-c60b35ca458d', 'Condition set view name', null, 'permissionName'),
       ('83d095dc-9dcf-3dd7-951a-e87419c43270', 'Condition set view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('f201283b-b905-36e6-a93b-c60b35ca458d', 'en', 'Condition set view', 0),
       ('83d095dc-9dcf-3dd7-951a-e87419c43270', 'en', 'Condition set view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000136', 'CONDITION_SET_VIEW', '00000000-0000-0000-0005-000000000001',
        'f201283b-b905-36e6-a93b-c60b35ca458d', '83d095dc-9dcf-3dd7-951a-e87419c43270')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000136', '00000000-0000-0000-0004-000000000136',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End CONDITION_SET_VIEW


--Start BRANCH_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('45eb6572-7c6e-358d-b4aa-1808bd13daed', 'Branch create name', null, 'permissionName'),
       ('3abd16c0-3a88-3300-b8f9-5b71bfe2c37c', 'Branch create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('45eb6572-7c6e-358d-b4aa-1808bd13daed', 'en', 'Branch create', 0),
       ('3abd16c0-3a88-3300-b8f9-5b71bfe2c37c', 'en', 'Branch create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000137', 'BRANCH_CREATE', '00000000-0000-0000-0005-000000000001',
        '45eb6572-7c6e-358d-b4aa-1808bd13daed', '3abd16c0-3a88-3300-b8f9-5b71bfe2c37c')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000137', '00000000-0000-0000-0004-000000000137',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End BRANCH_CREATE


--Start BRANCH_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('645d331e-a53d-39bb-82f0-776be2641e00', 'Branch update name', null, 'permissionName'),
       ('00a679f6-d896-3893-aa39-d22fd4c14b4c', 'Branch update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('645d331e-a53d-39bb-82f0-776be2641e00', 'en', 'Branch update', 0),
       ('00a679f6-d896-3893-aa39-d22fd4c14b4c', 'en', 'Branch update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000138', 'BRANCH_UPDATE', '00000000-0000-0000-0005-000000000001',
        '645d331e-a53d-39bb-82f0-776be2641e00', '00a679f6-d896-3893-aa39-d22fd4c14b4c')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000138', '00000000-0000-0000-0004-000000000138',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End BRANCH_UPDATE


--Start BRANCH_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('d1b90be7-6c0e-3639-a239-9acff3520c70', 'Branch delete name', null, 'permissionName'),
       ('d3f03949-57ab-32b0-823a-d9f29cdb18ef', 'Branch delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('d1b90be7-6c0e-3639-a239-9acff3520c70', 'en', 'Branch delete', 0),
       ('d3f03949-57ab-32b0-823a-d9f29cdb18ef', 'en', 'Branch delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000139', 'BRANCH_DELETE', '00000000-0000-0000-0005-000000000001',
        'd1b90be7-6c0e-3639-a239-9acff3520c70', 'd3f03949-57ab-32b0-823a-d9f29cdb18ef')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000139', '00000000-0000-0000-0004-000000000139',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End BRANCH_DELETE


--Start BRANCH_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('62bfb4f7-2bf9-3fc3-a5cc-cef294c3dfe4', 'Branch view name', null, 'permissionName'),
       ('c9263be9-6e56-3889-900d-5dd5ea72f6b4', 'Branch view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('62bfb4f7-2bf9-3fc3-a5cc-cef294c3dfe4', 'en', 'Branch view', 0),
       ('c9263be9-6e56-3889-900d-5dd5ea72f6b4', 'en', 'Branch view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000140', 'BRANCH_VIEW', '00000000-0000-0000-0005-000000000001',
        '62bfb4f7-2bf9-3fc3-a5cc-cef294c3dfe4', 'c9263be9-6e56-3889-900d-5dd5ea72f6b4')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000140', '00000000-0000-0000-0004-000000000140',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End BRANCH_VIEW


--Start DRAFT_COMMIT
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('48221083-3281-3fbc-82c7-fed31befa398', 'Draft commit name', null, 'permissionName'),
       ('ba2d6cfc-b67f-3825-bed4-52b4c32c7aa3', 'Draft commit description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('48221083-3281-3fbc-82c7-fed31befa398', 'en', 'Draft commit', 0),
       ('ba2d6cfc-b67f-3825-bed4-52b4c32c7aa3', 'en', 'Draft commit', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000141', 'DRAFT_COMMIT', '00000000-0000-0000-0005-000000000001',
        '48221083-3281-3fbc-82c7-fed31befa398', 'ba2d6cfc-b67f-3825-bed4-52b4c32c7aa3')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000141', '00000000-0000-0000-0004-000000000141',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DRAFT_COMMIT


--Start DOMAIN_BUSINESS_ACCOUNT_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('5411f20f-a73d-3f0a-8bde-719736f11215', 'Domain business account create name', null, 'permissionName'),
       ('0ff0a31f-8049-3b1b-aff2-d75f6106a38c', 'Domain business account create description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('5411f20f-a73d-3f0a-8bde-719736f11215', 'en', 'Domain business account create', 0),
       ('0ff0a31f-8049-3b1b-aff2-d75f6106a38c', 'en', 'Domain business account create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000142', 'DOMAIN_BUSINESS_ACCOUNT_CREATE',
        '00000000-0000-0000-0005-000000000001', '5411f20f-a73d-3f0a-8bde-719736f11215',
        '0ff0a31f-8049-3b1b-aff2-d75f6106a38c')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000142', '00000000-0000-0000-0004-000000000142',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_BUSINESS_ACCOUNT_CREATE


--Start DOMAIN_BUSINESS_ACCOUNT_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('c955cb5c-e5c2-330d-8648-78881d818621', 'Domain business account update name', null, 'permissionName'),
       ('92220fad-6cf5-3f24-940f-44e7e4aec489', 'Domain business account update description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('c955cb5c-e5c2-330d-8648-78881d818621', 'en', 'Domain business account update', 0),
       ('92220fad-6cf5-3f24-940f-44e7e4aec489', 'en', 'Domain business account update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000143', 'DOMAIN_BUSINESS_ACCOUNT_UPDATE',
        '00000000-0000-0000-0005-000000000001', 'c955cb5c-e5c2-330d-8648-78881d818621',
        '92220fad-6cf5-3f24-940f-44e7e4aec489')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000143', '00000000-0000-0000-0004-000000000143',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_BUSINESS_ACCOUNT_UPDATE


--Start DOMAIN_BUSINESS_ACCOUNT_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('4b54ffc7-d5c8-3ba6-b323-36577f6c857f', 'Domain business account delete name', null, 'permissionName'),
       ('675757a7-e19f-39a4-bcf0-90a8bcff1248', 'Domain business account delete description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('4b54ffc7-d5c8-3ba6-b323-36577f6c857f', 'en', 'Domain business account delete', 0),
       ('675757a7-e19f-39a4-bcf0-90a8bcff1248', 'en', 'Domain business account delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000144', 'DOMAIN_BUSINESS_ACCOUNT_DELETE',
        '00000000-0000-0000-0005-000000000001', '4b54ffc7-d5c8-3ba6-b323-36577f6c857f',
        '675757a7-e19f-39a4-bcf0-90a8bcff1248')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000144', '00000000-0000-0000-0004-000000000144',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_BUSINESS_ACCOUNT_DELETE


--Start DOMAIN_BUSINESS_ACCOUNT_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('9a9fcce6-b058-349f-96f0-d2ceb5107362', 'Domain business account view name', null, 'permissionName'),
       ('6010d33e-c479-37ab-bd0e-e94e2154e056', 'Domain business account view description', null,
        'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('9a9fcce6-b058-349f-96f0-d2ceb5107362', 'en', 'Domain business account view', 0),
       ('6010d33e-c479-37ab-bd0e-e94e2154e056', 'en', 'Domain business account view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000145', 'DOMAIN_BUSINESS_ACCOUNT_VIEW', '00000000-0000-0000-0005-000000000001',
        '9a9fcce6-b058-349f-96f0-d2ceb5107362', '6010d33e-c479-37ab-bd0e-e94e2154e056')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000145', '00000000-0000-0000-0004-000000000145',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_BUSINESS_ACCOUNT_VIEW


--Start DOMAIN_USER_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('3e9fb116-3161-3251-8f22-37658be3409e', 'Domain user create name', null, 'permissionName'),
       ('1b05ce52-955c-3c09-9159-4aad15a190ef', 'Domain user create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('3e9fb116-3161-3251-8f22-37658be3409e', 'en', 'Domain user create', 0),
       ('1b05ce52-955c-3c09-9159-4aad15a190ef', 'en', 'Domain user create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000146', 'DOMAIN_USER_CREATE', '00000000-0000-0000-0005-000000000001',
        '3e9fb116-3161-3251-8f22-37658be3409e', '1b05ce52-955c-3c09-9159-4aad15a190ef')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000146', '00000000-0000-0000-0004-000000000146',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_USER_CREATE


--Start DOMAIN_USER_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('cde6b872-912c-370b-b595-d14771b36138', 'Domain user update name', null, 'permissionName'),
       ('7a4cad58-b5ee-3392-a1fb-30c45f90db22', 'Domain user update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('cde6b872-912c-370b-b595-d14771b36138', 'en', 'Domain user update', 0),
       ('7a4cad58-b5ee-3392-a1fb-30c45f90db22', 'en', 'Domain user update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000147', 'DOMAIN_USER_UPDATE', '00000000-0000-0000-0005-000000000001',
        'cde6b872-912c-370b-b595-d14771b36138', '7a4cad58-b5ee-3392-a1fb-30c45f90db22')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000147', '00000000-0000-0000-0004-000000000147',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_USER_UPDATE


--Start DOMAIN_USER_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('d197a2f9-6336-39d0-b129-520aa276e1fa', 'Domain user delete name', null, 'permissionName'),
       ('faaaacbe-af98-33cf-b36e-de66c2188530', 'Domain user delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('d197a2f9-6336-39d0-b129-520aa276e1fa', 'en', 'Domain user delete', 0),
       ('faaaacbe-af98-33cf-b36e-de66c2188530', 'en', 'Domain user delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000148', 'DOMAIN_USER_DELETE', '00000000-0000-0000-0005-000000000001',
        'd197a2f9-6336-39d0-b129-520aa276e1fa', 'faaaacbe-af98-33cf-b36e-de66c2188530')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000148', '00000000-0000-0000-0004-000000000148',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_USER_DELETE


--Start DOMAIN_USER_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('340fe1eb-68ae-338e-baa1-99c9f5f93726', 'Domain user view name', null, 'permissionName'),
       ('5072a376-6f19-33bf-846b-3d4acbfb813d', 'Domain user view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('340fe1eb-68ae-338e-baa1-99c9f5f93726', 'en', 'Domain user view', 0),
       ('5072a376-6f19-33bf-846b-3d4acbfb813d', 'en', 'Domain user view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000149', 'DOMAIN_USER_VIEW', '00000000-0000-0000-0005-000000000001',
        '340fe1eb-68ae-338e-baa1-99c9f5f93726', '5072a376-6f19-33bf-846b-3d4acbfb813d')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000149', '00000000-0000-0000-0004-000000000149',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End DOMAIN_USER_VIEW


--Start BUSINESS_ACCOUNT_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('07114a9b-8ea2-3cfe-b279-5e0c5c88a195', 'Business account create name', null, 'permissionName'),
       ('f5c386c9-991f-378b-97d0-b1b8ddfe1fb0', 'Business account create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('07114a9b-8ea2-3cfe-b279-5e0c5c88a195', 'en', 'Business account create', 0),
       ('f5c386c9-991f-378b-97d0-b1b8ddfe1fb0', 'en', 'Business account create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000150', 'BUSINESS_ACCOUNT_CREATE', '00000000-0000-0000-0005-000000000001',
        '07114a9b-8ea2-3cfe-b279-5e0c5c88a195', 'f5c386c9-991f-378b-97d0-b1b8ddfe1fb0')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000150', '00000000-0000-0000-0004-000000000150',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End BUSINESS_ACCOUNT_CREATE


--Start BUSINESS_ACCOUNT_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('fc799647-7a24-3c9e-829e-d3af43173d02', 'Business account update name', null, 'permissionName'),
       ('8dcf82b5-226f-30df-91c8-f581d519f378', 'Business account update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('fc799647-7a24-3c9e-829e-d3af43173d02', 'en', 'Business account update', 0),
       ('8dcf82b5-226f-30df-91c8-f581d519f378', 'en', 'Business account update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000151', 'BUSINESS_ACCOUNT_UPDATE', '00000000-0000-0000-0005-000000000001',
        'fc799647-7a24-3c9e-829e-d3af43173d02', '8dcf82b5-226f-30df-91c8-f581d519f378')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000151', '00000000-0000-0000-0004-000000000151',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End BUSINESS_ACCOUNT_UPDATE


--Start BUSINESS_ACCOUNT_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('ab276bf5-0e0e-3345-8b79-f57be2c0bf5b', 'Business account delete name', null, 'permissionName'),
       ('bc09a248-e7a0-3a9c-ac6d-8a0c9d22a001', 'Business account delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('ab276bf5-0e0e-3345-8b79-f57be2c0bf5b', 'en', 'Business account delete', 0),
       ('bc09a248-e7a0-3a9c-ac6d-8a0c9d22a001', 'en', 'Business account delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000152', 'BUSINESS_ACCOUNT_DELETE', '00000000-0000-0000-0005-000000000001',
        'ab276bf5-0e0e-3345-8b79-f57be2c0bf5b', 'bc09a248-e7a0-3a9c-ac6d-8a0c9d22a001')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000152', '00000000-0000-0000-0004-000000000152',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End BUSINESS_ACCOUNT_DELETE


--Start BUSINESS_ACCOUNT_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('154cdeaf-59dc-37f2-adfd-a69ecd62b347', 'Business account view name', null, 'permissionName'),
       ('5bc78ec5-5842-315d-8e84-84518fda3372', 'Business account view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('154cdeaf-59dc-37f2-adfd-a69ecd62b347', 'en', 'Business account view', 0),
       ('5bc78ec5-5842-315d-8e84-84518fda3372', 'en', 'Business account view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000153', 'BUSINESS_ACCOUNT_VIEW', '00000000-0000-0000-0005-000000000001',
        '154cdeaf-59dc-37f2-adfd-a69ecd62b347', '5bc78ec5-5842-315d-8e84-84518fda3372')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000153', '00000000-0000-0000-0004-000000000153',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End BUSINESS_ACCOUNT_VIEW


--Start SPACE_ROLE_MANAGE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('19c39c9d-54c7-358f-a6a8-ae0f3938b0b9', 'Space role manage name', null, 'permissionName'),
       ('66d63225-c46a-33ea-b57a-39cd347d1a49', 'Space role manage description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('19c39c9d-54c7-358f-a6a8-ae0f3938b0b9', 'en', 'Space role manage', 0),
       ('66d63225-c46a-33ea-b57a-39cd347d1a49', 'en', 'Space role manage', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000154', 'SPACE_ROLE_MANAGE', '00000000-0000-0000-0005-000000000001',
        '19c39c9d-54c7-358f-a6a8-ae0f3938b0b9', '66d63225-c46a-33ea-b57a-39cd347d1a49')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000154', '00000000-0000-0000-0004-000000000154',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End SPACE_ROLE_MANAGE


--Start SPACE_ROLE_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('d93d5dbe-33b1-3315-9af7-35fd42eb56ff', 'Space role view name', null, 'permissionName'),
       ('95e9b7af-4c39-3919-9113-8efb5cd7e8b0', 'Space role view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('d93d5dbe-33b1-3315-9af7-35fd42eb56ff', 'en', 'Space role view', 0),
       ('95e9b7af-4c39-3919-9113-8efb5cd7e8b0', 'en', 'Space role view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000155', 'SPACE_ROLE_VIEW', '00000000-0000-0000-0005-000000000001',
        'd93d5dbe-33b1-3315-9af7-35fd42eb56ff', '95e9b7af-4c39-3919-9113-8efb5cd7e8b0')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000155', '00000000-0000-0000-0004-000000000155',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End SPACE_ROLE_VIEW


--Start SYSTEM_APP_INFO_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('a72162da-59c6-3d76-a4af-512c96c37340', 'System app info view name', null, 'permissionName'),
       ('27e72847-99f5-3624-b205-4044ac243697', 'System app info view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('a72162da-59c6-3d76-a4af-512c96c37340', 'en', 'System app info view', 0),
       ('27e72847-99f5-3624-b205-4044ac243697', 'en', 'System app info view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000156', 'SYSTEM_APP_INFO_VIEW', '00000000-0000-0000-0005-000000000001',
        'a72162da-59c6-3d76-a4af-512c96c37340', '27e72847-99f5-3624-b205-4044ac243697')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000156', '00000000-0000-0000-0004-000000000156',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End SYSTEM_APP_INFO_VIEW


--Start LOG_SUBSTITUTION_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('d11277f9-e75a-3aac-a66d-78fb1dd07e4e', 'Log substitution view name', null, 'permissionName'),
       ('3da3c613-235b-351b-9d80-4ccc2abe48bc', 'Log substitution view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('d11277f9-e75a-3aac-a66d-78fb1dd07e4e', 'en', 'Log substitution view', 0),
       ('3da3c613-235b-351b-9d80-4ccc2abe48bc', 'en', 'Log substitution view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000157', 'LOG_SUBSTITUTION_VIEW', '00000000-0000-0000-0005-000000000001',
        'd11277f9-e75a-3aac-a66d-78fb1dd07e4e', '3da3c613-235b-351b-9d80-4ccc2abe48bc')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000157', '00000000-0000-0000-0004-000000000157',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End LOG_SUBSTITUTION_VIEW


--Start FEATURER_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('627e9054-402f-3f85-80b6-af477d0ab0a4', 'Featurer view name', null, 'permissionName'),
       ('0b51f8f0-e8c1-3ab6-9237-943bbdb94904', 'Featurer view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('627e9054-402f-3f85-80b6-af477d0ab0a4', 'en', 'Featurer view', 0),
       ('0b51f8f0-e8c1-3ab6-9237-943bbdb94904', 'en', 'Featurer view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000158', 'FEATURER_VIEW', '00000000-0000-0000-0005-000000000001',
        '627e9054-402f-3f85-80b6-af477d0ab0a4', '0b51f8f0-e8c1-3ab6-9237-943bbdb94904')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000158', '00000000-0000-0000-0004-000000000158',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End FEATURER_VIEW


--Start TIER_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('84b2f85f-e0b5-3cc5-a283-870d89af3f08', 'Tier create name', null, 'permissionName'),
       ('d3efcecb-9919-31d4-befb-94c7d975bc03', 'Tier create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('84b2f85f-e0b5-3cc5-a283-870d89af3f08', 'en', 'Tier create', 0),
       ('d3efcecb-9919-31d4-befb-94c7d975bc03', 'en', 'Tier create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000159', 'TIER_CREATE', '00000000-0000-0000-0005-000000000001',
        '84b2f85f-e0b5-3cc5-a283-870d89af3f08', 'd3efcecb-9919-31d4-befb-94c7d975bc03')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000159', '00000000-0000-0000-0004-000000000159',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TIER_CREATE


--Start TIER_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('fb015edb-da28-3d57-94c2-9baf388707ca', 'Tier update name', null, 'permissionName'),
       ('afb3a0fb-a6f4-3280-9e86-7c2a706122ad', 'Tier update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('fb015edb-da28-3d57-94c2-9baf388707ca', 'en', 'Tier update', 0),
       ('afb3a0fb-a6f4-3280-9e86-7c2a706122ad', 'en', 'Tier update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000160', 'TIER_UPDATE', '00000000-0000-0000-0005-000000000001',
        'fb015edb-da28-3d57-94c2-9baf388707ca', 'afb3a0fb-a6f4-3280-9e86-7c2a706122ad')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000160', '00000000-0000-0000-0004-000000000160',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TIER_UPDATE


--Start TIER_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('e9f049c8-9c01-30bd-99b4-69e4d5aa2578', 'Tier delete name', null, 'permissionName'),
       ('180828db-cb0d-3ac0-b1f5-55183b62beb7', 'Tier delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('e9f049c8-9c01-30bd-99b4-69e4d5aa2578', 'en', 'Tier delete', 0),
       ('180828db-cb0d-3ac0-b1f5-55183b62beb7', 'en', 'Tier delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000161', 'TIER_DELETE', '00000000-0000-0000-0005-000000000001',
        'e9f049c8-9c01-30bd-99b4-69e4d5aa2578', '180828db-cb0d-3ac0-b1f5-55183b62beb7')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000161', '00000000-0000-0000-0004-000000000161',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TIER_DELETE


--Start TIER_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('a9d3c4ff-3f82-3231-bb16-6ba5865cc3e9', 'Tier view name', null, 'permissionName'),
       ('15f89032-ba67-31ee-bb73-98f860df0edc', 'Tier view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('a9d3c4ff-3f82-3231-bb16-6ba5865cc3e9', 'en', 'Tier view', 0),
       ('15f89032-ba67-31ee-bb73-98f860df0edc', 'en', 'Tier view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000162', 'TIER_VIEW', '00000000-0000-0000-0005-000000000001',
        'a9d3c4ff-3f82-3231-bb16-6ba5865cc3e9', '15f89032-ba67-31ee-bb73-98f860df0edc')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000162', '00000000-0000-0000-0004-000000000162',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End TIER_VIEW


--Start FACE_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('3e2e4f4e-4901-3737-83bf-94142f9ed634', 'Face view name', null, 'permissionName'),
       ('90416e09-fd68-33ca-8c93-0df035944d6d', 'Face view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('3e2e4f4e-4901-3737-83bf-94142f9ed634', 'en', 'Face view', 0),
       ('90416e09-fd68-33ca-8c93-0df035944d6d', 'en', 'Face view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000163', 'FACE_VIEW', '00000000-0000-0000-0005-000000000001',
        '3e2e4f4e-4901-3737-83bf-94142f9ed634', '90416e09-fd68-33ca-8c93-0df035944d6d')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000163', '00000000-0000-0000-0004-000000000163',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End FACE_VIEW


--Start PIPELINE_STEP_CREATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('2470da36-faf6-367d-ba33-e47d6f30fe35', 'Pipeline step create name', null, 'permissionName'),
       ('d7da2de3-7e51-35ef-8f09-22b4c219dd08', 'Pipeline step create description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('2470da36-faf6-367d-ba33-e47d6f30fe35', 'en', 'Pipeline step create', 0),
       ('d7da2de3-7e51-35ef-8f09-22b4c219dd08', 'en', 'Pipeline step create', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000164', 'PIPELINE_STEP_CREATE', '00000000-0000-0000-0005-000000000001',
        '2470da36-faf6-367d-ba33-e47d6f30fe35', 'd7da2de3-7e51-35ef-8f09-22b4c219dd08')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000164', '00000000-0000-0000-0004-000000000164',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PIPELINE_STEP_CREATE


--Start PIPELINE_STEP_UPDATE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('5e8a0219-0067-3c7f-a068-c7ebf240cd3c', 'Pipeline step update name', null, 'permissionName'),
       ('ffe943f5-7118-3b4c-86a0-87b5ad126cc7', 'Pipeline step update description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('5e8a0219-0067-3c7f-a068-c7ebf240cd3c', 'en', 'Pipeline step update', 0),
       ('ffe943f5-7118-3b4c-86a0-87b5ad126cc7', 'en', 'Pipeline step update', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000165', 'PIPELINE_STEP_UPDATE', '00000000-0000-0000-0005-000000000001',
        '5e8a0219-0067-3c7f-a068-c7ebf240cd3c', 'ffe943f5-7118-3b4c-86a0-87b5ad126cc7')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000165', '00000000-0000-0000-0004-000000000165',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PIPELINE_STEP_UPDATE


--Start PIPELINE_STEP_DELETE
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('3468ef6b-edac-385f-8988-6020c6725d71', 'Pipeline step delete name', null, 'permissionName'),
       ('d80e168c-cb20-3a49-8313-f8c8c79e3870', 'Pipeline step delete description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('3468ef6b-edac-385f-8988-6020c6725d71', 'en', 'Pipeline step delete', 0),
       ('d80e168c-cb20-3a49-8313-f8c8c79e3870', 'en', 'Pipeline step delete', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000166', 'PIPELINE_STEP_DELETE', '00000000-0000-0000-0005-000000000001',
        '3468ef6b-edac-385f-8988-6020c6725d71', 'd80e168c-cb20-3a49-8313-f8c8c79e3870')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000166', '00000000-0000-0000-0004-000000000166',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PIPELINE_STEP_DELETE


--Start PIPELINE_STEP_VIEW
insert into public.i18n (id, name, key, i18n_type_id)
VALUES ('03b6fe3d-6aa7-3ee5-901c-17cf711d0b37', 'Pipeline step view name', null, 'permissionName'),
       ('19552839-cf9d-383d-a0c2-028c68a8ce00', 'Pipeline step view description', null, 'permissionDescription')
on conflict (id) do update set name         = excluded.name,
                               key          = excluded.key,
                               i18n_type_id = excluded.i18n_type_id;

insert into public.i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('03b6fe3d-6aa7-3ee5-901c-17cf711d0b37', 'en', 'Pipeline step view', 0),
       ('19552839-cf9d-383d-a0c2-028c68a8ce00', 'en', 'Pipeline step view', 0)
on conflict (i18n_id,locale) do update set translation   = excluded.translation,
                                           usage_counter = excluded.usage_counter;;

INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0004-000000000167', 'PIPELINE_STEP_VIEW', '00000000-0000-0000-0005-000000000001',
        '03b6fe3d-6aa7-3ee5-901c-17cf711d0b37', '19552839-cf9d-383d-a0c2-028c68a8ce00')
on conflict (id) do update set key=excluded.key;

INSERT INTO public.permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000167', '00000000-0000-0000-0004-000000000167',
        '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default)
on conflict do nothing;
--End PIPELINE_STEP_VIEW