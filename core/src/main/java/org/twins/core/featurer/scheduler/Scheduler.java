package org.twins.core.featurer.scheduler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.twins.core.dao.scheduler.SchedulerLogEntity;
import org.twins.core.dao.scheduler.SchedulerLogRepository;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.UUID;

@FeaturerType(
        id = FeaturerTwins.TYPE_47,
        name = "Scheduler",
        description = "Services for scheduling tasks")
@Slf4j
@Getter
public abstract class Scheduler extends FeaturerTwins {

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    private SchedulerLogRepository schedulerLogRepo;

    public Runnable getRunnableForScheduling(Properties properties, UUID schedulerId) {
        return () -> {
            SchedulerLogEntity schedulerLog = new SchedulerLogEntity();
            long startTime = System.currentTimeMillis();
            String result = processTasks(properties);

            if (!result.isEmpty()) {
                schedulerLog
                        .setId(UUID.randomUUID())
                        .setSchedulerId(schedulerId)
                        .setExecutionTime(System.currentTimeMillis() - startTime)
                        .setResult(result);
            }
        };
    }

    protected abstract String processTasks(Properties properties);
}
