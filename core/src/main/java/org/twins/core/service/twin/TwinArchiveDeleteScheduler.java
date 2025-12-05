package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinArchiveRepository;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.scheduler.Scheduler;

import java.util.Properties;

@RequiredArgsConstructor
@Service
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_4702,
        name = "TwinArchiveDeleteScheduler",
        description = "Scheduler for clearing twin archive table"
)
public class TwinArchiveDeleteScheduler extends Scheduler {

    @FeaturerParam(
            name = "batchSize",
            description = "Param to specify the number of tasks that will be collected from db for execution"
    )
    public static final FeaturerParamInt batchSizeParam = new FeaturerParamInt("batchSize");

    private final TwinArchiveRepository twinArchiveRepository;

    protected String processTasks(Properties properties) {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("twinArchiveDeleteScheduler");
            long size = twinArchiveRepository.count();

            if (size > 0) {
                log.info("Deleting {} twin archive records from database", size);

                if (batchSizeParam.extract(properties) == null) {
                    twinArchiveRepository.deleteAll();
                } else {
                    twinArchiveRepository.deleteBatch(PageRequest.of(0, batchSizeParam.extract(properties)));
                }
            } else {
                log.info("No twin archive records to be deleted from database");
            }

            return STR."\{batchSizeParam.extract(properties) == null ? size : batchSizeParam.extract(properties)} task(s) from db was deleted";
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }

        return "";
    }
}
