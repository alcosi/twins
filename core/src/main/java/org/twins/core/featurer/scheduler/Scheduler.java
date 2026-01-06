package org.twins.core.featurer.scheduler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.scheduler.SchedulerLogEntity;
import org.twins.core.dao.scheduler.SchedulerLogRepository;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@FeaturerType(
        id = FeaturerTwins.TYPE_47,
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
            LoggerUtils.logSession();
            SchedulerLogEntity schedulerLog = new SchedulerLogEntity();
            long startTime = System.currentTimeMillis();
            // using getBean here to prevent errors with Spring proxy (processTasks with @Transactional)
            String result = applicationContext.getBean(this.getClass()).processTasks(properties);

            if (!result.isEmpty() && schedulerEntity.getLogEnabled()) {
                schedulerLog
                        .setSchedulerId(schedulerEntity.getId())
                        .setExecutionTime(System.currentTimeMillis() - startTime)
                        .setResult(result);

                schedulerLogRepo.save(schedulerLog);
            }
        };
    }

    protected final String getLogSource() {
        return StringUtils.uncapitalize(this.getClass().getSimpleName());
    }

    protected abstract String processTasks(Properties properties);
}
