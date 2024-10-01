alter table twin_status
    rename column color to background_color;

alter table twin_status
    add if not exists font_color varchar;