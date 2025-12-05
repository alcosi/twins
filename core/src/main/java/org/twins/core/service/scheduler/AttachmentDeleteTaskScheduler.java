package org.twins.core.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.AttachmentDeleteTaskEntity;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;
import org.twins.core.enums.attachment.AttachmentDeleteTaskStatus;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.attachment.AttachmentDeleteTask;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_4701,
        name = "AttachmentDeleteTaskScheduler",
        description = "Scheduler for clearing external file storages after twin/attachment deletion"
)
public class AttachmentDeleteTaskScheduler extends Scheduler {

    @FeaturerParam(
            name = "batchSize",
            description = "Param to specify the number of tasks that will be collected from db for execution"
    )
    public static final FeaturerParamInt batchSizeParam = new FeaturerParamInt("batchSize");

    @Qualifier("attachmentDeleteTaskExecutor")
    private final Executor executor;
    private final AttachmentDeleteTaskRepository attachmentDeleteTaskRepository;

    protected String processTasks(Properties properties) {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("attachmentDeleteTaskScheduler");

            var taskEntityList = collectTasks(batchSizeParam.extract(properties));
            if (CollectionUtils.isEmpty(taskEntityList)) {
                log.debug("No attachment delete tasks found");
                return "";
            }

            log.info("{} attachment delete task(s) should be processed", taskEntityList.size());
            taskEntityList.forEach(taskEntity -> {
                log.info("Running attachment delete task[{}] from status[{}]", taskEntity.getId(), taskEntity.getStatus());
                taskEntity.setStatus(AttachmentDeleteTaskStatus.IN_PROGRESS);
                attachmentDeleteTaskRepository.save(taskEntity);
                AttachmentDeleteTask attachmentDeleteTask = applicationContext.getBean(AttachmentDeleteTask.class, taskEntity);
                executor.execute(attachmentDeleteTask);
            });

            return STR."\{batchSizeParam.extract(properties) == null ? "All" : batchSizeParam.extract(properties)} task(s) from db was processed";
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }

        return "";
    }

    private List<AttachmentDeleteTaskEntity> collectTasks(Integer batchSize) {
        log.debug("Loading attachment delete tasks from database");

        if (batchSize == null) {
            return attachmentDeleteTaskRepository.findByStatusIn(List.of(AttachmentDeleteTaskStatus.NEED_START));
        } else {
            return attachmentDeleteTaskRepository.findByStatusIn(List.of(AttachmentDeleteTaskStatus.NEED_START), PageRequest.of(0, batchSize));
        }
    }
}
