-- configuring WNR simple navbar example

insert into face values ('b929c624-368c-4a5f-84b7-0522f3257e3b', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'NB001', 'WNR navigation bar', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('d425b8a6-9855-4baa-ae29-99b6b0bfb446', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG001', 'Projects page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('f0e1c3b8-d88f-46a3-b013-e52a7cdff34f', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG001', 'Tasks page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('61f47cfe-5ea3-44f1-b007-effa920fceb9', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG001', 'Tools page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('0663cb2f-0a0e-437b-a81d-a2f74df04295', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG001', 'Supplies page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('07abc204-e1f4-45ff-8585-1379fa34ba3d', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'WT001', 'All projects table widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('040bdc99-3288-40b3-892f-65a470cd0aa8', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'WT001', 'All tasks table widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('b3a5c11c-3f07-42cc-bb04-be1da3291439', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'WT001', 'All tools table widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('11407415-838d-4bea-b837-d62cc5017045', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'WT001', 'All supplies table widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into i18n values ('7a5e326e-7152-4d15-9c27-15ac64bc17be', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('cef1c0ff-68ec-4a86-b796-b20f546da276', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('7a5e326e-7152-4d15-9c27-15ac64bc17be', 'en', 'Settings') on conflict do nothing ;
insert into i18n_translation values ('cef1c0ff-68ec-4a86-b796-b20f546da276', 'en', 'User area') on conflict do nothing ;
insert into face_navbar_nb001 values ('b929c624-368c-4a5f-84b7-0522f3257e3b', '7a5e326e-7152-4d15-9c27-15ac64bc17be', null,  'cef1c0ff-68ec-4a86-b796-b20f546da276', null) on conflict do nothing;
insert into i18n values ('77a4eee9-41cf-4747-adb9-088d9d88c953', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('88cdaaeb-5f10-4f69-9bff-d284824bb142', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('6c4ec589-423d-4e36-959d-6ac4b21a2b77', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('7b7c59b3-2fcc-470e-a1a1-81d78560074e', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('77a4eee9-41cf-4747-adb9-088d9d88c953', 'en', 'Projects') on conflict do nothing ;
insert into i18n_translation values ('88cdaaeb-5f10-4f69-9bff-d284824bb142', 'en', 'Tasks') on conflict do nothing ;
insert into i18n_translation values ('6c4ec589-423d-4e36-959d-6ac4b21a2b77', 'en', 'Tools') on conflict do nothing ;
insert into i18n_translation values ('7b7c59b3-2fcc-470e-a1a1-81d78560074e', 'en', 'Supplies') on conflict do nothing ;
insert into resource values ('0d3f5ec8-7538-44b5-8a27-536fb1813546', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', '00000000-0000-0000-0007-000000000002', 'https://www.svgrepo.com/show/532810/folder.svg', 'folder.svg', '00000000-0000-0000-0000-000000000000', 0, now());
insert into resource values ('a336688e-ca02-4fcc-98c2-3389f00245f0', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', '00000000-0000-0000-0007-000000000002', 'https://www.svgrepo.com/show/334303/task.svg', 'task.svg', '00000000-0000-0000-0000-000000000000', 0, now());
insert into resource values ('b7b10ea0-b4f1-41a4-808f-73c9609efd81', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', '00000000-0000-0000-0007-000000000002', 'https://www.svgrepo.com/show/471992/tool-02.svg', 'tool-02.svg', '00000000-0000-0000-0000-000000000000', 0, now());
insert into resource values ('bce7f395-6903-44e2-9841-105a57af1601', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', '00000000-0000-0000-0007-000000000002', 'https://www.svgrepo.com/show/372317/container.svg', 'container.svg', '00000000-0000-0000-0000-000000000000', 0, now());
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, icon_resource_id, face_navbar_nb001_status_id, target_page_face_id) values ('ea954367-2e2b-4b36-ac37-0afc54e9d6fc','b929c624-368c-4a5f-84b7-0522f3257e3b', 'projects', '77a4eee9-41cf-4747-adb9-088d9d88c953', '0d3f5ec8-7538-44b5-8a27-536fb1813546', 'ACTIVE', 'd425b8a6-9855-4baa-ae29-99b6b0bfb446') on conflict do nothing;
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, icon_resource_id, face_navbar_nb001_status_id, target_page_face_id) values ('f30e3369-6f35-45c2-a333-94ac510b39e2','b929c624-368c-4a5f-84b7-0522f3257e3b', 'tasks',    '88cdaaeb-5f10-4f69-9bff-d284824bb142', 'a336688e-ca02-4fcc-98c2-3389f00245f0', 'ACTIVE', 'f0e1c3b8-d88f-46a3-b013-e52a7cdff34f') on conflict do nothing;
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, icon_resource_id, face_navbar_nb001_status_id, target_page_face_id) values ('b53cde28-d5b1-4dad-83a5-8572d0bd8293','b929c624-368c-4a5f-84b7-0522f3257e3b', 'tools',    '6c4ec589-423d-4e36-959d-6ac4b21a2b77', 'b7b10ea0-b4f1-41a4-808f-73c9609efd81', 'ACTIVE', '61f47cfe-5ea3-44f1-b007-effa920fceb9') on conflict do nothing;
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, icon_resource_id, face_navbar_nb001_status_id, target_page_face_id) values ('c5ab6edb-dda3-4d0f-a9cd-a512bc849f7d','b929c624-368c-4a5f-84b7-0522f3257e3b', 'supplies', '7b7c59b3-2fcc-470e-a1a1-81d78560074e', 'bce7f395-6903-44e2-9841-105a57af1601', 'ACTIVE', '0663cb2f-0a0e-437b-a81d-a2f74df04295') on conflict do nothing;
insert into i18n values ('b0908c8c-73a2-46a9-80b5-f60621aab4f6', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('ea886b84-676e-42c8-afd8-2995d53d64b7', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('ed00371a-0e94-4a4c-bd11-360a12354beb', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('8c685ec3-42cb-41e3-baf5-df4a301e287b', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('b0908c8c-73a2-46a9-80b5-f60621aab4f6', 'en', 'Projects') on conflict do nothing ;
insert into i18n_translation values ('ea886b84-676e-42c8-afd8-2995d53d64b7', 'en', 'Tasks') on conflict do nothing ;
insert into i18n_translation values ('ed00371a-0e94-4a4c-bd11-360a12354beb', 'en', 'Tools') on conflict do nothing ;
insert into i18n_translation values ('8c685ec3-42cb-41e3-baf5-df4a301e287b', 'en', 'Supplies') on conflict do nothing ;
insert into face_page_pg001 (face_id, title_i18n_id, face_page_pg001_layout_id) values ('d425b8a6-9855-4baa-ae29-99b6b0bfb446', 'b0908c8c-73a2-46a9-80b5-f60621aab4f6', 'ONE_COLUMN') on conflict do nothing;
insert into face_page_pg001 (face_id, title_i18n_id, face_page_pg001_layout_id) values ('f0e1c3b8-d88f-46a3-b013-e52a7cdff34f', 'ea886b84-676e-42c8-afd8-2995d53d64b7', 'ONE_COLUMN') on conflict do nothing;
insert into face_page_pg001 (face_id, title_i18n_id, face_page_pg001_layout_id) values ('61f47cfe-5ea3-44f1-b007-effa920fceb9', 'ed00371a-0e94-4a4c-bd11-360a12354beb', 'ONE_COLUMN') on conflict do nothing;
insert into face_page_pg001 (face_id, title_i18n_id, face_page_pg001_layout_id) values ('0663cb2f-0a0e-437b-a81d-a2f74df04295', '8c685ec3-42cb-41e3-baf5-df4a301e287b', 'ONE_COLUMN') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('b7b67c9c-b810-485d-9edc-dac500140243', 'd425b8a6-9855-4baa-ae29-99b6b0bfb446', 1, 1, '07abc204-e1f4-45ff-8585-1379fa34ba3d') on conflict do nothing;
-- unsupported widget creation
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('2c4df067-6bec-4fd3-a9b8-30915aa051d5', 'd425b8a6-9855-4baa-ae29-99b6b0bfb446', 1, 2, '07abc204-e1f4-45ff-8585-1379fa34ba3d') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('a713d757-a180-4f94-bbac-c100c1f6123f', 'f0e1c3b8-d88f-46a3-b013-e52a7cdff34f', 1, 1, '040bdc99-3288-40b3-892f-65a470cd0aa8') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('a9be080f-4fc1-42e0-8ccc-eee603cce971', '61f47cfe-5ea3-44f1-b007-effa920fceb9', 1, 1, 'b3a5c11c-3f07-42cc-bb04-be1da3291439') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('93662b84-b488-44e4-9f3c-25737080509d', '0663cb2f-0a0e-437b-a81d-a2f74df04295', 1, 1, '11407415-838d-4bea-b837-d62cc5017045') on conflict do nothing;

insert into i18n values ('45e8f935-3565-43df-ad50-00efcf2b9b39', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('6ad93758-d2e7-4dda-a23c-b86ea04ec3a8', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('3298a373-80fd-4e02-a6d6-851d66d63cdc', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('5ede3e35-f56a-4ea1-9e08-a543e1fa4d08', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('45e8f935-3565-43df-ad50-00efcf2b9b39', 'en', 'All projects') on conflict do nothing ;
insert into i18n_translation values ('6ad93758-d2e7-4dda-a23c-b86ea04ec3a8', 'en', 'All tasks') on conflict do nothing ;
insert into i18n_translation values ('3298a373-80fd-4e02-a6d6-851d66d63cdc', 'en', 'All tools') on conflict do nothing ;
insert into i18n_translation values ('5ede3e35-f56a-4ea1-9e08-a543e1fa4d08', 'en', 'All supplies') on conflict do nothing ;

insert into face_widget_wt001 (face_id, key, label_i18n_id, twin_class_id, search_id, show_columns) VALUES ('07abc204-e1f4-45ff-8585-1379fa34ba3d', 'projects'  , '45e8f935-3565-43df-ad50-00efcf2b9b39', '458c6d7d-99c8-4d87-89c6-2f72d0f5d673', 'd7d9b3cf-5312-4342-87da-1872132579c7', '{assignee,createdAt}') on conflict do nothing ;
insert into face_widget_wt001 (face_id, key, label_i18n_id, twin_class_id, search_id, show_columns) VALUES ('040bdc99-3288-40b3-892f-65a470cd0aa8', 'tasks'     , '6ad93758-d2e7-4dda-a23c-b86ea04ec3a8', '7c027b60-0f6c-445c-9889-8ee3855d2c59', null, null) on conflict do nothing ;
insert into face_widget_wt001 (face_id, key, label_i18n_id, twin_class_id, search_id, show_columns) VALUES ('b3a5c11c-3f07-42cc-bb04-be1da3291439', 'tools'     , '3298a373-80fd-4e02-a6d6-851d66d63cdc', 'ab750e98-70dd-404e-8164-4e0daa187164', 'd7d9b3cf-5312-4342-87da-1872132579c7', null) on conflict do nothing ;
insert into face_widget_wt001 (face_id, key, label_i18n_id, twin_class_id, search_id, show_columns) VALUES ('11407415-838d-4bea-b837-d62cc5017045', 'supplies'  , '5ede3e35-f56a-4ea1-9e08-a543e1fa4d08', '841ba0d4-d2ae-46cf-918f-d24e07703da8', null, null) on conflict do nothing ;
update domain set navbar_face_id = 'b929c624-368c-4a5f-84b7-0522f3257e3b' where id = 'f67ad556-dd27-4871-9a00-16fb0e8a4102' and domain.navbar_face_id is null;



-- unsupported page creation
insert into face_component values ('PG999', 'PAGE', 'Some currently unsupported page')  on conflict do nothing ;
insert into face_component values ('WT999', 'WIDGET', 'Some currently unsupported widget')  on conflict do nothing ;
insert into face values ('814ec8cf-6164-4649-806b-5a352412aae1', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG999', 'Unsupported page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('93e57cce-cee5-45c2-8c72-3f4e9a249422', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'WT999', 'Unsupported widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into i18n values ('25dbc22b-8d7c-4107-bf21-653834784692', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('3aa7de34-f2e2-438d-b20d-a03c030aa511', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('25dbc22b-8d7c-4107-bf21-653834784692', 'en', 'Storages') on conflict do nothing ;
insert into i18n_translation values ('3aa7de34-f2e2-438d-b20d-a03c030aa511', 'en', 'Unsupported') on conflict do nothing ;
insert into resource values ('9270a9e7-a17f-4cba-99a0-80587a3ae4ce', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', '00000000-0000-0000-0007-000000000002', 'https://www.svgrepo.com/show/302817/file-cabinet.svg', 'storage.svg', '00000000-0000-0000-0000-000000000000', 0, now());
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, icon_resource_id, face_navbar_nb001_status_id, target_page_face_id) values ('eb56cbe2-4f0a-478d-a2b9-e9f9ec19e973','b929c624-368c-4a5f-84b7-0522f3257e3b', 'storages', '25dbc22b-8d7c-4107-bf21-653834784692', '9270a9e7-a17f-4cba-99a0-80587a3ae4ce', 'ACTIVE', '814ec8cf-6164-4649-806b-5a352412aae1') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('cc740162-1566-4d8e-baec-10ea8825866c', '0663cb2f-0a0e-437b-a81d-a2f74df04295', 1, 2, '93e57cce-cee5-45c2-8c72-3f4e9a249422') on conflict do nothing;

--TOOL CARD
--TW001, TW002
insert into face values ('11f0d837-e84e-473e-9bfa-a16b3aa75bdb', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'TW001', 'Tool image gallery', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('042824f1-fc92-4dac-a8b9-7573fc8a8543', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'TW002', 'Tool description localization', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('66a43fbc-409c-4fb5-8963-ccaddeb80ab2', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG001', 'Tool page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;

update twin_class set page_face_id = '66a43fbc-409c-4fb5-8963-ccaddeb80ab2' where id = 'ab750e98-70dd-404e-8164-4e0daa187164';
-- update twin_class set page_face_id = '66a43fbc-409c-4fb5-8963-ccaddeb80ab2' where id = 'c9fc9ddd-fbe8-4b5f-ba29-a8408df569f3';

insert into i18n values ('9127aae6-d712-4769-88fa-53030cdfea0f', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('9127aae6-d712-4769-88fa-53030cdfea0f', 'en', 'Tool') on conflict do nothing ;
insert into face_page_pg001 (face_id, title_i18n_id, face_page_pg001_layout_id) values ('66a43fbc-409c-4fb5-8963-ccaddeb80ab2', '9127aae6-d712-4769-88fa-53030cdfea0f', 'ONE_COLUMN') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('86f90d84-930c-402f-9551-dce470c377b8', '66a43fbc-409c-4fb5-8963-ccaddeb80ab2', 1, 1, '11f0d837-e84e-473e-9bfa-a16b3aa75bdb') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('d088c5ac-b065-40b1-8be4-8fbb2aefddf8', '66a43fbc-409c-4fb5-8963-ccaddeb80ab2', 1, 2, '042824f1-fc92-4dac-a8b9-7573fc8a8543') on conflict do nothing;

insert into face_twidget_tw001 (face_id, key) VALUES ('11f0d837-e84e-473e-9bfa-a16b3aa75bdb', 'image_gallery') on conflict do nothing ;
insert into face_twidget_tw002 (face_id, key, label_i18n_id, i18n_twin_class_field_id) VALUES ('042824f1-fc92-4dac-a8b9-7573fc8a8543', 'image_gallery', null, 'b33eb6fd-abd7-4161-b22c-d400f6ab7845') on conflict do nothing ;
insert into i18n values ('99982b47-7da4-4f0e-b388-dba94990f4ce', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('0b8ef1d3-265a-47bb-9149-247c30200f81', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('99982b47-7da4-4f0e-b388-dba94990f4ce', 'en', 'English translaion') on conflict do nothing ;
insert into i18n_translation values ('0b8ef1d3-265a-47bb-9149-247c30200f81', 'en', 'Polish translation') on conflict do nothing ;

insert into face_twidget_tw002_accordion_item (id, face_id, locale, label_i18n_id) VALUES ('2c282928-e6e6-4dae-bf53-db1789cfea5d', '042824f1-fc92-4dac-a8b9-7573fc8a8543', 'en', '99982b47-7da4-4f0e-b388-dba94990f4ce') on conflict do nothing ;
insert into face_twidget_tw002_accordion_item (id, face_id, locale, label_i18n_id) VALUES ('057ebf15-40d1-4fc9-9fea-dbd8a16a1fea', '042824f1-fc92-4dac-a8b9-7573fc8a8543', 'pl', '0b8ef1d3-265a-47bb-9149-247c30200f81') on conflict do nothing ;


--TASK CARD
--TW001, TW002
insert into face values ('3b510111-8634-4915-905b-c5472f155fee', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'TW001', 'Task image gallery', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('29b07cd8-5bc7-4d5e-bc93-582fb822769c', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'TW002', 'Task description localization', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('c9379447-1af6-4f83-9e1b-7ecdf0e582e2', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG001', 'Task page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;

update twin_class set page_face_id = 'c9379447-1af6-4f83-9e1b-7ecdf0e582e2' where id = '7c027b60-0f6c-445c-9889-8ee3855d2c59';
-- update twin_class set page_face_id = 'c9379447-1af6-4f83-9e1b-7ecdf0e582e2' where id = 'c9fc9ddd-fbe8-4b5f-ba29-a8408df569f3';

insert into i18n values ('9127aae6-d712-4769-88fa-53030cdfea0f', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('9127aae6-d712-4769-88fa-53030cdfea0f', 'en', 'Tool') on conflict do nothing ;
insert into face_page_pg001 (face_id, title_i18n_id, face_page_pg001_layout_id) values ('c9379447-1af6-4f83-9e1b-7ecdf0e582e2', '9127aae6-d712-4769-88fa-53030cdfea0f', 'TWO_COLUMNS') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('e7858515-86f7-4783-8c77-09fda9ae8c2d', 'c9379447-1af6-4f83-9e1b-7ecdf0e582e2', 1, 1, '3b510111-8634-4915-905b-c5472f155fee') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('30f8fb89-1de5-46f0-920c-3e98e1f4f41a', 'c9379447-1af6-4f83-9e1b-7ecdf0e582e2', 2, 1, '29b07cd8-5bc7-4d5e-bc93-582fb822769c') on conflict do nothing;

insert into face_twidget_tw001 (face_id, key) VALUES ('3b510111-8634-4915-905b-c5472f155fee', 'image_gallery') on conflict do nothing ;
insert into face_twidget_tw002 (face_id, key, label_i18n_id, i18n_twin_class_field_id) VALUES ('29b07cd8-5bc7-4d5e-bc93-582fb822769c', 'image_gallery', null, 'b33eb6fd-abd7-4161-b22c-d400f6ab7845') on conflict do nothing ;
insert into i18n values ('3ac56dfe-1e67-4d1c-b392-af96e146a718', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('c5741ce8-59cb-47e3-8c13-2eb44a18216c', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('3ac56dfe-1e67-4d1c-b392-af96e146a718', 'en', 'English translation') on conflict do nothing ;
insert into i18n_translation values ('c5741ce8-59cb-47e3-8c13-2eb44a18216c', 'en', 'Polish translation') on conflict do nothing ;

insert into face_twidget_tw002_accordion_item (id, face_id, locale, label_i18n_id) VALUES ('2c282928-e6e6-4dae-bf53-db1789cfea5d', '29b07cd8-5bc7-4d5e-bc93-582fb822769c', 'en', '3ac56dfe-1e67-4d1c-b392-af96e146a718') on conflict do nothing ;
insert into face_twidget_tw002_accordion_item (id, face_id, locale, label_i18n_id) VALUES ('057ebf15-40d1-4fc9-9fea-dbd8a16a1fea', '29b07cd8-5bc7-4d5e-bc93-582fb822769c', 'pl', 'c5741ce8-59cb-47e3-8c13-2eb44a18216c') on conflict do nothing ;

--TOOL CART
--TW001, TW002, PG002
insert into face values ('e2813e86-9973-452e-ba12-9016c231fe02', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'TW001', 'Project image gallery', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('6d953791-dc10-423d-a45f-13b7c4d722d8', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'TW002', 'Project description localization', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('392cdff0-abfc-4d4c-83dc-26fdef55b16a', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', 'PG002', 'Project page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;

update twin_class set page_face_id = '392cdff0-abfc-4d4c-83dc-26fdef55b16a' where id = '458c6d7d-99c8-4d87-89c6-2f72d0f5d673';

insert into i18n values ('6c142450-545c-4393-8042-2c46dff62467', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('6c142450-545c-4393-8042-2c46dff62467', 'en', 'Project') on conflict do nothing ;
insert into face_page_pg002 (face_id, title_i18n_id, face_page_pg002_layout_id) values ('392cdff0-abfc-4d4c-83dc-26fdef55b16a', '6c142450-545c-4393-8042-2c46dff62467', 'TOP') on conflict do nothing;
insert into i18n values ('7b437bb5-41aa-49f2-9ea2-ab64ce42ec79', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('3b2a0443-7154-4413-9380-58752cf5dbaa', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('7b437bb5-41aa-49f2-9ea2-ab64ce42ec79', 'en', 'General') on conflict do nothing ;
insert into i18n_translation values ('3b2a0443-7154-4413-9380-58752cf5dbaa', 'en', 'Tasks') on conflict do nothing ;
insert into resource values ('135b46ee-0fb2-4283-b5a0-c3f645e655e9', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', '00000000-0000-0000-0007-000000000002', 'https://www.svgrepo.com/show/532810/folder.svg', 'project.svg', '00000000-0000-0000-0000-000000000000', 0, now());
insert into resource values ('07bf280f-0cfd-4a57-98d9-c36a31a2fa4b', 'f67ad556-dd27-4871-9a00-16fb0e8a4102', '00000000-0000-0000-0007-000000000002', 'https://www.svgrepo.com/show/497576/task-square.svg', 'tasks.svg', '00000000-0000-0000-0000-000000000000', 0, now());
insert into face_page_pg002_tab (id, face_id, title_i18n_id, icon_resource_id, face_page_pg002_tab_layout_id) values ('7a4c8ca4-0573-47c4-a7bf-33cbce37dc00', '392cdff0-abfc-4d4c-83dc-26fdef55b16a', '7b437bb5-41aa-49f2-9ea2-ab64ce42ec79', '135b46ee-0fb2-4283-b5a0-c3f645e655e9', 'TWO_COLUMNS') on conflict do nothing;
insert into face_page_pg002_tab (id, face_id, title_i18n_id, icon_resource_id, face_page_pg002_tab_layout_id) values ('fa874081-3d5b-4acf-a7d0-0501d6229af5', '392cdff0-abfc-4d4c-83dc-26fdef55b16a', '3b2a0443-7154-4413-9380-58752cf5dbaa', '07bf280f-0cfd-4a57-98d9-c36a31a2fa4b', 'ONE_COLUMN') on conflict do nothing;

insert into face_page_pg002_widget (id, face_page_pg002_tab_id, "column", "row", widget_face_id) values ('1a502323-e718-47ae-9b7d-93762228c125', '7a4c8ca4-0573-47c4-a7bf-33cbce37dc00', 1, 1, 'e2813e86-9973-452e-ba12-9016c231fe02') on conflict do nothing;
insert into face_page_pg002_widget (id, face_page_pg002_tab_id, "column", "row", widget_face_id) values ('bc743a7c-b7ae-4f23-8501-c627fefc9889', '7a4c8ca4-0573-47c4-a7bf-33cbce37dc00', 2, 1, '6d953791-dc10-423d-a45f-13b7c4d722d8') on conflict do nothing;

insert into face_twidget_tw001 (face_id, key) VALUES ('e2813e86-9973-452e-ba12-9016c231fe02', 'image_gallery') on conflict do nothing ;
insert into face_twidget_tw002 (face_id, key, label_i18n_id, i18n_twin_class_field_id) VALUES ('6d953791-dc10-423d-a45f-13b7c4d722d8', 'image_gallery', null, 'b33eb6fd-abd7-4161-b22c-d400f6ab7845') on conflict do nothing ;
insert into i18n values ('b1fe7c7d-b44f-4942-b484-e627ff1ae80d', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('c13fd376-cd58-474f-98c3-9d9a67154cd6', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('b1fe7c7d-b44f-4942-b484-e627ff1ae80d', 'en', 'English translation') on conflict do nothing ;
insert into i18n_translation values ('c13fd376-cd58-474f-98c3-9d9a67154cd6', 'en', 'Polish translation') on conflict do nothing ;

insert into face_twidget_tw002_accordion_item (id, face_id, locale, label_i18n_id) VALUES ('2c282928-e6e6-4dae-bf53-db1789cfea5d', '6d953791-dc10-423d-a45f-13b7c4d722d8', 'en', 'b1fe7c7d-b44f-4942-b484-e627ff1ae80d') on conflict do nothing ;
insert into face_twidget_tw002_accordion_item (id, face_id, locale, label_i18n_id) VALUES ('057ebf15-40d1-4fc9-9fea-dbd8a16a1fea', '6d953791-dc10-423d-a45f-13b7c4d722d8', 'pl', 'c13fd376-cd58-474f-98c3-9d9a67154cd6') on conflict do nothing ;


