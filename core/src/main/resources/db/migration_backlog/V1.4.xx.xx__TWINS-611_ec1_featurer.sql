insert into featurer_type(id, name, description)
values (50, 'Scheduler', 'Services for scheduling tasks')
on conflict do nothing;

insert into featurer(id, featurer_type_id, class, name, description, deprecated)
values (5001, 50, 'org.twins.core.featurer.scheduler.SchedulerAttachmentDeleteTaskRunner', 'SchedulerAttachmentDeleteTaskRunner', 'Scheduler for clearing external file storages after twin/attachment deletion', false),
       (5002, 50, 'org.twins.core.featurer.scheduler.SchedulerTwinArchiveCleaner', 'SchedulerTwinArchiveCleaner', 'Scheduler for clearing twin archive table', false),
       (5003, 50, 'org.twins.core.featurer.scheduler.SchedulerTwinChangeTaskRunner', 'SchedulerTwinChangeTaskRunner', 'Scheduler for executing twin changes', false),
       (5004, 50, 'org.twins.core.featurer.scheduler.SchedulerDraftEraseScopeCollectTaskRunner', 'SchedulerDraftEraseScopeCollectTaskRunner', 'Scheduler for executing draft erases', false),
       (5005, 50, 'org.twins.core.featurer.scheduler.SchedulerDraftCommitTaskRunner', 'SchedulerDraftCommitTaskRunner', 'Scheduler for executing draft commits', false),
       (5006, 50, 'org.twins.core.featurer.scheduler.SchedulerSchedulerLogCleaner', 'SchedulerSchedulerLogCleaner', 'Scheduler for cleaning scheduler log table', false),
       (5007, 50, 'org.twins.core.featurer.scheduler.SchedulerAttachmentDeleteTaskCleaner', 'SchedulerAttachmentDeleteTaskCleaner', 'Scheduler for cleaning attachment delete task table', false),
       (5008, 50, 'org.twins.core.featurer.scheduler.SchedulerHistoryNotificationTaskRunner', 'SchedulerHistoryNotificationTaskRunner', 'Scheduler for history notifications sending', false),
       (5009, 50, 'org.twins.core.featurer.scheduler.SchedulerHistoryNotificationTaskCleaner', 'SchedulerHistoryNotificationTaskCleaner', 'Scheduler for cleaning history notification task table', false)
on conflict do nothing;
