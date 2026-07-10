package org.twins.core.featurer.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamDuration;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Properties;

@Slf4j
public abstract class SchedulerCleaner extends Scheduler {

    @FeaturerParam(
            name = "interval",
            description = "Param to specify the time interval in which we should search for records",
            exampleValues = "DAYS:3",
            optional = true
    )
    public static final FeaturerParamDuration intervalParam = new FeaturerParamDuration("interval");

    protected String processTask(Properties properties) {
        try {
            LoggerUtils.logController(getLogSource());
            long size = countAll();

            if (size == 0) {
                log.info("No records to be deleted from database");
                return "0 task(s) from db was deleted";
            }

            Duration interval = intervalParam.extract(properties);
            long deletedCount = interval.equals(Duration.ZERO)
                    ? deleteAllRecords(size)
                    : deleteRecordsAfterInterval(interval);

            return deletedCount + " task(s) from db was deleted";
        } catch (Exception e) {
            log.error("Scheduler cleaner [{}] failed", getLogSource(), e);

            return "Processing tasks failed with exception: " + e;
        } finally {
            LoggerUtils.cleanMDC();
        }
    }

    private long deleteAllRecords(long totalCount) {
        log.info("Deleting {} records from database", totalCount);
        deleteAll();

        return totalCount;
    }

    // maybe use 1 db request
    private long deleteRecordsAfterInterval(Duration interval) {
        Timestamp createdBefore = Timestamp.valueOf(LocalDateTime.now().minus(interval));
        long count = countAllByCreatedAtBefore(createdBefore);

        log.info("Deleting {} records from database", count);
        deleteAllByCreatedAtBefore(createdBefore);

        return count;
    }

    protected abstract void deleteAll();
    protected abstract long countAll();
    protected abstract void deleteAllByCreatedAtBefore(Timestamp createdBefore);
    protected abstract long countAllByCreatedAtBefore(Timestamp createdBefore);
}
