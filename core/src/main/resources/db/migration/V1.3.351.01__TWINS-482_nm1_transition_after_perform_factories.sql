alter table twin_change_task_status
    alter column id type varchar(40) using id::varchar(40);

alter table twin_factory_launcher
    alter column id type varchar(40) using id::varchar(40);

insert into twin_change_task_status values ('WAITING_FOR_DRAFT_COMMIT') on conflict do nothing;

insert into twin_factory_launcher values ('afterTransitionPerform') on conflict do nothing ;
