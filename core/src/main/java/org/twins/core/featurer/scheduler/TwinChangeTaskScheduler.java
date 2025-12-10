package org.twins.core.featurer.scheduler;

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

            var collectedTasks = collectTasks(batchSizeParam.extract(properties));
            if (CollectionUtils.isEmpty(collectedTasks)) {
                log.debug("No run factory tasks");
                return "";
            }

            collectedTasks.forEach(task -> task.setStatusId(TwinChangeTaskStatus.IN_PROGRESS));
            var savedTasks = twinChangeTaskRepository.saveAll(collectedTasks);

            log.info("{} twin change task(s) should be processed", collectedTasks.size());
            for (var taskEntity : savedTasks) {
                try {
                    log.info("Running twin change task[{}] from status[{}]", taskEntity.getId(), taskEntity.getStatusId());
                    var draftCommitTask = applicationContext.getBean(TwinChangeTask.class, taskEntity);
                    taskExecutor.execute(draftCommitTask);
                } catch (Exception e) {
                    log.error("Exception ex: {}", e.getMessage(), e);
                }
            }

            return STR."\{collectedTasks.size()} task(s) from db was processed";
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
