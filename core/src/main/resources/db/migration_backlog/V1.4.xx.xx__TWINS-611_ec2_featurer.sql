insert into featurer(id, featurer_type_id, class, name, description, deprecated)
values (4701, 47, 'org.twins.core.featurer.scheduler.AttachmentDeleteTaskScheduler', 'AttachmentDeleteTaskScheduler', 'Scheduler for clearing external file storages after twin/attachment deletion', false),
       (4702, 47, 'org.twins.core.featurer.scheduler.TwinArchiveDeleteScheduler', 'TwinArchiveDeleteScheduler', 'Scheduler for clearing twin archive table', false),
       (4703, 47, 'org.twins.core.featurer.scheduler.TwinChangeTaskScheduler', 'TwinChangeTaskScheduler', 'Scheduler for executing thin changes', false),
       (4704, 47, 'org.twins.core.featurer.scheduler.DraftEraseScopeCollectScheduler', 'DraftEraseScopeCollectScheduler', 'Scheduler for executing draft erases', false),
       (4705, 47, 'org.twins.core.featurer.scheduler.DraftCommitScheduler', 'DraftCommitScheduler', 'Scheduler for executing draft commits', false),
       (4706, 47, 'org.twins.core.featurer.scheduler.SchedulerLogDeleteScheduler', 'SchedulerLogDeleteScheduler', 'Scheduler for clearing scheduler log table', false),
       (4707, 47, 'org.twins.core.featurer.scheduler.AttachmentDeleteTaskDeleteScheduler', 'AttachmentDeleteTaskDeleteScheduler', 'Scheduler for attachment delete task table', false)
on conflict do nothing;
