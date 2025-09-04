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
import org.twins.core.dao.twin.TwinChangeTaskEntity;
import org.twins.core.dao.twin.TwinChangeTaskRepository;
import org.twins.core.dao.twin.TwinChangeTaskStatus;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class TwinChangeTaskScheduler {
    final ApplicationContext applicationContext;
    @Qualifier("twinChangeTaskExecutor")
    final TaskExecutor taskExecutor;
    final TwinChangeTaskRepository twinChangeTaskRepository;

    @Scheduled(fixedDelayString = "${twin.change.task.scheduler.delay:2000}")
    public void collectChangesTasks() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("factoryTaskScheduler$");
            log.debug("Loading twin change tasks from database");
            List<TwinChangeTaskEntity> taskEntityList = twinChangeTaskRepository.findByStatusIdIn(List.of(TwinChangeTaskStatus.NEED_START));
            if (CollectionUtils.isEmpty(taskEntityList)) {
                log.debug("No run factory tasks");
                return;
            }
            log.info("{} twin change task(s) should be processed", taskEntityList.size());
            for (var taskEntity : taskEntityList) {
                try {
                    log.info("Running twin change task[{}] from status[{}]", taskEntity.getId(), taskEntity.getStatusId());
                    taskEntity.setStatusId(TwinChangeTaskStatus.IN_PROGRESS);
                    twinChangeTaskRepository.save(taskEntity);
                    TwinChangeTask draftCommitTask = applicationContext.getBean(TwinChangeTask.class, taskEntity);
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
