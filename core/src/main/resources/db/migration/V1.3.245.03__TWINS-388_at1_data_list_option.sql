alter table data_list_option
    add if not exists background_color varchar(10) default null;

alter table data_list_option
    add if not exists font_color varchar(10) default null;
