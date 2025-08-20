package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerStatus;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerTaskEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerTaskRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class TwinflowTransitionTriggerTaskScheduler {

    final ApplicationContext applicationContext;
    @Qualifier("twinChangeTaskExecutor")
    final TaskExecutor taskExecutor;
    final TwinflowTransitionTriggerTaskRepository twinflowTransitionTriggerTaskRepository;

    @Scheduled(fixedDelayString = "${draft.erase.scope.collect.scheduler.delay:2000}")
    public void collectChangesTasks() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("factoryTaskScheduler$");
            log.debug("Loading twin change tasks from database");
            List<TwinflowTransitionTriggerTaskEntity> taskEntityList = twinflowTransitionTriggerTaskRepository.findByStatusIdIn(List.of(TwinflowTransitionTriggerStatus.NEED_START));
            if (CollectionUtils.isEmpty(taskEntityList)) {
                log.debug("No run factory tasks");
                return;
            }
            log.info("{} twin change task(s) should be processed", taskEntityList.size());
            for (var taskEntity : taskEntityList) {
                try {
                    log.info("Running twin change task[{}] from status[{}]", taskEntity.getId(), taskEntity.getStatusId());
                    taskEntity.setStatusId(TwinflowTransitionTriggerStatus.IN_PROGRESS);
                    twinflowTransitionTriggerTaskRepository.save(taskEntity);
                    TwinflowTransitionTriggerTask transitionTriggerTask = applicationContext.getBean(TwinflowTransitionTriggerTask.class, taskEntity);
                    taskExecutor.execute(transitionTriggerTask);
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
