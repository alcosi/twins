package org.twins.core.service.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskRepository;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class TwinTriggerTaskScheduler {

    final ApplicationContext applicationContext;
    @Qualifier("twinTriggerTaskExecutor")
    final TaskExecutor taskExecutor;
    final TwinTriggerTaskRepository twinTriggerTaskRepository;
    final TwinTriggerTaskService twinTriggerTaskService;

    @Scheduled(fixedDelayString = "${twin.trigger.task.scheduler.delay:2000}")
    public void collectTriggerTasks() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("twinTriggerTaskScheduler$");
            log.debug("Loading twin trigger tasks from database");
            List<TwinTriggerTaskEntity> taskEntityList = twinTriggerTaskRepository.findByStatusIdIn(List.of(TwinTriggerTaskStatus.NEED_START)).stream().toList();
            if (CollectionUtils.isEmpty(taskEntityList)) {
                log.debug("No twin trigger tasks to run");
                return;
            }
            log.info("{} twin trigger task(s) should be processed", taskEntityList.size());
            for (var taskEntity : taskEntityList) {
                try {
                    log.info("Running twin trigger task[{}] from status[{}]", taskEntity.getId(), taskEntity.getStatusId());
                    taskEntity.setStatusId(TwinTriggerTaskStatus.IN_PROGRESS);
                    twinTriggerTaskService.saveSafe(taskEntity);
                    TwinTriggerTask twinTriggerTask = applicationContext.getBean(TwinTriggerTask.class, taskEntity);
                    taskExecutor.execute(twinTriggerTask);
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
