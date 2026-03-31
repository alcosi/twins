INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (5013::integer, 50::integer, '', '', '', DEFAULT) on conflict do nothing;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (5014::integer, 50::integer, '', '', '', DEFAULT) on conflict do nothing;

insert into scheduler(id, domain_id, scheduler_featurer_id, scheduler_params, active, log_enabled, cron, fixed_rate, description, created_at, updated_at)
values
    ('00000000-0000-0000-0015-000000000014', null, 5013, null, true, true, '8 0 0 * * *', null, 'Scheduler for clearing external file storages after twin/attachment deletion', now(), now()),
    ('00000000-0000-0000-0015-000000000015', null, 5014, null, true, true, '9 0 0 * * *', null, 'Scheduler for clearing twin archive table', now(), now())
on conflict do nothing;
