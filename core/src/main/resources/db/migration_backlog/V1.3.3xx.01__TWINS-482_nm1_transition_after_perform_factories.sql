insert into twin_change_task_status values ('WAITING_FOR_DRAFT_COMMIT') on conflict do nothing;

insert into twin_factory_launcher values ('afterTransitionPerform') on conflict do nothing ;



