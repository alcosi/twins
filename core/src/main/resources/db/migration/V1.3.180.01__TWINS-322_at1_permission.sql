-- tabs system permissions for twinfaces
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('f1203df7-a6d9-4b5d-a3ab-75481afe9877', '', null, 'permissionName'),
    ('eebf50d3-3053-42ea-9863-57104ce9b7fe', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('f1203df7-a6d9-4b5d-a3ab-75481afe9877', 'en', 'Domain manage permission', 0),
    ('eebf50d3-3053-42ea-9863-57104ce9b7fe', 'en', 'domain manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000006', 'DOMAIN_MANAGE', '00000000-0000-0000-0005-000000000001', 'f1203df7-a6d9-4b5d-a3ab-75481afe9877', 'eebf50d3-3053-42ea-9863-57104ce9b7fe') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('4cea498e-9738-40e4-93a4-816d6be269e7', '', null, 'permissionName'),
    ('c582243f-4717-406c-8806-30cc8cce43bb', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('4cea498e-9738-40e4-93a4-816d6be269e7', 'en', 'Twin class field manage permission', 0),
    ('c582243f-4717-406c-8806-30cc8cce43bb', 'en', 'twin class field manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000007', 'TWIN_CLASS_FIELD_MANAGE', '00000000-0000-0000-0005-000000000001', '4cea498e-9738-40e4-93a4-816d6be269e7', 'c582243f-4717-406c-8806-30cc8cce43bb') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('232cbe43-9ca5-427a-86c9-74a56c8b8e7f', '', null, 'permissionName'),
    ('782a73d2-5eff-43db-982e-943fd1f72426', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('232cbe43-9ca5-427a-86c9-74a56c8b8e7f', 'en', 'Twin status manage permission', 0),
    ('782a73d2-5eff-43db-982e-943fd1f72426', 'en', 'twin status manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000008', 'TWIN_STATUS_MANAGE', '00000000-0000-0000-0005-000000000001', '232cbe43-9ca5-427a-86c9-74a56c8b8e7f', '782a73d2-5eff-43db-982e-943fd1f72426') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('9d9a97b8-ae09-467b-ad09-4a12ae89abb8', '', null, 'permissionName'),
    ('110a1ef2-be50-4d74-b68b-9a2d28b1b83f', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('9d9a97b8-ae09-467b-ad09-4a12ae89abb8', 'en', 'Twin manage permission', 0),
    ('110a1ef2-be50-4d74-b68b-9a2d28b1b83f', 'en', 'twin manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000009', 'TWIN_MANAGE', '00000000-0000-0000-0005-000000000001', '9d9a97b8-ae09-467b-ad09-4a12ae89abb8', '110a1ef2-be50-4d74-b68b-9a2d28b1b83f') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('1b30d251-b068-4bb8-aaa2-21382feb7d56', '', null, 'permissionName'),
    ('792a5685-751b-496c-b1ef-322d96e1c94a', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('1b30d251-b068-4bb8-aaa2-21382feb7d56', 'en', 'Comment manage permission', 0),
    ('792a5685-751b-496c-b1ef-322d96e1c94a', 'en', 'comment manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000010', 'COMMENT_MANAGE', '00000000-0000-0000-0005-000000000001', '1b30d251-b068-4bb8-aaa2-21382feb7d56', '792a5685-751b-496c-b1ef-322d96e1c94a') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('6b066663-6182-4632-ab63-b53c27d00651', '', null, 'permissionName'),
    ('a585f023-595f-4c53-bb49-a23b48a731fd', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('6b066663-6182-4632-ab63-b53c27d00651', 'en', 'Attachment manage permission', 0),
    ('a585f023-595f-4c53-bb49-a23b48a731fd', 'en', 'attachment manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000011', 'ATTACHMENT_MANAGE', '00000000-0000-0000-0005-000000000001', '6b066663-6182-4632-ab63-b53c27d00651', 'eebf50d3-3053-42ea-9863-57104ce9b7fe') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('a35b40b9-65bf-4a72-aec4-a5c62cae5e3e', '', null, 'permissionName'),
    ('f27a3375-7742-466b-acf3-1581a5dec66f', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('a35b40b9-65bf-4a72-aec4-a5c62cae5e3e', 'en', 'User manage permission', 0),
    ('f27a3375-7742-466b-acf3-1581a5dec66f', 'en', 'user manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000012', 'USER_MANAGE', '00000000-0000-0000-0005-000000000001', 'a35b40b9-65bf-4a72-aec4-a5c62cae5e3e', 'f27a3375-7742-466b-acf3-1581a5dec66f') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('49dfc3a4-4dc7-4769-8463-0b05f4a17b2d', '', null, 'permissionName'),
    ('fdc14ecd-bf6a-48bc-a570-caa1088d7b65', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('49dfc3a4-4dc7-4769-8463-0b05f4a17b2d', 'en', 'User group manage permission', 0),
    ('fdc14ecd-bf6a-48bc-a570-caa1088d7b65', 'en', 'user group manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000013', 'USER_GROUP_MANAGE', '00000000-0000-0000-0005-000000000001', '49dfc3a4-4dc7-4769-8463-0b05f4a17b2d', 'fdc14ecd-bf6a-48bc-a570-caa1088d7b65') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('89806276-a5d1-4bf5-9e29-d2387bf6cfd8', '', null, 'permissionName'),
    ('c118e27b-b756-43a4-b722-66ea2443d68d', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('89806276-a5d1-4bf5-9e29-d2387bf6cfd8', 'en', 'Data list manage permission', 0),
    ('c118e27b-b756-43a4-b722-66ea2443d68d', 'en', 'data list manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000014', 'DATA_LIST_MANAGE', '00000000-0000-0000-0005-000000000001', '89806276-a5d1-4bf5-9e29-d2387bf6cfd8', 'c118e27b-b756-43a4-b722-66ea2443d68d') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('31b339c3-f8f5-4474-b2d4-8adf752f916d', '', null, 'permissionName'),
    ('a781f6da-184e-425e-a582-7194dbbb651a', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('31b339c3-f8f5-4474-b2d4-8adf752f916d', 'en', 'Data list option manage permission', 0),
    ('a781f6da-184e-425e-a582-7194dbbb651a', 'en', 'data list option manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000015', 'DATA_LIST_OPTION_MANAGE', '00000000-0000-0000-0005-000000000001', '31b339c3-f8f5-4474-b2d4-8adf752f916d', 'a781f6da-184e-425e-a582-7194dbbb651a') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('4a59d325-5218-4aa1-959e-e406a46f8e51', '', null, 'permissionName'),
    ('5cf2fe15-8958-4be0-87f9-a37816b853b3', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('4a59d325-5218-4aa1-959e-e406a46f8e51', 'en', 'Data list subset manage permission', 0),
    ('5cf2fe15-8958-4be0-87f9-a37816b853b3', 'en', 'data list subset manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000016', 'DATA_LIST_SUBSET_MANAGE', '00000000-0000-0000-0005-000000000001', '4a59d325-5218-4aa1-959e-e406a46f8e51', '5cf2fe15-8958-4be0-87f9-a37816b853b3') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('e3833ff4-e5b2-410d-a48a-1aaada9c681c', '', null, 'permissionName'),
    ('5019537b-27a9-4603-8671-2abfcc9be1cf', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('e3833ff4-e5b2-410d-a48a-1aaada9c681c', 'en', 'Permission manage permission', 0),
    ('5019537b-27a9-4603-8671-2abfcc9be1cf', 'en', 'permission manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000017', 'PERMISSION_MANAGE', '00000000-0000-0000-0005-000000000001', 'e3833ff4-e5b2-410d-a48a-1aaada9c681c', '5019537b-27a9-4603-8671-2abfcc9be1cf') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('1751502c-8da7-4b32-8101-62df0f9ad11e', '', null, 'permissionName'),
    ('22626457-54f4-4927-9d05-ee8cb13b237c', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('1751502c-8da7-4b32-8101-62df0f9ad11e', 'en', 'Permission group manage permission', 0),
    ('22626457-54f4-4927-9d05-ee8cb13b237c', 'en', 'permission group manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000018', 'PERMISSION_GROUP_MANAGE', '00000000-0000-0000-0005-000000000001', '1751502c-8da7-4b32-8101-62df0f9ad11e', '22626457-54f4-4927-9d05-ee8cb13b237c') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('cdc74f7c-2894-4b01-a981-e6441d0aacaf', '', null, 'permissionName'),
    ('41689330-3a80-48a3-870e-2ae48237eea9', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('cdc74f7c-2894-4b01-a981-e6441d0aacaf', 'en', 'Permission schema manage permission', 0),
    ('41689330-3a80-48a3-870e-2ae48237eea9', 'en', 'permission schema manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000019', 'PERMISSION_SCHEMA_MANAGE', '00000000-0000-0000-0005-000000000001', 'cdc74f7c-2894-4b01-a981-e6441d0aacaf', '41689330-3a80-48a3-870e-2ae48237eea9') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('1d03aee8-a358-48b0-9a77-b7cffeb6afe0', '', null, 'permissionName'),
    ('ba202321-5091-4da0-95b0-8fc6fcc0110a', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('1d03aee8-a358-48b0-9a77-b7cffeb6afe0', 'en', 'Factory manage permission', 0),
    ('ba202321-5091-4da0-95b0-8fc6fcc0110a', 'en', 'factory manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000020', 'FACTORY_MANAGE', '00000000-0000-0000-0005-000000000001', '1d03aee8-a358-48b0-9a77-b7cffeb6afe0', 'ba202321-5091-4da0-95b0-8fc6fcc0110a') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('aaebc4a6-92a3-4f66-8394-9824ecda27ea', '', null, 'permissionName'),
    ('e0e27b29-abb9-4e80-ad3f-cca9b3819fdc', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('aaebc4a6-92a3-4f66-8394-9824ecda27ea', 'en', 'multiplier manage permission', 0),
    ('e0e27b29-abb9-4e80-ad3f-cca9b3819fdc', 'en', 'multiplier manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000021', 'MULTIPLIER_MANAGE', '00000000-0000-0000-0005-000000000001', 'aaebc4a6-92a3-4f66-8394-9824ecda27ea', 'e0e27b29-abb9-4e80-ad3f-cca9b3819fdc') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('1f752658-3594-4b35-8311-76aa11d21468', '', null, 'permissionName'),
    ('d639ec60-a9c3-4584-8572-b22538f3c189', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('1f752658-3594-4b35-8311-76aa11d21468', 'en', 'Multiplier param manage permission', 0),
    ('d639ec60-a9c3-4584-8572-b22538f3c189', 'en', 'multiplier param manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000022', 'MULTIPLIER_PARAM_MANAGE', '00000000-0000-0000-0005-000000000001', '1f752658-3594-4b35-8311-76aa11d21468', 'd639ec60-a9c3-4584-8572-b22538f3c189') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('2d35015c-2446-4339-8cad-b60b9dbac6c8', '', null, 'permissionName'),
    ('9e4026c0-1496-4fae-aa83-e2a973f91bc0', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('2d35015c-2446-4339-8cad-b60b9dbac6c8', 'en', 'Pipeline manage permission', 0),
    ('9e4026c0-1496-4fae-aa83-e2a973f91bc0', 'en', 'pipeline manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000023', 'PIPELINE_MANAGE', '00000000-0000-0000-0005-000000000001', '2d35015c-2446-4339-8cad-b60b9dbac6c8', '9e4026c0-1496-4fae-aa83-e2a973f91bc0') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('4bca9177-8301-42dd-b947-bf93d1d75c8a', '', null, 'permissionName'),
    ('006df45e-bcf8-4aa8-8776-4decb7c6f537', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('4bca9177-8301-42dd-b947-bf93d1d75c8a', 'en', 'Pipeline step manage permission', 0),
    ('006df45e-bcf8-4aa8-8776-4decb7c6f537', 'en', 'pipeline step manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000024', 'PIPELINE_STEP_MANAGE', '00000000-0000-0000-0005-000000000001', '4bca9177-8301-42dd-b947-bf93d1d75c8a', '006df45e-bcf8-4aa8-8776-4decb7c6f537') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('6bec0d9e-646e-4de3-8527-8d43af615a71', '', null, 'permissionName'),
    ('c0275859-5465-40eb-96c9-47e56f23ecbd', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('6bec0d9e-646e-4de3-8527-8d43af615a71', 'en', 'Branch manage permission', 0),
    ('c0275859-5465-40eb-96c9-47e56f23ecbd', 'en', 'branch manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000025', 'BRANCH_MANAGE', '00000000-0000-0000-0005-000000000001', '6bec0d9e-646e-4de3-8527-8d43af615a71', 'c0275859-5465-40eb-96c9-47e56f23ecbd') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('bfcaf5aa-2c7e-49f1-bb71-72bddb8a0abf', '', null, 'permissionName'),
    ('9ab910b0-6054-4412-a034-c00c1e136414', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('bfcaf5aa-2c7e-49f1-bb71-72bddb8a0abf', 'en', 'Eraser manage permission', 0),
    ('9ab910b0-6054-4412-a034-c00c1e136414', 'en', 'eraser manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000026', 'ERASER_MANAGE', '00000000-0000-0000-0005-000000000001', 'bfcaf5aa-2c7e-49f1-bb71-72bddb8a0abf', '9ab910b0-6054-4412-a034-c00c1e136414') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('bb4e4298-469b-4cb7-baa2-a5e1932709a4', '', null, 'permissionName'),
    ('123e3aae-5b83-4e61-8e1f-e5b4ec602d60', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('bb4e4298-469b-4cb7-baa2-a5e1932709a4', 'en', 'Condition set manage permission', 0),
    ('123e3aae-5b83-4e61-8e1f-e5b4ec602d60', 'en', 'condition set manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000027', 'CONDITION_SET_MANAGE', '00000000-0000-0000-0005-000000000001', 'bb4e4298-469b-4cb7-baa2-a5e1932709a4', '123e3aae-5b83-4e61-8e1f-e5b4ec602d60') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('52f0d0f1-ee39-4616-9fa9-dfa4e2e588d2', '', null, 'permissionName'),
    ('4a0fe586-47b6-4b9b-b613-934961c272fe', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('52f0d0f1-ee39-4616-9fa9-dfa4e2e588d2', 'en', 'Twinflow manage permission', 0),
    ('4a0fe586-47b6-4b9b-b613-934961c272fe', 'en', 'twinflow manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000028', 'TWINFLOW_MANAGE', '00000000-0000-0000-0005-000000000001', '52f0d0f1-ee39-4616-9fa9-dfa4e2e588d2', '4a0fe586-47b6-4b9b-b613-934961c272fe') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('33ec937c-70d8-4cec-ab08-dc3c02e02076', '', null, 'permissionName'),
    ('54bfd6f8-0030-4e3c-adc4-1d3d8c778019', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('33ec937c-70d8-4cec-ab08-dc3c02e02076', 'en', 'Twinflow schema permission', 0),
    ('54bfd6f8-0030-4e3c-adc4-1d3d8c778019', 'en', 'twinflow schema manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000029', 'TWINFLOW_SCHEMA_MANAGE', '00000000-0000-0000-0005-000000000001', '33ec937c-70d8-4cec-ab08-dc3c02e02076', '54bfd6f8-0030-4e3c-adc4-1d3d8c778019') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('2c4ec981-4e53-4968-9ffe-215e69cee856', '', null, 'permissionName'),
    ('9b688865-81be-4ff1-9ea6-3e0d5f88ed01', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('2c4ec981-4e53-4968-9ffe-215e69cee856', 'en', 'Business account manage permission', 0),
    ('9b688865-81be-4ff1-9ea6-3e0d5f88ed01', 'en', 'business account manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000030', 'BUSINESS_ACCOUNT_MANAGE', '00000000-0000-0000-0005-000000000001', '2c4ec981-4e53-4968-9ffe-215e69cee856', '9b688865-81be-4ff1-9ea6-3e0d5f88ed01') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('b871973e-2f36-483e-a899-6ab754a2739c', '', null, 'permissionName'),
    ('88eafed6-b864-45ff-bc99-0485dd8ac06c', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('b871973e-2f36-483e-a899-6ab754a2739c', 'en', 'Tier manage permission', 0),
    ('88eafed6-b864-45ff-bc99-0485dd8ac06c', 'en', 'tier manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000031', 'TIER_MANAGE', '00000000-0000-0000-0005-000000000001', 'b871973e-2f36-483e-a899-6ab754a2739c', '88eafed6-b864-45ff-bc99-0485dd8ac06c') on conflict on constraint permission_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES
    ('7f7175c8-6d23-48b3-9bd2-2277ccf03f95', '', null, 'permissionName'),
    ('f3c8d5cd-1c08-4e29-bc09-df293e1ab44b', '', null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
    ('7f7175c8-6d23-48b3-9bd2-2277ccf03f95', 'en', 'Featurer manage permission', 0),
    ('f3c8d5cd-1c08-4e29-bc09-df293e1ab44b', 'en', 'featurer manage permission', 0) on conflict on constraint i18n_translation_uq do nothing;
INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES ('00000000-0000-0000-0004-000000000032', 'FEATURER_MANAGE', '00000000-0000-0000-0005-000000000001', '7f7175c8-6d23-48b3-9bd2-2277ccf03f95', 'f3c8d5cd-1c08-4e29-bc09-df293e1ab44b') on conflict on constraint permission_pk do nothing ;

-- set for user group -> systemScopeDomainManage
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000005', '00000000-0000-0000-0004-000000000006', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000006', '00000000-0000-0000-0004-000000000007', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000007', '00000000-0000-0000-0004-000000000008', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000008', '00000000-0000-0000-0004-000000000009', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000009', '00000000-0000-0000-0004-000000000010', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000010', '00000000-0000-0000-0004-000000000011', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000011', '00000000-0000-0000-0004-000000000012', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000012', '00000000-0000-0000-0004-000000000013', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000013', '00000000-0000-0000-0004-000000000014', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000014', '00000000-0000-0000-0004-000000000015', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000015', '00000000-0000-0000-0004-000000000016', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000016', '00000000-0000-0000-0004-000000000017', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000017', '00000000-0000-0000-0004-000000000018', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000018', '00000000-0000-0000-0004-000000000019', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000019', '00000000-0000-0000-0004-000000000020', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000020', '00000000-0000-0000-0004-000000000021', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000021', '00000000-0000-0000-0004-000000000022', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000022', '00000000-0000-0000-0004-000000000023', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000023', '00000000-0000-0000-0004-000000000024', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000024', '00000000-0000-0000-0004-000000000025', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000025', '00000000-0000-0000-0004-000000000026', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000026', '00000000-0000-0000-0004-000000000027', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000027', '00000000-0000-0000-0004-000000000028', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000028', '00000000-0000-0000-0004-000000000029', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000029', '00000000-0000-0000-0004-000000000030', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000030', '00000000-0000-0000-0004-000000000031', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
INSERT INTO public.permission_grant_global (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES ('00000000-0000-0000-0007-000000000031', '00000000-0000-0000-0004-000000000032', '00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0000-000000000000', default) on conflict on constraint permission_grant_global_pk do nothing ;
