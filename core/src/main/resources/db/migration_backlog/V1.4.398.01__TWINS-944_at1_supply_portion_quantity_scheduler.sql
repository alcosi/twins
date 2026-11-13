-- Consistency-check scheduler for SUPPLY_PORTION quantities (ticket TWINS-XXX placeholder).
-- Validates the conservation invariant issued = used + defective + remaining per portion twin
-- (Task flavor uses issuedQuantity / remainingQuantityTask, Project/Warehouse flavors use
-- issuedQuantityProjectAndWarehouse / remainingQuantityProjectAndWarehouse) and detects
-- negative quantity values in twin_field_decimal.
-- Implementation: featurer 5017 SchedulerConsistencyCheckSupplyPortionQuantity ( TwinFieldDecimalRepository
-- counts violations by twin_class.key = 'SUPPLY_PORTION' and field keys, so it does not depend on
-- environment-specific design-time UUIDs).
-- The featurer row is pre-seeded with empty class (same pattern as featurers 4901-4912):
-- FeaturerService.syncFeaturers runs at @PostConstruct, i.e. AFTER flyway, and fills class/name/description.
-- Scheduler rows are only picked up on startup (SchedulerService.startAll), so this scheduler starts
-- after the first restart carrying featurer 5017.
insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (5017::integer, 50::integer, '', '', '', DEFAULT) on conflict do nothing;

insert into scheduler (id, domain_id, scheduler_featurer_id, scheduler_params, active, log_enabled, cron, fixed_rate, description, created_at, updated_at)
values ('00000000-0000-0000-0015-000000000018'::uuid, null, 5017, null, true, false, '11 0 0 * * *', null, 'Scheduler to check supply portion quantity conservation (issued = used + defective + remaining) and absence of negative quantity values', now(), now())
on conflict (id) do update set
    scheduler_featurer_id = excluded.scheduler_featurer_id,
    active = excluded.active,
    log_enabled = excluded.log_enabled,
    cron = excluded.cron,
    description = excluded.description,
    updated_at = now();
-- Daily at 00:00:11 (second-offset pattern of the other daily checks, e.g. 5015 '10 0 0 * * *').
-- Reports ONLY through the AlertLogger (twins-alert.log); scheduler_log is disabled (log_enabled=false)
-- to keep the log table quiet. Set log_enabled=true to also persist per-run results in scheduler_log.
