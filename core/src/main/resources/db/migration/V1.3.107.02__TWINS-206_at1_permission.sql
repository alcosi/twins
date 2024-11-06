-- rename column from [name] to [name_i18n_id]
DO $$
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public'
                     AND table_name = 'permission'
                     AND column_name = 'name') THEN
            ALTER TABLE public.permission RENAME COLUMN name TO name_i18n_id;
        END IF;
    END $$;

alter table permission
    alter column name_i18n_id type uuid using name_i18n_id::uuid;

alter table permission
    drop constraint if exists permission_name_i18n_id_i18n_id_fk;

alter table permission
    add constraint permission_name_i18n_id_i18n_id_fk
        foreign key (name_i18n_id) references i18n;


-- rename column from [description] to [description_i18n_id]
DO $$
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public'
                     AND table_name = 'permission'
                     AND column_name = 'description') THEN
            ALTER TABLE public.permission RENAME COLUMN description TO description_i18n_id;
        END IF;
    END $$;

alter table permission
    alter column description_i18n_id type uuid using description_i18n_id::uuid;

alter table permission
    drop constraint if exists permission_description_i18n_id_i18n_id_fk;

alter table permission
    add constraint permission_description_i18n_id_i18n_id_fk
        foreign key (description_i18n_id) references i18n;


INSERT INTO public.i18n_type (id, name) VALUES ('permissionName', 'Permission name') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO public.i18n_type (id, name) VALUES ('permissionDescription', 'Permission description') on conflict on constraint i18n_type_pk do nothing ;


INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('7a375905-e71a-4469-9dec-2bdf333828dc', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('bcf7fe43-cecf-47e9-9b33-b3baada19c79', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('0a475787-042b-4bc0-bdf0-8c10c2f96186', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('dd4bce28-2533-463f-8dcb-5084f02d78bb', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('64e487ff-5ad1-4b7f-b93c-d527e187e58d', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('772ca2fc-3eb1-400e-a5a3-8c211cfd2559', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('8d6bc69a-c92c-4adf-b8a2-6e2a4ef9da5e', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('aebf144b-d1c2-4f03-9e49-2d895f5f7275', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('66ebfbe4-3779-4a92-a764-bbba0f7ad418', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('0b218bd0-9002-4c6a-97a9-ab1c6e803391', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('aa3c0eae-c236-4408-9bd7-329a0e75efc1', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('aa88eae9-8243-48ac-9ea0-26893bcf5642', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('5d7854bc-1557-4aca-8134-759a7551f59e', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('3a96e29f-f974-4cfb-8f7f-69ee0db0af6c', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('7e80b599-f7cc-46d7-a8ee-42afb9a97878', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('da790ac3-0809-4831-b371-3025af5c3460', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('5a726a07-a84b-4c87-8ffb-b750d01b4d8a', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('5850c1ed-b7ab-4c06-88a4-0969fe47ffd2', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('39e4b061-b17a-450c-ad5d-84b18b7f165b', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('cb051be7-fd28-4265-83c6-283b1fb17a13', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('5a6938ce-cdae-4329-9491-8b117fa24fda', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('ae454c40-7514-497a-b98c-b5af37023cd7', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('87f875aa-2d1b-47c0-96ec-2967e8dd45e1', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('d74af524-8276-4f99-b04a-5e4e54886694', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('040f2868-b8a0-4767-8e18-5194bcf83b9a', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('8fd378b3-b005-4aec-9aad-000e947eb7c5', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('ad0a27b3-4744-41a6-84a5-01454872721a', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('b35d8e5b-6873-4c9f-9017-1bde0c8162e8', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('5e750ade-cd88-4dd9-8f80-d78b9ff12a4c', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('13951d61-c579-4347-b019-3ec71a6565ca', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('c25f1295-c526-4d9b-892e-5b39c3a81d93', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('3e7f78c6-72a2-4cb6-b8fa-225f8913ce13', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('24166dfc-5cf4-4c82-a06d-4d2f485c0ddb', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('4c82094f-ebf8-44a2-b56e-0116d44fd445', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('7e9d1b19-e71e-4691-bec9-46c2de611de5', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('95c9ae68-5af4-4924-ab6f-359a271a655e', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('f277a510-d390-4482-8145-6f2af8ed1204', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('4270834c-a4be-4761-896f-7f09bad38e08', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('bae554df-b0d2-4094-8b95-c32b9d68490c', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('e025b2e1-0969-4a93-830c-b6eb4b107b89', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('68929267-7f32-416a-93e9-a897f48e9fc2', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('f5d476ad-0529-4297-b6bd-2bf409cd1558', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('3ac92f3f-28cd-43ba-b172-e2be35318d0a', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('6111200a-4ac3-4e15-b529-d3de60735bd3', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('22db2a31-8488-4017-9979-02087720ab82', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('fc54c03e-bc43-46a0-b096-d81c91b66233', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('e1416fb7-85fc-42ef-8d06-b884460f3d8f', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('b8f6a1eb-2611-4f4f-a45b-7ba7a71cf894', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('284ef8a0-6e1b-4bd4-a9c9-5cef4cfe0ab4', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('a2dac067-d74c-474d-85af-4caef13a92bd', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('144ed54c-aeb9-49d2-a45e-039e4ecef22a', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('8b44dcf5-e345-4f66-8141-97b017bde0e0', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('0fce4ceb-0810-40f3-8245-925858631684', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('6e09b9dc-4d6d-403b-9b77-194e745df511', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('baf5ef49-405a-444b-a549-026046047d84', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('2f0499a9-88fa-4bf4-afd1-60ef3f8ed1ee', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('bc1bdd42-bb14-403c-a080-8f908cbdb674', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('cc3271bc-428d-4a57-a81c-016f8795cf63', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('1a72a9e3-0a91-4e3a-897f-d515d531ed80', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('51c37870-f9d4-4f40-a1e8-88762c95ddb4', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('76e07c04-72c5-41b0-84a0-37fa14f0dd07', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('fced4940-ac13-4a22-ab52-371be45fba50', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('dd545c28-10d9-4e20-b9bd-ee8a60c5e1ab', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('bafa22dc-3f1e-4c8d-89e9-e0d9d23b8cf2', null, null, 'permissionName') on conflict on constraint i18n_pkey do nothing ;


INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('4ad08765-4460-466c-890a-4aea9902bbbb', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('846ee3fb-f849-4f48-b14c-97d5bde83696', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('261b06f0-14c9-45f4-882e-5adba439857e', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('0177f6cf-73fc-4eed-8e1b-5af6f1d75a19', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('aba03cb4-f45c-429c-8cec-b547a4d24228', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('f074a4af-14f4-45cc-9486-00fe9279d678', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('3fd67669-c06d-4411-888b-e5c71d1a0d80', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('251c803e-085a-441e-bd25-3c0f525462a9', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('75e1121b-ab93-47a0-9d87-fdc9031c4e7d', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('85dd7a0b-27de-43ae-86aa-ee29a27e8d6b', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('97da4df4-3298-4cd3-9797-83cefc76f49e', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('94ae7b2c-3ef6-4398-aa26-e3b462157234', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('7b034f7c-45cb-47b5-a70c-4d73f64535df', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('9ac3101b-9a06-4c1c-a615-5a2de265587f', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('55021622-8f1e-4e77-9943-b20ac5566293', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('9a22ce82-5c7e-4111-9dbf-6eef14e276fc', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('4f4a332b-81cd-4d29-b9cd-141c9ae5426b', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('ad0234c4-79bc-4e01-841c-19197884f6bf', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('d244a89d-d6b3-4154-be65-085b97fd31aa', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('9c58d789-2de6-43f4-8f42-028c5101393a', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('34c65016-e33f-40bb-becf-f1aa569df276', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('b91235e4-6489-4f78-bbce-d5f5262956e3', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('e8f12aaf-a3f5-4386-8fe4-3c2cd24f9f1b', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('b2d5cd70-4997-40f2-9d2b-2edf9f44b110', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('9ec1e490-a96c-4225-965a-ebae7473d264', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('530b2ce4-acd1-4a7c-9fde-811bc4db56fc', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('cde3e314-07b5-4575-963f-ca57a742119e', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('f2c2f356-5d2e-4aa3-a7db-160079934857', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('7a0457ff-25e1-4cca-9288-122c228a4c51', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('b4be8ab8-4c83-42a0-9314-9433b005060c', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('8bfa97fe-56ed-42dc-8308-565787c93553', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('4876a281-3fd5-4ed0-8574-81dcf38cc3ce', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('4bae8a6a-934e-4dde-b810-5a7256f6aa53', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('a4aa42aa-b8a1-416a-8377-7a73ad948cd5', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('5d4b30f0-f813-4615-b7e3-fbf93139bf7d', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('16644c82-7c17-4d6f-84d7-fabe2c20185a', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('a07cd37c-d922-4aae-986f-cce650035c2b', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('221cb3dc-ce31-42c2-8dff-51f8571c5188', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('5fec7de7-f191-49a1-a61e-6790f31361aa', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('9bcbc346-f55c-4236-90cb-4841f9ae7d3d', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('73286ed1-b678-4727-8224-4284f6e3907c', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('dfbe3a6c-59b3-4e33-851c-53391ecddac4', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('0183ef41-08a3-43fc-ad9b-1a29f60df232', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('0fda05b8-bd18-49cf-8ac1-c1c4acc14e59', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('5bf1de37-a4ce-4cee-a881-9c3bff3c3ac8', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('c6f5f686-062d-4189-9797-58327d4ee19d', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('2b9373cc-18ed-490a-b32a-406541ce955a', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('1f102fe4-a071-4d1b-ad6a-73d4a46278ae', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('3b39114a-18e7-4321-8825-3b5400fb4501', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('d5628114-c9ae-4445-8a28-e89f44ac6790', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('2bef2273-d7df-471d-8406-c1058bded677', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('2b0f40ab-dad9-41f2-9b55-2e5252d6d343', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('36a6fe61-9948-43b4-9da3-ebc46296438f', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('c2bcb883-cef3-4fda-aa72-c48d89956f4d', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('6fc9984b-a62d-4cf1-87a8-371116f28990', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('0b9c3453-eccc-4a77-8032-49c3069a6339', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('082cda93-bf6f-4d35-8ae1-e394cc4bd617', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('74005ef2-f0ce-4f01-a972-33fb2e428a9d', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('4258dd6a-0372-4571-8541-4e310a869060', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('a91ebc80-7768-4690-b6a2-a8f1865ff8b8', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('c6b2e1a2-464d-4744-8e57-d951a22ca1ce', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('f8925ce6-93b2-460b-b14f-c7bcff0d0ad6', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('78f54c58-d1a5-49b4-b1d0-e5b34dd4912d', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;
INSERT INTO public.i18n (id, name, key, i18n_type_id) VALUES ('8bebc90f-9a2a-4f07-b81d-09ac1a586f17', null, null, 'permissionDescription') on conflict on constraint i18n_pkey do nothing ;


UPDATE public.permission SET name_i18n_id = 'aa88eae9-8243-48ac-9ea0-26893bcf5642', description_i18n_id = '94ae7b2c-3ef6-4398-aa26-e3b462157234' WHERE id = '41a285b3-3c6f-440e-b1f5-7ff2b7d24f57';
UPDATE public.permission SET name_i18n_id = 'aa3c0eae-c236-4408-9bd7-329a0e75efc1', description_i18n_id = '97da4df4-3298-4cd3-9797-83cefc76f49e' WHERE id = '22fbbdf5-b22d-4e5d-a780-8096b00509b3';
UPDATE public.permission SET name_i18n_id = '284ef8a0-6e1b-4bd4-a9c9-5cef4cfe0ab4', description_i18n_id = '3b39114a-18e7-4321-8825-3b5400fb4501' WHERE id = 'ecad126a-61a1-495b-88d9-692356dcf973';
UPDATE public.permission SET name_i18n_id = '68929267-7f32-416a-93e9-a897f48e9fc2', description_i18n_id = '73286ed1-b678-4727-8224-4284f6e3907c' WHERE id = '00000000-0000-0000-0004-000000000002';
UPDATE public.permission SET name_i18n_id = 'bc1bdd42-bb14-403c-a080-8f908cbdb674', description_i18n_id = '082cda93-bf6f-4d35-8ae1-e394cc4bd617' WHERE id = 'b16f1f1d-0fff-42fb-bc59-35e0762bc473';
UPDATE public.permission SET name_i18n_id = '5e750ade-cd88-4dd9-8f80-d78b9ff12a4c', description_i18n_id = '7a0457ff-25e1-4cca-9288-122c228a4c51' WHERE id = '975fb4f2-5c2b-46bd-a9ad-b1f58646ffd7';
UPDATE public.permission SET name_i18n_id = 'baf5ef49-405a-444b-a549-026046047d84', description_i18n_id = '6fc9984b-a62d-4cf1-87a8-371116f28990' WHERE id = 'c1491beb-28a9-48c6-9cc5-16ef0016fb03';
UPDATE public.permission SET name_i18n_id = 'ad0a27b3-4744-41a6-84a5-01454872721a', description_i18n_id = 'cde3e314-07b5-4575-963f-ca57a742119e' WHERE id = '0860d6a8-689f-47c7-8ede-8792f29a9d13';
UPDATE public.permission SET name_i18n_id = 'fced4940-ac13-4a22-ab52-371be45fba50', description_i18n_id = 'f8925ce6-93b2-460b-b14f-c7bcff0d0ad6' WHERE id = 'd7f148cf-ee81-4a6f-91ee-aa9f5feed10c';
UPDATE public.permission SET name_i18n_id = '13951d61-c579-4347-b019-3ec71a6565ca', description_i18n_id = 'b4be8ab8-4c83-42a0-9314-9433b005060c' WHERE id = 'f85b380b-9e20-416a-b3c4-7f2074fbe5e7';
UPDATE public.permission SET name_i18n_id = '5a6938ce-cdae-4329-9491-8b117fa24fda', description_i18n_id = '34c65016-e33f-40bb-becf-f1aa569df276' WHERE id = '273f387a-0aa7-4fcd-a696-690df77ea607';
UPDATE public.permission SET name_i18n_id = 'f5d476ad-0529-4297-b6bd-2bf409cd1558', description_i18n_id = 'dfbe3a6c-59b3-4e33-851c-53391ecddac4' WHERE id = '7c404441-92b9-467b-9eb5-919194335380';
UPDATE public.permission SET name_i18n_id = 'f277a510-d390-4482-8145-6f2af8ed1204', description_i18n_id = 'a07cd37c-d922-4aae-986f-cce650035c2b' WHERE id = '00000000-0000-0000-0004-000000000001';
UPDATE public.permission SET name_i18n_id = '4270834c-a4be-4761-896f-7f09bad38e08', description_i18n_id = '221cb3dc-ce31-42c2-8dff-51f8571c5188' WHERE id = '0480b598-d765-4c41-96af-4ebef96134dd';
UPDATE public.permission SET name_i18n_id = 'b8f6a1eb-2611-4f4f-a45b-7ba7a71cf894', description_i18n_id = '1f102fe4-a071-4d1b-ad6a-73d4a46278ae' WHERE id = '51cd3a18-3606-4ca5-b4a5-a97fbce594f3';
UPDATE public.permission SET name_i18n_id = '39e4b061-b17a-450c-ad5d-84b18b7f165b', description_i18n_id = 'd244a89d-d6b3-4154-be65-085b97fd31aa' WHERE id = 'fb4003c9-ad7b-4578-8755-8dec210beebd';
UPDATE public.permission SET name_i18n_id = '772ca2fc-3eb1-400e-a5a3-8c211cfd2559', description_i18n_id = 'f074a4af-14f4-45cc-9486-00fe9279d678' WHERE id = '64630081-d0ae-472c-ad2b-f2a48c1a021a';
UPDATE public.permission SET name_i18n_id = '6111200a-4ac3-4e15-b529-d3de60735bd3', description_i18n_id = '0fda05b8-bd18-49cf-8ac1-c1c4acc14e59' WHERE id = '0f9e7bd9-4abb-4c2f-b0ff-cccde4e156a9';
UPDATE public.permission SET name_i18n_id = 'bafa22dc-3f1e-4c8d-89e9-e0d9d23b8cf2', description_i18n_id = '8bebc90f-9a2a-4f07-b81d-09ac1a586f17' WHERE id = '00000000-0000-0000-0004-000000000005';
UPDATE public.permission SET name_i18n_id = 'cc3271bc-428d-4a57-a81c-016f8795cf63', description_i18n_id = '74005ef2-f0ce-4f01-a972-33fb2e428a9d' WHERE id = '0e6254ea-4885-45b6-81f7-d5a4ff17e089';
UPDATE public.permission SET name_i18n_id = 'b35d8e5b-6873-4c9f-9017-1bde0c8162e8', description_i18n_id = 'f2c2f356-5d2e-4aa3-a7db-160079934857' WHERE id = 'eca397c9-193a-44be-adcc-b65f606a08c1';
UPDATE public.permission SET name_i18n_id = '87f875aa-2d1b-47c0-96ec-2967e8dd45e1', description_i18n_id = 'e8f12aaf-a3f5-4386-8fe4-3c2cd24f9f1b' WHERE id = '97125b10-7368-4f30-bf9e-b3f5943939fb';
UPDATE public.permission SET name_i18n_id = 'bae554df-b0d2-4094-8b95-c32b9d68490c', description_i18n_id = '5fec7de7-f191-49a1-a61e-6790f31361aa' WHERE id = 'be9f945a-7e83-4f79-837c-f811a776291a';
UPDATE public.permission SET name_i18n_id = '5a726a07-a84b-4c87-8ffb-b750d01b4d8a', description_i18n_id = '4f4a332b-81cd-4d29-b9cd-141c9ae5426b' WHERE id = '19b479d0-5a8f-443d-ac2b-ae85fe3d2339';
UPDATE public.permission SET name_i18n_id = 'aebf144b-d1c2-4f03-9e49-2d895f5f7275', description_i18n_id = '251c803e-085a-441e-bd25-3c0f525462a9' WHERE id = '2e5a6bbc-deab-468c-8ef3-c3a05d79a693';
UPDATE public.permission SET name_i18n_id = '95c9ae68-5af4-4924-ab6f-359a271a655e', description_i18n_id = '16644c82-7c17-4d6f-84d7-fabe2c20185a' WHERE id = '12a0ccee-d91e-49e6-a71b-e2652f03be3c';
UPDATE public.permission SET name_i18n_id = 'e025b2e1-0969-4a93-830c-b6eb4b107b89', description_i18n_id = '9bcbc346-f55c-4236-90cb-4841f9ae7d3d' WHERE id = 'c4fb8111-bb82-43bb-81ce-20a5d0baae19';
UPDATE public.permission SET name_i18n_id = 'dd4bce28-2533-463f-8dcb-5084f02d78bb', description_i18n_id = '0177f6cf-73fc-4eed-8e1b-5af6f1d75a19' WHERE id = 'b14a2de9-a8e0-45de-9795-741ba9e11f00';
UPDATE public.permission SET name_i18n_id = '0b218bd0-9002-4c6a-97a9-ab1c6e803391', description_i18n_id = '85dd7a0b-27de-43ae-86aa-ee29a27e8d6b' WHERE id = 'e00d09fc-1828-4d25-90f9-4d2461160ec7';
UPDATE public.permission SET name_i18n_id = '64e487ff-5ad1-4b7f-b93c-d527e187e58d', description_i18n_id = 'aba03cb4-f45c-429c-8cec-b547a4d24228' WHERE id = '8f551adc-d3bd-47c2-b424-fbf6da0650f1';
UPDATE public.permission SET name_i18n_id = '3ac92f3f-28cd-43ba-b172-e2be35318d0a', description_i18n_id = '0183ef41-08a3-43fc-ad9b-1a29f60df232' WHERE id = 'b4487706-aff6-441c-a99a-ded2fe006969';
UPDATE public.permission SET name_i18n_id = '8d6bc69a-c92c-4adf-b8a2-6e2a4ef9da5e', description_i18n_id = '3fd67669-c06d-4411-888b-e5c71d1a0d80' WHERE id = '02509bde-74c3-4547-a7be-1e6647e319d7';
UPDATE public.permission SET name_i18n_id = '51c37870-f9d4-4f40-a1e8-88762c95ddb4', description_i18n_id = 'a91ebc80-7768-4690-b6a2-a8f1865ff8b8' WHERE id = 'aa9a5078-8488-43c7-b54a-6abc1d5ef6b0';
UPDATE public.permission SET name_i18n_id = '8b44dcf5-e345-4f66-8141-97b017bde0e0', description_i18n_id = '2b0f40ab-dad9-41f2-9b55-2e5252d6d343' WHERE id = '18ed3be3-066f-4eb0-b6a4-84a89d8a7de3';
UPDATE public.permission SET name_i18n_id = '3a96e29f-f974-4cfb-8f7f-69ee0db0af6c', description_i18n_id = '9ac3101b-9a06-4c1c-a615-5a2de265587f' WHERE id = 'a62c04f4-6f5a-497c-aa71-3065e3529d29';
UPDATE public.permission SET name_i18n_id = '24166dfc-5cf4-4c82-a06d-4d2f485c0ddb', description_i18n_id = '4bae8a6a-934e-4dde-b810-5a7256f6aa53' WHERE id = 'a182e82b-34a4-4b76-8048-4008ba723afe';
UPDATE public.permission SET name_i18n_id = 'ae454c40-7514-497a-b98c-b5af37023cd7', description_i18n_id = 'b91235e4-6489-4f78-bbce-d5f5262956e3' WHERE id = '9e252069-75d9-421d-99a7-65b20578b33a';
UPDATE public.permission SET name_i18n_id = 'e1416fb7-85fc-42ef-8d06-b884460f3d8f', description_i18n_id = '2b9373cc-18ed-490a-b32a-406541ce955a' WHERE id = 'aab2b662-3c42-4249-8126-81f1e2a14cea';
UPDATE public.permission SET name_i18n_id = '0a475787-042b-4bc0-bdf0-8c10c2f96186', description_i18n_id = '261b06f0-14c9-45f4-882e-5adba439857e' WHERE id = 'fa82c9d5-b247-4481-be9d-16124981390e';
UPDATE public.permission SET name_i18n_id = 'c25f1295-c526-4d9b-892e-5b39c3a81d93', description_i18n_id = '8bfa97fe-56ed-42dc-8308-565787c93553' WHERE id = '4954f971-9e01-4424-9fd1-818263b827e9';
UPDATE public.permission SET name_i18n_id = '22db2a31-8488-4017-9979-02087720ab82', description_i18n_id = '5bf1de37-a4ce-4cee-a881-9c3bff3c3ac8' WHERE id = '7211a51f-f1cc-4481-b32c-4e340635b84e';
UPDATE public.permission SET name_i18n_id = 'bcf7fe43-cecf-47e9-9b33-b3baada19c79', description_i18n_id = '846ee3fb-f849-4f48-b14c-97d5bde83696' WHERE id = 'ca9bc15b-0f60-4edc-af5a-a0e9c5a3bb94';
UPDATE public.permission SET name_i18n_id = '66ebfbe4-3779-4a92-a764-bbba0f7ad418', description_i18n_id = '75e1121b-ab93-47a0-9d87-fdc9031c4e7d' WHERE id = '64825c96-9bc2-4efd-888b-1db1a38a0016';
UPDATE public.permission SET name_i18n_id = '5850c1ed-b7ab-4c06-88a4-0969fe47ffd2', description_i18n_id = 'ad0234c4-79bc-4e01-841c-19197884f6bf' WHERE id = '46107bd9-39e2-47c1-9da3-77351c544f50';
UPDATE public.permission SET name_i18n_id = '2f0499a9-88fa-4bf4-afd1-60ef3f8ed1ee', description_i18n_id = '0b9c3453-eccc-4a77-8032-49c3069a6339' WHERE id = 'd33c2bde-947a-4ca4-9522-70ccac91bac9';
UPDATE public.permission SET name_i18n_id = '0fce4ceb-0810-40f3-8245-925858631684', description_i18n_id = '36a6fe61-9948-43b4-9da3-ebc46296438f' WHERE id = '097a8885-a387-446c-995a-429bda632c4b';
UPDATE public.permission SET name_i18n_id = '7e80b599-f7cc-46d7-a8ee-42afb9a97878', description_i18n_id = '55021622-8f1e-4e77-9943-b20ac5566293' WHERE id = '36b59d9f-d6ad-4103-b929-2cf75a218bc3';
UPDATE public.permission SET name_i18n_id = '6e09b9dc-4d6d-403b-9b77-194e745df511', description_i18n_id = 'c2bcb883-cef3-4fda-aa72-c48d89956f4d' WHERE id = '6ae3e8c5-eac1-420b-9076-3552e93e67f0';
UPDATE public.permission SET name_i18n_id = '040f2868-b8a0-4767-8e18-5194bcf83b9a', description_i18n_id = '9ec1e490-a96c-4225-965a-ebae7473d264' WHERE id = 'dd869856-676a-422d-8fb2-4fc3f826ac2c';
UPDATE public.permission SET name_i18n_id = 'a2dac067-d74c-474d-85af-4caef13a92bd', description_i18n_id = 'd5628114-c9ae-4445-8a28-e89f44ac6790' WHERE id = 'b3048801-1d05-4c98-b1a0-29ed47278442';
UPDATE public.permission SET name_i18n_id = '3e7f78c6-72a2-4cb6-b8fa-225f8913ce13', description_i18n_id = '4876a281-3fd5-4ed0-8574-81dcf38cc3ce' WHERE id = 'c58eeea0-be59-426e-885d-eb02f113d921';
UPDATE public.permission SET name_i18n_id = '1a72a9e3-0a91-4e3a-897f-d515d531ed80', description_i18n_id = '4258dd6a-0372-4571-8541-4e310a869060' WHERE id = '59daa8e0-60ab-43d3-92b1-5ff9d8c575a4';
UPDATE public.permission SET name_i18n_id = '76e07c04-72c5-41b0-84a0-37fa14f0dd07', description_i18n_id = 'c6b2e1a2-464d-4744-8e57-d951a22ca1ce' WHERE id = 'dfb69606-963b-4649-a0b7-98e294ba5d1d';
UPDATE public.permission SET name_i18n_id = 'd74af524-8276-4f99-b04a-5e4e54886694', description_i18n_id = 'b2d5cd70-4997-40f2-9d2b-2edf9f44b110' WHERE id = '1bc60bd6-3d2a-40f5-aaf9-66d8cfe2b20c';
UPDATE public.permission SET name_i18n_id = '4c82094f-ebf8-44a2-b56e-0116d44fd445', description_i18n_id = 'a4aa42aa-b8a1-416a-8377-7a73ad948cd5' WHERE id = '5a7cd5f9-326b-4553-90c2-50ddd48dc3b1';
UPDATE public.permission SET name_i18n_id = 'dd545c28-10d9-4e20-b9bd-ee8a60c5e1ab', description_i18n_id = '78f54c58-d1a5-49b4-b1d0-e5b34dd4912d' WHERE id = '00000000-0000-0000-0004-000000000004';
UPDATE public.permission SET name_i18n_id = '144ed54c-aeb9-49d2-a45e-039e4ecef22a', description_i18n_id = '2bef2273-d7df-471d-8406-c1058bded677' WHERE id = '00000000-0000-0000-0004-000000000003';
UPDATE public.permission SET name_i18n_id = 'da790ac3-0809-4831-b371-3025af5c3460', description_i18n_id = '9a22ce82-5c7e-4111-9dbf-6eef14e276fc' WHERE id = 'bcb6d059-e38f-4bca-9b36-c90e4c88259e';
UPDATE public.permission SET name_i18n_id = '5d7854bc-1557-4aca-8134-759a7551f59e', description_i18n_id = '7b034f7c-45cb-47b5-a70c-4d73f64535df' WHERE id = '6191233a-4104-4e22-977d-fb79ea494dca';
UPDATE public.permission SET name_i18n_id = 'fc54c03e-bc43-46a0-b096-d81c91b66233', description_i18n_id = 'c6f5f686-062d-4189-9797-58327d4ee19d' WHERE id = '55d8b612-7797-4c6c-9db0-e12d0ed5c0d6';
UPDATE public.permission SET name_i18n_id = '7e9d1b19-e71e-4691-bec9-46c2de611de5', description_i18n_id = '5d4b30f0-f813-4615-b7e3-fbf93139bf7d' WHERE id = '0970b456-9a71-40b3-b884-8b8e160fa424';
UPDATE public.permission SET name_i18n_id = '7a375905-e71a-4469-9dec-2bdf333828dc', description_i18n_id = '4ad08765-4460-466c-890a-4aea9902bbbb' WHERE id = 'abdeef68-7d6d-4385-9906-e3b701d2c503';
UPDATE public.permission SET name_i18n_id = '8fd378b3-b005-4aec-9aad-000e947eb7c5', description_i18n_id = '530b2ce4-acd1-4a7c-9fde-811bc4db56fc' WHERE id = 'b404380b-a422-4b8a-86db-5691a8a9d54d';
UPDATE public.permission SET name_i18n_id = 'cb051be7-fd28-4265-83c6-283b1fb17a13', description_i18n_id = '9c58d789-2de6-43f4-8f42-028c5101393a' WHERE id = '7e0c2f29-6f9b-4965-8f68-890f022920a1';
