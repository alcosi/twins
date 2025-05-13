alter table public.data_list_option
    add if not exists background_color varchar(10);

alter table public.data_list_option
    add if not exists font_color varchar(10);
