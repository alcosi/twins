alter table permission
    rename column name to name_i18n_id;
alter table permission
    alter column name_i18n_id type uuid using name_i18n_id::uuid;

alter table permission
    add constraint permission_name_i18n_id_i18n_id_fk
        foreign key (name_i18n_id) references i18n
            on update cascade on delete cascade;

alter table permission
    rename column description to description_i18n_id;
alter table permission
    alter column description_i18n_id type uuid using description_i18n_id::uuid;

alter table permission
    add constraint permission_description_i18n_id_i18n_id_fk
        foreign key (description_i18n_id) references i18n
            on update cascade on delete cascade;


INSERT INTO public.i18n_type (id, name) VALUES ('permissionName', 'Permission name') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO public.i18n_type (id, name) VALUES ('permissionDescription', 'Permission description') on conflict on constraint i18n_type_pk do nothing ;
