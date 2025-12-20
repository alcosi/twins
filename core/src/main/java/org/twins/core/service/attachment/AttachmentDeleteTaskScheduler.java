package org.twins.core.service.attachment;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;
import org.twins.core.enums.attachment.AttachmentDeleteTaskStatus;

import java.util.List;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
public class AttachmentDeleteTaskScheduler {

    private final ApplicationContext applicationContext;
    private final AttachmentDeleteTaskRepository attachmentDeleteTaskRepository;
    @Qualifier("attachmentDeleteTaskExecutor")
    private final Executor executor;

    @Scheduled(fixedDelayString = "${attachment.delete.tasks.collect.scheduler.delay:2000}")
    public void collectDeleteTasks() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("attachmentDeleteTaskScheduler");
            log.debug("Loading attachment delete tasks from database");

            var taskEntityList = attachmentDeleteTaskRepository.findByStatusIn(List.of(AttachmentDeleteTaskStatus.NEED_START));
            if (CollectionUtils.isEmpty(taskEntityList)) {
                log.debug("No attachment delete tasks found");
                return;
            }

            log.info("{} attachment delete task(s) should be processed", taskEntityList.size());
            taskEntityList.forEach(taskEntity -> {
                log.info("Running attachment delete task[{}] from status[{}]", taskEntity.getId(), taskEntity.getStatus());
                taskEntity.setStatus(AttachmentDeleteTaskStatus.IN_PROGRESS);
                attachmentDeleteTaskRepository.save(taskEntity);
                AttachmentDeleteTask attachmentDeleteTask = applicationContext.getBean(AttachmentDeleteTask.class, taskEntity);
                executor.execute(attachmentDeleteTask);
            });
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }
    }
}
