package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryTaskEntity;
import org.twins.core.dao.factory.TwinFactoryTaskRepository;
import org.twins.core.dao.factory.TwinFactoryTaskStatus;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class FactoryTaskScheduler {
    final ApplicationContext applicationContext;
    @Qualifier("runFactoryTaskExecutor")
    final TaskExecutor taskExecutor;
    final TwinFactoryTaskRepository twinFactoryTaskRepository;

    @Scheduled(fixedDelayString = "${draft.erase.scope.collect.scheduler.delay:2000}")
    public void collectEraseScope() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("factoryTaskScheduler$");
            log.debug("Loading run factory tasks from database");
            List<TwinFactoryTaskEntity> taskEntityList = twinFactoryTaskRepository.findByStatusIdIn(List.of(TwinFactoryTaskStatus.NEED_START));
            if (CollectionUtils.isEmpty(taskEntityList)) {
                log.debug("No run factory tasks");
                return;
            }
            log.info("{} run factory task should be processed", taskEntityList.size());
            for (var taskEntity : taskEntityList) {
                try {
                    log.info("Running run factory task[{}] from status[{}]", taskEntity.getId(), taskEntity.getStatusId());
                    taskEntity.setStatusId(TwinFactoryTaskStatus.IN_PROGRESS);
                    twinFactoryTaskRepository.save(taskEntity);
                    FactoryTask draftCommitTask = applicationContext.getBean(FactoryTask.class, taskEntity);
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
