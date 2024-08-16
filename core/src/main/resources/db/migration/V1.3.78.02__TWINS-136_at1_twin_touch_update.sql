-- drop constraint twin_touch_twin_id_fk
alter table twin_touch
    drop constraint if exists twin_touch_twin_id_fk;
-- add constraint with update and delete cascade
alter table twin_touch
    add constraint twin_touch_twin_id_fk
        foreign key (twin_id) references twin
            on delete cascade on update cascade ;

-- drop constraint twin_touch_touch_id_fk
alter table twin_touch
    drop constraint if exists twin_touch_touch_id_fk;
-- add constraint with update and delete cascade
alter table twin_touch
    add constraint twin_touch_touch_id_fk
        foreign key (touch_id) references touch
            on update cascade on delete cascade;
