INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (5015::integer, 50::integer, '', '', '', DEFAULT) on conflict do nothing;

insert into scheduler(id, domain_id, scheduler_featurer_id, scheduler_params, active, log_enabled, cron, fixed_rate, description, created_at, updated_at)
values
    ('00000000-0000-0000-0015-000000000015', null, 5015, null, true, true, '10 0 0 * * *', null, 'Scheduler to check user group map involves count is not less then 0', now(), now()) on conflict do nothing;
