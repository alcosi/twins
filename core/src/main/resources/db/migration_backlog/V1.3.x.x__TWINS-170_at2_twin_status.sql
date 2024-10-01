alter table twin_status
    rename column color to background_color;

alter table twin_status
    add font_color varchar;