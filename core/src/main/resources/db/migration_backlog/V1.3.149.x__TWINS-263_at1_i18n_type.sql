ALTER TABLE user_group
    DROP COLUMN IF EXISTS name,
    DROP COLUMN IF EXISTS description;

ALTER TABLE user_group
    ADD COLUMN IF NOT EXISTS name_i18n_id uuid,
    ADD COLUMN IF NOT EXISTS description_i18n_id uuid;

alter table user_group
    drop constraint if exists user_group_description_i18n_id_fk;

alter table public.user_group
    add constraint user_group_description_i18n_id_fk
        foreign key (description_i18n_id) references public.i18n;

alter table user_group
    drop constraint if exists user_group_name_i18n_id_fk;

alter table public.user_group
    add constraint user_group_name_i18n_id_fk
        foreign key (name_i18n_id) references public.i18n;

INSERT INTO public.i18n_type (id, name) VALUES ('userGroupName', 'User group name') on conflict on constraint i18n_type_pk do nothing ;
INSERT INTO public.i18n_type (id, name) VALUES ('userGroupDescription', 'User group description') on conflict on constraint i18n_type_pk do nothing ;
