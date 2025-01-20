DELETE FROM public.data_list WHERE id = 'cb174b0d-aed3-4ec9-94db-2e69828cfbf0'::uuid;

-- creating i18n_type
INSERT INTO public.i18n_type (id, name) VALUES ('dataListDescription', 'Data list description') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO public.i18n_type (id, name) VALUES ('dataListName', 'Data list name') on conflict on constraint i18n_type_pk do nothing ;

-- creating i18n (for name and description)
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('4b2c133f-07cf-4944-b0c9-f4322cf696ce', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('175729c1-da45-4023-b751-cdd0acad295b', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('790c0043-591d-4ac3-8ab6-cfec06d77f4a', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('9f281355-d586-43a9-8ee5-97999e498214', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('61a8949a-4a13-4ba0-b94c-9d42311b3d00', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('5a259f9e-8267-4618-b913-84efdb2813b5', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('a6f7dc82-0a59-4b0a-8a8f-d1a0d7756c84', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('b4e19283-4364-4891-be68-81b15c81d2e2', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('b11a8030-9282-4c0e-b8d8-9768cb225a6b', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('e35d7220-f495-4267-a510-3433b8346db8', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('a00872c5-5405-4fc1-bc26-a95696ce9a5c', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('13406f33-e087-45bf-8841-ba24e98bc871', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('49a91da3-f03d-488d-83be-80c5e6ec3cf9', 'default', null, 'dataListName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('80f0f850-9fa2-4002-86ec-911cc2fe0aba', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('718c53e5-3d8c-462b-896e-0149145e63e0', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('d52cfd60-80c2-4c80-8af5-c3ba7e1bae26', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('1dd1f714-9241-4e1c-a9ed-820fc3046361', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('4769935c-ea90-4966-9fb7-ad1ad1ba7e02', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('0157a8a9-8827-4b5f-bf7e-031346b58a06', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('f0ce723a-01c0-4f77-aaf5-8c0114bb7234', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('f10ede23-589d-4635-8ab6-51a26cacb9cb', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('8f6b836e-86ff-4946-8eb4-ce12f81ddd52', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('12fe92f4-f8c7-462b-8e85-141ec5942dd8', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('b1453d9f-1e0f-431c-9c28-c734bbf4098d', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('affdc4ec-6b72-4d41-b892-640672b5ab16', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('b7e2a5da-5a50-4c08-99e4-7fa2e68efc38', 'default', null, 'dataListDescription') on conflict on constraint i18n_pkey do nothing ;

-- creating translation
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('4b2c133f-07cf-4944-b0c9-f4322cf696ce', 'en', 'Education', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('175729c1-da45-4023-b751-cdd0acad295b', 'en', 'Tools brand list', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('790c0043-591d-4ac3-8ab6-cfec06d77f4a', 'en', 'Time unit from week', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('9f281355-d586-43a9-8ee5-97999e498214', 'en', 'Qualification specialization', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('61a8949a-4a13-4ba0-b94c-9d42311b3d00', 'en', 'ID Documents type', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('5a259f9e-8267-4618-b913-84efdb2813b5', 'en', 'Currency', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('a6f7dc82-0a59-4b0a-8a8f-d1a0d7756c84', 'en', 'Unit of measure', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('b4e19283-4364-4891-be68-81b15c81d2e2', 'en', 'Tool transfer type', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('b11a8030-9282-4c0e-b8d8-9768cb225a6b', 'en', 'Tool marker list', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('e35d7220-f495-4267-a510-3433b8346db8', 'en', 'Supply tag list', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('a00872c5-5405-4fc1-bc26-a95696ce9a5c', 'en', 'Tool tag list', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('13406f33-e087-45bf-8841-ba24e98bc871', 'en', 'Supply portion marker list', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('49a91da3-f03d-488d-83be-80c5e6ec3cf9', 'en', 'Country list', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('80f0f850-9fa2-4002-86ec-911cc2fe0aba', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('718c53e5-3d8c-462b-896e-0149145e63e0', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('d52cfd60-80c2-4c80-8af5-c3ba7e1bae26', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('1dd1f714-9241-4e1c-a9ed-820fc3046361', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('4769935c-ea90-4966-9fb7-ad1ad1ba7e02', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('0157a8a9-8827-4b5f-bf7e-031346b58a06', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('f0ce723a-01c0-4f77-aaf5-8c0114bb7234', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('f10ede23-589d-4635-8ab6-51a26cacb9cb', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('8f6b836e-86ff-4946-8eb4-ce12f81ddd52', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('12fe92f4-f8c7-462b-8e85-141ec5942dd8', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('b1453d9f-1e0f-431c-9c28-c734bbf4098d', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('affdc4ec-6b72-4d41-b892-640672b5ab16', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('b7e2a5da-5a50-4c08-99e4-7fa2e68efc38', 'en', null, default) on conflict on constraint i18n_translation_uq do nothing ;


UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = '82fe0e1f-a2f7-4fa2-bdac-6580f8090381'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = 'f93e5042-877a-441d-94e5-36aac33be3c9'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = '332d0131-35fd-4586-9d47-dd6dbf758dca'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = 'f64302c0-a324-4bc0-9b52-abdd362a036e'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = '3d8f8d85-4124-40b6-9ccc-62c455414bab'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = '8ffbb153-eb70-47f4-8049-dd5a7feb974c'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = 'e844a4e5-1c09-474e-816f-05cdb1f093ed'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = 'a56d84f1-b821-4914-bf11-87cd77fa00d8'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = 'b97e8fa9-f5e7-4dc7-a4e8-dd3901fe5039'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = '2166bfc2-1e48-4142-99cd-55480683bc7d'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = '66c0d4f4-b176-41c6-aae9-003ae42b45c0'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = 'f6c548c4-37fd-4f08-9f9b-e0526d7cdb8b'::uuid;
UPDATE public.data_list SET name_i18n_id = null::uuid WHERE id = '4bbbee01-aac8-49df-b1f8-1d8f85329f12'::uuid;

UPDATE public.data_list SET name_i18n_id = '49a91da3-f03d-488d-83be-80c5e6ec3cf9'::uuid, description_i18n_id = 'b7e2a5da-5a50-4c08-99e4-7fa2e68efc38'::uuid WHERE id = '66c0d4f4-b176-41c6-aae9-003ae42b45c0'::uuid;
UPDATE public.data_list SET name_i18n_id = '5a259f9e-8267-4618-b913-84efdb2813b5'::uuid, description_i18n_id = '0157a8a9-8827-4b5f-bf7e-031346b58a06'::uuid WHERE id = '4bbbee01-aac8-49df-b1f8-1d8f85329f12'::uuid;
UPDATE public.data_list SET name_i18n_id = 'a00872c5-5405-4fc1-bc26-a95696ce9a5c'::uuid, description_i18n_id = 'b1453d9f-1e0f-431c-9c28-c734bbf4098d'::uuid WHERE id = '332d0131-35fd-4586-9d47-dd6dbf758dca'::uuid;
UPDATE public.data_list SET name_i18n_id = '9f281355-d586-43a9-8ee5-97999e498214'::uuid, description_i18n_id = '1dd1f714-9241-4e1c-a9ed-820fc3046361'::uuid WHERE id = '2166bfc2-1e48-4142-99cd-55480683bc7d'::uuid;
UPDATE public.data_list SET name_i18n_id = '790c0043-591d-4ac3-8ab6-cfec06d77f4a'::uuid, description_i18n_id = 'd52cfd60-80c2-4c80-8af5-c3ba7e1bae26'::uuid WHERE id = 'b97e8fa9-f5e7-4dc7-a4e8-dd3901fe5039'::uuid;
UPDATE public.data_list SET name_i18n_id = '4b2c133f-07cf-4944-b0c9-f4322cf696ce'::uuid, description_i18n_id = '80f0f850-9fa2-4002-86ec-911cc2fe0aba'::uuid WHERE id = 'a56d84f1-b821-4914-bf11-87cd77fa00d8'::uuid;
UPDATE public.data_list SET name_i18n_id = 'b11a8030-9282-4c0e-b8d8-9768cb225a6b'::uuid, description_i18n_id = '8f6b836e-86ff-4946-8eb4-ce12f81ddd52'::uuid WHERE id = '82fe0e1f-a2f7-4fa2-bdac-6580f8090381'::uuid;
UPDATE public.data_list SET name_i18n_id = '61a8949a-4a13-4ba0-b94c-9d42311b3d00'::uuid, description_i18n_id = '4769935c-ea90-4966-9fb7-ad1ad1ba7e02'::uuid WHERE id = '3d8f8d85-4124-40b6-9ccc-62c455414bab'::uuid;
UPDATE public.data_list SET name_i18n_id = '175729c1-da45-4023-b751-cdd0acad295b'::uuid, description_i18n_id = '718c53e5-3d8c-462b-896e-0149145e63e0'::uuid WHERE id = 'e844a4e5-1c09-474e-816f-05cdb1f093ed'::uuid;
UPDATE public.data_list SET name_i18n_id = 'a6f7dc82-0a59-4b0a-8a8f-d1a0d7756c84'::uuid, description_i18n_id = 'f0ce723a-01c0-4f77-aaf5-8c0114bb7234'::uuid WHERE id = '8ffbb153-eb70-47f4-8049-dd5a7feb974c'::uuid;
UPDATE public.data_list SET name_i18n_id = '13406f33-e087-45bf-8841-ba24e98bc871'::uuid, description_i18n_id = 'affdc4ec-6b72-4d41-b892-640672b5ab16'::uuid WHERE id = 'f93e5042-877a-441d-94e5-36aac33be3c9'::uuid;
UPDATE public.data_list SET name_i18n_id = 'e35d7220-f495-4267-a510-3433b8346db8'::uuid, description_i18n_id = '12fe92f4-f8c7-462b-8e85-141ec5942dd8'::uuid WHERE id = 'f64302c0-a324-4bc0-9b52-abdd362a036e'::uuid;
UPDATE public.data_list SET name_i18n_id = 'b4e19283-4364-4891-be68-81b15c81d2e2'::uuid, description_i18n_id = 'f10ede23-589d-4635-8ab6-51a26cacb9cb'::uuid WHERE id = 'f6c548c4-37fd-4f08-9f9b-e0526d7cdb8b'::uuid;

INSERT INTO public.i18n_type (id, name) VALUES ('dataListAttributeName', 'Data list attribute name') on conflict on constraint i18n_type_pk do nothing ;

INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('e6b37934-d68b-4d11-9d40-7857805038ea', 'default', null, 'dataListAttributeName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('51c3390c-f2a6-4226-a23c-eff6433816ca', 'default', null, 'dataListAttributeName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('6c5078c3-5b83-4843-8ab3-ecfac261adc6', 'default', null, 'dataListAttributeName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('856557c1-9b37-489f-b63f-f907b5816d1a', 'default', null, 'dataListAttributeName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('ef0ff285-b0ce-440b-853a-1aae2f8ea6f8', 'default', null, 'dataListAttributeName') on conflict on constraint i18n_pkey do nothing ;

INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('e6b37934-d68b-4d11-9d40-7857805038ea', 'en', 'Ico code', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('51c3390c-f2a6-4226-a23c-eff6433816ca', 'en', 'Num code', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('6c5078c3-5b83-4843-8ab3-ecfac261adc6', 'en', 'Color', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('856557c1-9b37-489f-b63f-f907b5816d1a', 'en', 'Phone code', default) on conflict on constraint i18n_translation_uq do nothing ;
INSERT INTO public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES ('ef0ff285-b0ce-440b-853a-1aae2f8ea6f8', 'en', 'Phone pattern', default) on conflict on constraint i18n_translation_uq do nothing ;

UPDATE public.data_list SET attribute_1_name_i18n_id = '6c5078c3-5b83-4843-8ab3-ecfac261adc6'::uuid WHERE id = 'f93e5042-877a-441d-94e5-36aac33be3c9'::uuid;
UPDATE public.data_list SET attribute_1_name_i18n_id = '6c5078c3-5b83-4843-8ab3-ecfac261adc6'::uuid WHERE id = '82fe0e1f-a2f7-4fa2-bdac-6580f8090381'::uuid;
UPDATE public.data_list SET attribute_1_name_i18n_id = 'e6b37934-d68b-4d11-9d40-7857805038ea'::uuid, attribute_2_name_i18n_id = '856557c1-9b37-489f-b63f-f907b5816d1a'::uuid, attribute_3_name_i18n_id = 'ef0ff285-b0ce-440b-853a-1aae2f8ea6f8'::uuid WHERE id = '66c0d4f4-b176-41c6-aae9-003ae42b45c0'::uuid;
UPDATE public.data_list SET attribute_1_name_i18n_id = '51c3390c-f2a6-4226-a23c-eff6433816ca'::uuid WHERE id = '4bbbee01-aac8-49df-b1f8-1d8f85329f12'::uuid;
