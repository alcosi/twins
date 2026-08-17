package org.twins.core.featurer.scheduler;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.scheduler.SchedulerLogEntity;
import org.twins.core.dao.scheduler.SchedulerLogRepository;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.holder.EntityRequestCacheHolder;

import java.util.Properties;

@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@FeaturerType(
        id = FeaturerTwins.TYPE_50,
        name = "Scheduler",
        description = "Services for scheduling tasks")
@Slf4j
public abstract class Scheduler extends FeaturerTwins {

    @Autowired
    @Getter
    protected ApplicationContext applicationContext;
    @Autowired
    private SchedulerLogRepository schedulerLogRepo;

    public Runnable getRunnableForScheduling(Properties properties, SchedulerEntity schedulerEntity) {
        return () -> {
            // Schedulers run on virtual threads (virtualThreadTaskScheduler) with no web-request scope, so
            // EntitySecureFindServiceImpl's REQUEST cache falls back to a thread-local holder. Clear it in
            // finally so the next scheduled tick (and any reused thread) never sees stale cached entities.
            try {
                LoggerUtils.logSession();
                SchedulerLogEntity schedulerLog = new SchedulerLogEntity();
                long startTime = System.currentTimeMillis();
                // using getBean here to prevent errors with Spring proxy (processTask with @Transactional)
                String result = applicationContext.getBean(this.getClass()).processTask(properties);

                if (!result.isEmpty() && schedulerEntity.getLogEnabled()) {
                    schedulerLog
                            .setSchedulerId(schedulerEntity.getId())
                            .setExecutionTime(System.currentTimeMillis() - startTime)
                            .setResult(result);

                    schedulerLogRepo.save(schedulerLog);
                }
            } finally {
                EntityRequestCacheHolder.clear();
            }
        };
    }

    protected final String getLogSource() {
        return StringUtils.uncapitalize(this.getClass().getSimpleName()) + "$";
    }

    protected abstract String processTask(Properties properties);
}
