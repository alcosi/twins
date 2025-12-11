package org.twins.core.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationStatus;

import java.util.List;

@RequiredArgsConstructor
@Service
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
            List<HistoryNotificationTaskEntity> taskList = historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationStatus.NEED_START));
            if (CollectionUtils.isEmpty(taskList)) {
                log.debug("No notification tasks");
                return;
            }
            log.info("{} history notifications should be processed", taskList.size());
            for (var task : taskList) {
                try {
                    log.info("Running history notification[{}] from status[{}]", task.getId(), task.getStatusId());
                    task.setStatusId(HistoryNotificationStatus.IN_PROGRESS);
                    historyNotificationTaskRepository.save(task);
                    HistoryNotificationTask draftCommitTask = applicationContext.getBean(HistoryNotificationTask.class, task);
                    taskExecutor.execute(draftCommitTask);
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
