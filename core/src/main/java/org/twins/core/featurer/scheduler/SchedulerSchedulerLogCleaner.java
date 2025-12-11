package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.scheduler.SchedulerLogRepository;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@RequiredArgsConstructor
@Service
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_4706,
        name = "SchedulerSchedulerLogCleaner",
        description = "Scheduler for cleaning scheduler log table"
)
public class SchedulerSchedulerLogCleaner extends Scheduler {

    @FeaturerParam(
            name = "batchSize",
            description = "Param to specify the number of tasks that will be collected from db for execution"
    )
    public static final FeaturerParamInt batchSizeParam = new FeaturerParamInt("batchSize");

    private final SchedulerLogRepository schedulerLogRepository;

    @Override
    protected String processTasks(Properties properties) {
        try {
            LoggerUtils.logController("schedulerLogDeleteScheduler");
            long size = schedulerLogRepository.count();

            if (size > 0) {
                if (batchSizeParam.extract(properties) == null) {
                    log.info("Deleting {} scheduler log records from database", size);
                    schedulerLogRepository.deleteAll();
                } else {
                    log.info("Deleting {} scheduler log records from database", batchSizeParam.extract(properties));
                    schedulerLogRepository.deleteBatch(PageRequest.of(0, batchSizeParam.extract(properties)));
                }
            } else {
                log.info("No scheduler log records to be deleted from database");
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
