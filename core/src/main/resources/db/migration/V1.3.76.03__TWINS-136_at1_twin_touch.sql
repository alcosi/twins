-- CREATE TOUCH TABLE
create table if not exists touch(
    id varchar(50)
        constraint touch_pk
            primary key
);
-- INSERT VALUES FOR TOUCH
INSERT INTO touch (id) VALUES ('WATCHED') on conflict do nothing;
INSERT INTO touch (id) VALUES ('STARRED') on conflict do nothing;
INSERT INTO touch (id) VALUES ('REVIEWED') on conflict do nothing;
-- CREATE TWIN TOUCH
create table if not exists twin_touch(
    id        uuid      not null,
    twin_id   uuid      not null
        constraint twin_touch_twin_id_fk
            references twin,
    touch_id  varchar   not null
        constraint twin_touch_touch_id_fk
            references touch,
    user_id   uuid      not null
        constraint twin_touch_user_id_fk
            references "user",
    created_at TIMESTAMP not null,
    constraint twin_touch_uq
        unique (twin_id, touch_id, user_id)
);
-- DROP twin_starred AND twin_watcher
drop table if exists twin_starred;
drop table if exists twin_watcher;