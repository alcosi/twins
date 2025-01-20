-- updating fk cascade delete
alter table public.data_list_option
drop constraint data_list_option_data_list_id_fk;

alter table public.data_list_option
    add constraint data_list_option_data_list_id_fk
        foreign key (data_list_id) references public.data_list
            on update cascade on delete cascade;

-- rename name into name_i18n_id
DO $$
BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'data_list' AND column_name = 'name_i18n_id'
        ) THEN
ALTER TABLE data_list
    RENAME COLUMN name TO name_i18n_id;
END IF;
END $$;

-- rename description into description_i18n_id
DO $$
BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'data_list' AND column_name = 'description_i18n_id'
        ) THEN
ALTER TABLE data_list
    RENAME COLUMN description TO description_i18n_id;
END IF;
END $$;

alter table public.data_list
    alter column name_i18n_id drop not null;

UPDATE public.data_list
SET name_i18n_id = NULL
WHERE name_i18n_id is not null ;

alter table public.data_list
alter column name_i18n_id type uuid using name_i18n_id::uuid;

alter table public.data_list
alter column description_i18n_id type uuid using description_i18n_id::uuid;

alter table public.data_list
    add if not exists attribute_1_name_i18n_id uuid;

alter table public.data_list
    add if not exists attribute_2_name_i18n_id uuid;

alter table public.data_list
    add if not exists attribute_3_name_i18n_id uuid;

alter table public.data_list
    add if not exists attribute_4_name_i18n_id uuid;

alter table public.data_list
    add if not exists created_at timestamp;