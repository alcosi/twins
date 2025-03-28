-- configuring WNR simple navbar example

insert into face values ('eca56eab-44b7-48ae-8b70-849cbb99a263', '0bc892b6-ef88-47c4-ad92-19cc89576f65', 'NB001', 'Onshelves navigation bar', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('a3d30213-a3ba-4858-9b5c-ebcaf2060764', '0bc892b6-ef88-47c4-ad92-19cc89576f65', 'PG001', 'Products', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('19a27370-f6f5-4056-9e73-ca2645fae098', '0bc892b6-ef88-47c4-ad92-19cc89576f65', 'PG001', 'Marketplaces', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('ae8de8e7-c99f-42f1-98a0-d2cb3d44350e', '0bc892b6-ef88-47c4-ad92-19cc89576f65', 'WT001', 'All products table widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('4fdf673c-3714-4f33-9047-ce43be0808ec', '0bc892b6-ef88-47c4-ad92-19cc89576f65', 'WT001', 'All marketplaces table widget', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into i18n values ('91eae11b-a245-45f6-a38f-33d88c283e7b', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('6d464941-c718-4b2c-bf45-f99510bbb9d8', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('91eae11b-a245-45f6-a38f-33d88c283e7b', 'en', 'Settings') on conflict do nothing ;
insert into i18n_translation values ('6d464941-c718-4b2c-bf45-f99510bbb9d8', 'en', 'Onshelves') on conflict do nothing ;
insert into face_navbar_nb001 values ('eca56eab-44b7-48ae-8b70-849cbb99a263', '91eae11b-a245-45f6-a38f-33d88c283e7b', null,  '6d464941-c718-4b2c-bf45-f99510bbb9d8', null) on conflict do nothing;
insert into i18n values ('c5d1a7a9-1271-4017-942c-7ca96544fd0e', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('64943c21-13d5-462d-8636-da109746453a', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('c5d1a7a9-1271-4017-942c-7ca96544fd0e', 'en', 'Products') on conflict do nothing ;
insert into i18n_translation values ('64943c21-13d5-462d-8636-da109746453a', 'en', 'Marketplaces') on conflict do nothing ;

INSERT INTO public.storage (id, domain_id, storager_featurer_id, storager_params, description, created_at, updated_at) VALUES ('00000000-0000-0000-0007-000000000002', null, 2903, 'selfHostDomainBaseUri => /, supportedMimeTypes => "*/ico,*/icns,*/ico,*/svg,*/svg+xml,*/webp,*/png,*/gif,*/jpeg,*/jpg,*/jpeg-lossless", fileSizeLimit => 1000000', 'External url storage', '2025-02-07 10:02:47.844931', '2025-02-07 10:02:47.844931');
insert into resource values ('bc70cd65-33e0-4bf1-848f-8a827c70b3cc', '0bc892b6-ef88-47c4-ad92-19cc89576f65', '00000000-0000-0000-0007-000000000002', 'https://www.svgrepo.com/show/505500/shirt.svg', 'shirt.svg', '00000000-0000-0000-0000-000000000000', 0, now());
insert into resource values ('6165731d-f2ea-42a2-a100-c1825025c68b', '0bc892b6-ef88-47c4-ad92-19cc89576f65', '00000000-0000-0000-0007-000000000002', 'https://www.svgrepo.com/show/487551/marketplace.svg', 'marketplace.svg', '00000000-0000-0000-0000-000000000000', 0, now());
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, icon_resource_id, face_navbar_nb001_status_id, target_page_face_id) values ('4ee6ffb0-340d-458e-9aa1-30298e6e4633','eca56eab-44b7-48ae-8b70-849cbb99a263', 'products', 'c5d1a7a9-1271-4017-942c-7ca96544fd0e', 'bc70cd65-33e0-4bf1-848f-8a827c70b3cc', 'ACTIVE', 'a3d30213-a3ba-4858-9b5c-ebcaf2060764') on conflict do nothing;
insert into face_navbar_nb001_menu_items (id, face_id, key, label_i18n_id, icon_resource_id, face_navbar_nb001_status_id, target_page_face_id) values ('326f04b8-304a-4e06-a453-b62d8441e0eb','eca56eab-44b7-48ae-8b70-849cbb99a263', 'marketplaces',    '64943c21-13d5-462d-8636-da109746453a', '6165731d-f2ea-42a2-a100-c1825025c68b', 'ACTIVE', '19a27370-f6f5-4056-9e73-ca2645fae098') on conflict do nothing;
insert into i18n values ('cef56958-6d5f-4455-aa0e-2e9dcd1f6025', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('fa8cdbc5-b069-4764-a194-fbc9be4190f4', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('cef56958-6d5f-4455-aa0e-2e9dcd1f6025', 'en', 'Products') on conflict do nothing ;
insert into i18n_translation values ('fa8cdbc5-b069-4764-a194-fbc9be4190f4', 'en', 'Marketplaces') on conflict do nothing ;

insert into face_page_pg001 (face_id, title_i18n_id, face_page_pg001_layout_id) values ('a3d30213-a3ba-4858-9b5c-ebcaf2060764', 'cef56958-6d5f-4455-aa0e-2e9dcd1f6025', 'ONE_COLUMN') on conflict do nothing;
insert into face_page_pg001 (face_id, title_i18n_id, face_page_pg001_layout_id) values ('19a27370-f6f5-4056-9e73-ca2645fae098', 'fa8cdbc5-b069-4764-a194-fbc9be4190f4', 'ONE_COLUMN') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('84fd0447-8f11-47fa-add6-3144c15639ab', 'a3d30213-a3ba-4858-9b5c-ebcaf2060764', 1, 1, 'ae8de8e7-c99f-42f1-98a0-d2cb3d44350e') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('ef74c994-608e-4963-a0bc-676b3111f7b6', '19a27370-f6f5-4056-9e73-ca2645fae098', 1, 1, '4fdf673c-3714-4f33-9047-ce43be0808ec') on conflict do nothing;

insert into i18n values ('02c5b728-228c-49f3-9440-d34ed512317b', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('6c86c353-0111-45fc-b835-a567911d1c90', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('02c5b728-228c-49f3-9440-d34ed512317b', 'en', 'All products') on conflict do nothing ;
insert into i18n_translation values ('6c86c353-0111-45fc-b835-a567911d1c90', 'en', 'All marketplaces') on conflict do nothing ;

insert into face_widget_wt001 (face_id, key, label_i18n_id, twin_class_id, search_id, show_columns) VALUES ('ae8de8e7-c99f-42f1-98a0-d2cb3d44350e', 'products'  , '02c5b728-228c-49f3-9440-d34ed512317b', 'c9fc9ddd-fbe8-4b5f-ba29-a8408df569f3', null, '{assignee,createdAt}') on conflict do nothing ;
insert into face_widget_wt001 (face_id, key, label_i18n_id, twin_class_id, search_id, show_columns) VALUES ('4fdf673c-3714-4f33-9047-ce43be0808ec', 'marketplaces'     , '6c86c353-0111-45fc-b835-a567911d1c90', '895861bd-9bd3-4d89-b1c3-32bd58022aa6', null, '{assignee,createdAt}') on conflict do nothing ;
update domain set navbar_face_id = 'eca56eab-44b7-48ae-8b70-849cbb99a263' where id = '0bc892b6-ef88-47c4-ad92-19cc89576f65' and domain.navbar_face_id is null;

--TW001, TW002
insert into face values ('85279869-60a5-437f-bd46-15e4683899f3', '0bc892b6-ef88-47c4-ad92-19cc89576f65', 'TW001', 'Product image gallery', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('8d919e34-7d57-4eaa-a884-6decf19718f0', '0bc892b6-ef88-47c4-ad92-19cc89576f65', 'TW002', 'Product description localization', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;
insert into face values ('2639883f-3b16-4792-8125-d3d101fe85f0', '0bc892b6-ef88-47c4-ad92-19cc89576f65', 'PG001', 'Product page', '', now(), '00000000-0000-0000-0000-000000000000') on conflict do nothing ;

update twin_class set page_face_id = '2639883f-3b16-4792-8125-d3d101fe85f0' where id = 'c9fc9ddd-fbe8-4b5f-ba29-a8408df569f3';
-- update twin_class set page_face_id = '2639883f-3b16-4792-8125-d3d101fe85f0' where id = 'c9fc9ddd-fbe8-4b5f-ba29-a8408df569f3';
insert into i18n values ('bdb47d2e-989d-4656-a2d4-eef8efcb0c0d', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('bdb47d2e-989d-4656-a2d4-eef8efcb0c0d', 'en', 'Product') on conflict do nothing ;
insert into face_page_pg001 (face_id, title_i18n_id, face_page_pg001_layout_id) values ('2639883f-3b16-4792-8125-d3d101fe85f0', 'bdb47d2e-989d-4656-a2d4-eef8efcb0c0d', 'ONE_COLUMN') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('86f90d84-930c-402f-9551-dce470c377b8', '2639883f-3b16-4792-8125-d3d101fe85f0', 1, 1, '85279869-60a5-437f-bd46-15e4683899f3') on conflict do nothing;
insert into face_page_pg001_widget (id, face_id, "column", "row", widget_face_id) values ('d088c5ac-b065-40b1-8be4-8fbb2aefddf8', '2639883f-3b16-4792-8125-d3d101fe85f0', 1, 2, '8d919e34-7d57-4eaa-a884-6decf19718f0') on conflict do nothing;


insert into face_twidget_tw001 (face_id, key) VALUES ('85279869-60a5-437f-bd46-15e4683899f3', 'image_gallery') on conflict do nothing ;
insert into face_twidget_tw002 (face_id, key, label_i18n_id, i18n_twin_class_field_id) VALUES ('85279869-60a5-437f-bd46-15e4683899f3', 'image_gallery', null, '8e7b9315-736c-4868-8d77-f78e4feef820') on conflict do nothing ;
insert into i18n values ('d591e2cb-34b6-4b1e-9bd2-a4f9033cf8d7', '', null, 'faceElement') on conflict do nothing ;
insert into i18n values ('35cce1af-8f41-4e1d-8200-60f0a9aa4cc3', '', null, 'faceElement') on conflict do nothing ;
insert into i18n_translation values ('d591e2cb-34b6-4b1e-9bd2-a4f9033cf8d7', 'en', 'English translaion') on conflict do nothing ;
insert into i18n_translation values ('35cce1af-8f41-4e1d-8200-60f0a9aa4cc3', 'en', 'Polish translation') on conflict do nothing ;

insert into face_twidget_tw002_accordion_item (id, face_id, locale, label_i18n_id) VALUES ('2c282928-e6e6-4dae-bf53-db1789cfea5d', '85279869-60a5-437f-bd46-15e4683899f3', 'en', 'd591e2cb-34b6-4b1e-9bd2-a4f9033cf8d7') on conflict do nothing ;
insert into face_twidget_tw002_accordion_item (id, face_id, locale, label_i18n_id) VALUES ('057ebf15-40d1-4fc9-9fea-dbd8a16a1fea', '85279869-60a5-437f-bd46-15e4683899f3', 'pl', '35cce1af-8f41-4e1d-8200-60f0a9aa4cc3') on conflict do nothing ;