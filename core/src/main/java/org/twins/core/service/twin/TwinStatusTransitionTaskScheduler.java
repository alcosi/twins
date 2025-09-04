package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerStatus;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerTaskEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerTaskRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class TwinStatusTransitionTaskScheduler {
    final ApplicationContext applicationContext;
    @Qualifier("twinStatusTransitionTaskExecutor")
    final TaskExecutor taskExecutor;
    final TwinStatusTransitionTriggerTaskRepository twinStatusTransitionTaskRepository;

    @Scheduled(fixedDelayString = "${twin.status.transition.task.scheduler.delay:2000}")
    public void collectChangesTasks() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("factoryTaskScheduler$");
            log.debug("Loading twin change tasks from database");
            List<TwinStatusTransitionTriggerTaskEntity> taskEntityList = twinStatusTransitionTaskRepository.findByStatusIdIn(List.of(TwinStatusTransitionTriggerStatus.NEED_START));
            if (CollectionUtils.isEmpty(taskEntityList)) {
                log.debug("No run factory tasks");
                return;
            }
            log.info("{} twin change task(s) should be processed", taskEntityList.size());
            for (var taskEntity : taskEntityList) {
                try {
                    log.info("Running twin change task[{}] from status[{}]", taskEntity.getId(), taskEntity.getStatusId());
                    taskEntity.setStatusId(TwinStatusTransitionTriggerStatus.IN_PROGRESS);
                    twinStatusTransitionTaskRepository.save(taskEntity);
                    TwinStatusTransitionTask twinStatusTriggerTask = applicationContext.getBean(TwinStatusTransitionTask.class, taskEntity);
                    taskExecutor.execute(twinStatusTriggerTask);
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
