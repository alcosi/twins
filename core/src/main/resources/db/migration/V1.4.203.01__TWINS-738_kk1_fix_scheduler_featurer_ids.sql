-- Fix scheduler featurer IDs - split 5010 into two separate schedulers

-- Update existing scheduler 5010 to be for TwinTriggerTaskRunner
UPDATE scheduler
SET
    cron = NULL,
    fixed_rate = 2000,
    description = 'Scheduler for executing twin triggers',
    updated_at = CURRENT_TIMESTAMP
WHERE scheduler_featurer_id = 5010;

-- Create new scheduler 5016 for SchedulerConsistencyCheckPermissionMaterGlobal
INSERT INTO scheduler (id, domain_id, scheduler_featurer_id, cron, active, log_enabled, scheduler_params, fixed_rate, description, created_at, updated_at)
VALUES ('00000000-0000-0000-0015-000000000011'::uuid, NULL, 5016, '5 0 * * * *', true, true, NULL, NULL, 'Scheduler to check permission materialization global grants count is not less then 0', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    scheduler_featurer_id = 5016,
    cron = EXCLUDED.cron,
    fixed_rate = EXCLUDED.fixed_rate,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;
