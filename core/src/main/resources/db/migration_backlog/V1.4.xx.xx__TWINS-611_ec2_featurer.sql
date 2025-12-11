insert into featurer(id, featurer_type_id, class, name, description, deprecated)
values (4701, 47, 'org.twins.core.featurer.scheduler.SchedulerAttachmentDeleteSchedulerAttachmentDelete', 'Scheduler for clearing external file storages after twin/attachment deletion', false),
       (4702, 47, 'org.twins.core.featurer.scheduler.SchedulerTwinArchiveDelete', 'SchedulerTwinArchiveDelete', 'Scheduler for clearing twin archive table', false),
       (4703, 47, 'org.twins.core.featurer.scheduler.SchedulerTwinChangeSchedulerTwinChange', 'Scheduler for executing thin changes', false),
       (4704, 47, 'org.twins.core.featurer.scheduler.SchedulerDraftEraseScopeCollect', 'SchedulerDraftEraseScopeCollect', 'Scheduler for executing draft erases', false),
       (4705, 47, 'org.twins.core.featurer.scheduler.SchedulerDraftCommit', 'SchedulerDraftCommit', 'Scheduler for executing draft commits', false),
       (4706, 47, 'org.twins.core.featurer.scheduler.SchedulerSchedulerLogCleanerr', 'SchedulerSchedulerLogCleaner', 'Scheduler for cleaning scheduler log table', false),
       (4707, 47, 'org.twins.core.featurer.scheduler.SchedulerAttachmentDeleteTaskCleanerr', 'SchedulerAttachmentDeleteTaskCleaner', 'Scheduler for cleaning attachment delete task table', false)
on conflict do nothing;
