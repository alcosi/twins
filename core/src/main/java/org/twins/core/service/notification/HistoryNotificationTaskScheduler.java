package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationTaskStatus;

import java.util.List;

@RequiredArgsConstructor
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
public class HistoryNotificationTaskScheduler {
    final ApplicationContext applicationContext;
    @Qualifier("historyNotificationTaskExecutor")
    final TaskExecutor taskExecutor;
    final HistoryNotificationTaskRepository historyNotificationTaskRepository;

    @Scheduled(fixedDelayString = "${draft.erase.scope.collect.scheduler.delay:2000}")
    public void collectHistoryNotificationTasks() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("historyNotificationScheduler$");
            log.debug("Loading history notifications from database");
            //todo after merging TWINS-611 add batch limit support
            var collectedTasks = historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START));
            if (CollectionUtils.isEmpty(collectedTasks)) {
                log.debug("No notification tasks");
                return;
            }
            collectedTasks.forEach(task -> task.setStatusId(HistoryNotificationTaskStatus.IN_PROGRESS));
            var savedTasks = historyNotificationTaskRepository.saveAll(collectedTasks);

            log.info("{} history notifications should be processed", collectedTasks.size());
            for (var task : savedTasks) {
                try {
                    HistoryNotificationTask historyNotificationTask = applicationContext.getBean(HistoryNotificationTask.class, task);
                    taskExecutor.execute(historyNotificationTask);
                } catch (Exception e) {
                    log.error("Exception ex: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }
    }
}
