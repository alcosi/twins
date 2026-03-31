package org.twins.core.featurer.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Properties;

@Slf4j
public abstract class SchedulerConsistencyCheck extends Scheduler {
    @Transactional
    protected String processTask(Properties properties) {
        try {
            LoggerUtils.logController(getLogSource());
            long size = invalidRecordsCount();

            if (size == 0) {
                log.info("No invalid [{}] records detected", consistencyCheckName());
                return "0 invalid [" + consistencyCheckName() + "] rows were detected";
            } else {
                LoggerUtils.alertLog.error("Invalid [{}] records detected: {}. Please check the logs for details", consistencyCheckName(), size);
                return size + " invalid [" + consistencyCheckName() + "] rows were detected";
            }
        } catch (Exception e) {
            log.error("Exception: ", e);
            return "Processing tasks failed with exception: " + e;
        } finally {
            LoggerUtils.cleanMDC();
        }
    }


    protected abstract long invalidRecordsCount();
    protected abstract String consistencyCheckName();
}
