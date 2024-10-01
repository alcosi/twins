alter table if exists twin_status
    rename column color to background_color;

alter table if exists twin_status
    add font_color varchar;