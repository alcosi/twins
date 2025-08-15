alter table twin_change_task
    drop constraint if exists twin_change_task_twin_id_fk;

alter table twin_change_task
    add constraint twin_change_task_twin_id_fk
        foreign key (twin_id) references twin
            on update cascade on delete cascade;
