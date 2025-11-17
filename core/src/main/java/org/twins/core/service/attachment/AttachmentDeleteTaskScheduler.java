package org.twins.core.service.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.TaskStatus;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;

import java.util.List;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AttachmentDeleteTaskScheduler {

    private final ApplicationContext applicationContext;
    private final AttachmentDeleteTaskRepository attachmentDeleteTaskRepository;

    @Scheduled(fixedDelayString = "${attachment.delete.tasks.collect.scheduler.delay:2000}")
    public void collectDeleteTasks() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("attachmentDeleteTaskScheduler");
            log.debug("Loading attachment delete tasks from database");

            var taskEntityList = attachmentDeleteTaskRepository.findByStatusIn(List.of(TaskStatus.NEED_START));
            if (CollectionUtils.isEmpty(taskEntityList)) {
                log.debug("No attachment delete tasks found");
                return;
            }

            log.info("{} attachment delete task(s) should be processed", taskEntityList.size());
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                taskEntityList.forEach(taskEntity -> {
                    log.info("Running attachment delete task[{}] from status[{}]", taskEntity.getId(), taskEntity.getStatus());
                    taskEntity.setStatus(TaskStatus.IN_PROGRESS);
                    attachmentDeleteTaskRepository.save(taskEntity);
                    AttachmentDeleteTask attachmentDeleteTask = applicationContext.getBean(AttachmentDeleteTask.class, taskEntity);
                    executor.execute(attachmentDeleteTask);
                });
            }
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }
    }
}
