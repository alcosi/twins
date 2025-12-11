package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_4707,
        name = "SchedulerAttachmentDeleteTaskCleaner",
        description = "Scheduler for cleaning attachment delete task table"
)
public class SchedulerAttachmentDeleteTaskCleaner extends Scheduler {

    @FeaturerParam(
            name = "batchSize",
            description = "Param to specify the number of tasks that will be collected from db for execution"
    )
    public static final FeaturerParamInt batchSizeParam = new FeaturerParamInt("batchSize");

    private final AttachmentDeleteTaskRepository attachmentDeleteTaskRepository;

    protected String processTasks(Properties properties) {
        try {
            LoggerUtils.logController("attachmentDeleteTaskDeleteScheduler");
            long size = attachmentDeleteTaskRepository.count();

            if (size > 0) {
                if (batchSizeParam.extract(properties) == null) {
                    log.info("Deleting {} attachment delete task records from database", size);
                    attachmentDeleteTaskRepository.deleteAll();
                } else {
                    log.info("Deleting {} attachment delete task from database", batchSizeParam.extract(properties));
                    attachmentDeleteTaskRepository.deleteBatch(PageRequest.of(0, batchSizeParam.extract(properties)));
                }
            } else {
                log.info("No attachment delete task records to be deleted from database");
            }

            return STR."\{batchSizeParam.extract(properties) == null ? size : batchSizeParam.extract(properties)} task(s) from db was deleted";
        } catch (Exception e) {
            log.error("Exception: ", e);
            return STR."Processing tasks failed with exception: \{e}";
        } finally {
            LoggerUtils.cleanMDC();
        }
    }
}
