package org.twins.core.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.TwinChangeTaskStatus;
import org.twins.core.dao.twin.TwinChangeTaskEntity;
import org.twins.core.dao.twin.TwinChangeTaskRepository;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinChangeTask;

import java.util.List;
import java.util.Properties;

@RequiredArgsConstructor
@Service
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_4703,
        name = "TwinChangeTaskScheduler",
        description = "Scheduler for executing thin changes"
)
public class TwinChangeTaskScheduler extends Scheduler {

    @FeaturerParam(
            name = "batchSize",
            description = "Param to specify the number of tasks that will be collected from db for execution"
    )
    public static final FeaturerParamInt batchSizeParam = new FeaturerParamInt("batchSize");

    @Qualifier("twinChangeTaskExecutor")
    final TaskExecutor taskExecutor;
    final TwinChangeTaskRepository twinChangeTaskRepository;

    protected String processTasks(Properties properties) {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("factoryTaskScheduler$");

            var taskEntityList = collectTasks(batchSizeParam.extract(properties));
            if (CollectionUtils.isEmpty(taskEntityList)) {
                log.debug("No run factory tasks");
                return "";
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

            return STR."\{taskEntityList.size()} task(s) from db was processed";
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }

        return "";
    }

    private List<TwinChangeTaskEntity> collectTasks(Integer batchSize) {
        log.debug("Loading twin change tasks from database");

        if (batchSize == null) {
            return twinChangeTaskRepository.findByStatusIdIn(List.of(TwinChangeTaskStatus.NEED_START));
        } else {
            return twinChangeTaskRepository.findByStatusIdIn(List.of(TwinChangeTaskStatus.NEED_START), PageRequest.of(0, batchSize));
        }
    }
}
