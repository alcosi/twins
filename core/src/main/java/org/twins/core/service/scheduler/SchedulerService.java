package org.twins.core.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.scheduler.SchedulerRepository;
import org.twins.core.exception.ErrorCodeTwins;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final SchedulerRepository schedulerRepository;
    private final FeaturerService featurerService;
    private final Map<UUID, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    public void init() {
        try {
            startAll();
        } catch (ServiceException e) {
            log.error("Error in schedulers init: ", e);

            for (var future : scheduledTasks.values()) {
                future.cancel(true);
            }

            scheduledTasks.clear();
        }
    }

    public void startAll() throws ServiceException {
        log.info("Starting all schedulers");

        List<SchedulerEntity> activeSchedules = schedulerRepository.findAll().stream()
                .filter(SchedulerEntity::isActive)
                .toList();

        for (SchedulerEntity config : activeSchedules) {
            Properties properties = featurerService.extractProperties(config.getFeaturerId(), config.getSchedulerParams(), new HashMap<>());
            Scheduler scheduler = featurerService.getFeaturer(config.getFeaturerId(), Scheduler.class);
            Runnable schedulerTask = scheduler.getRunnableForScheduling(properties, config.getId());

            scheduleTask(schedulerTask, config);
        }
    }

    public void stopAll(boolean force) {
        log.info("Stopping all schedulers with force {}", force);

        for (var scheduledFuture : scheduledTasks.values()) {
            scheduledFuture.cancel(force);
        }

        scheduledTasks.clear();
    }

    public void restartAll(boolean force) throws ServiceException {
        stopAll(force);
        startAll();
    }

    public void startOne(UUID schedulerId) throws ServiceException {
        log.info("Starting scheduler[id:{}]", schedulerId);
        SchedulerEntity config = schedulerRepository.findById(schedulerId).orElseThrow(() -> new ServiceException(ErrorCodeCommon.UUID_UNKNOWN));

        if (!config.isActive()) {
            throw new ServiceException(ErrorCodeTwins.SCHEDULER_IS_NOT_ACTIVE);
        }

        Properties properties = featurerService.extractProperties(config.getFeaturerId(), config.getSchedulerParams(), new HashMap<>());
        Scheduler scheduler = featurerService.getFeaturer(config.getFeaturerId(), Scheduler.class);
        Runnable schedulerTask = scheduler.getRunnableForScheduling(properties, config.getId());

        scheduleTask(schedulerTask, config);
    }

    public void stopOne(UUID schedulerId, boolean force) throws ServiceException {
        log.info("Stopping scheduler[id:{}] with force {}", schedulerId, force);

        if (scheduledTasks.get(schedulerId) == null) {
            throw new ServiceException(ErrorCodeTwins.SCHEDULER_IS_NOT_RUNNING);
        } else {
            scheduledTasks.get(schedulerId).cancel(force);
            scheduledTasks.remove(schedulerId);
        }
    }

    public void restartOne(UUID schedulerId, boolean force) throws ServiceException {
        startOne(schedulerId);
        stopOne(schedulerId, force);
    }

    private void scheduleTask(Runnable runnable, SchedulerEntity schedulerConfig) throws ServiceException {
        if (scheduledTasks.containsKey(schedulerConfig.getId())) {
            throw new ServiceException(ErrorCodeTwins.SCHEDULER_IS_ALREADY_RUNNING);
        }

        ScheduledFuture<?> future;

        if (schedulerConfig.getCron() != null) {
            future = taskScheduler.schedule(
                    runnable,
                    new CronTrigger(schedulerConfig.getCron())
            );
        } else if (schedulerConfig.getFixedRate() != null) {
            future = taskScheduler.scheduleAtFixedRate(
                    runnable,
                    Duration.ofMillis(schedulerConfig.getFixedRate()));
        } else {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS);
        }

        scheduledTasks.put(schedulerConfig.getId(), future);
    }
}
