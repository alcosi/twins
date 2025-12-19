alter table data_list_option
    add if not exists description_i18n_id uuid
        constraint data_list_option_i18n_id_fk_2
            references i18n
            on update cascade on delete restrict;

INSERT INTO i18n_type (id, name)
VALUES ('dataListOptionDescription', 'dataList option description')
on conflict (id) do nothing;