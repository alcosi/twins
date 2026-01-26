create table if not exists twin_status_type
(
    id          varchar(40) not null
        primary key,
    description text
);

insert into twin_status_type (id, description)
values ('SKETCH', 'Sketch')
on conflict do nothing;
insert into twin_status_type (id, description)
values ('BASIC', 'Basic')
on conflict do nothing;

alter table twin_status
    add if not exists twin_status_type varchar(40) default 'BASIC' not null references twin_status_type on delete no action on update cascade;

update twin_status set twin_status_type = 'SKETCH' where id = '00000001-0000-0000-0000-000000000001';

alter table twinflow
    add if not exists initial_sketch_twin_status_id uuid default '00000001-0000-0000-0000-000000000001';


