package org.twins.core.featurer.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.ListUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.scheduler.tasks.HistoryNotificationTask;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.notification.HistoryNotificationChunk;
import org.twins.core.service.notification.HistoryNotificationTaskService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;

@Service
@Featurer(
        id = FeaturerTwins.ID_5008,
        name = "SchedulerHistoryNotificationTaskRunner",
        description = "Scheduler for history notifications sending"
)
@Slf4j
public class SchedulerHistoryNotificationTaskRunner extends SchedulerTaskRunner<HistoryNotificationTask, HistoryNotificationTaskEntity> {

    @FeaturerParam(
            name = "processBatchSize",
            description = "Number of task entities processed by a single batch HistoryNotificationTask Runnable",
            optional = true,
            defaultValue = "100"
    )
    public static final FeaturerParamInt processBatchSizeParam = new FeaturerParamInt("processBatchSize");

    private final HistoryNotificationTaskRepository historyNotificationTaskRepository;
    private final HistoryService historyService;
    private final HistoryNotificationTaskService historyNotificationTaskService;
    // base SchedulerTaskRunner.taskExecutor is private — keep a local reference for the overridden dispatch
    private final Executor taskExecutor;

    @Autowired
    public SchedulerHistoryNotificationTaskRunner(@Qualifier("historyNotificationTaskExecutor") Executor taskExecutor,
                                                  HistoryNotificationTaskRepository historyNotificationTaskRepository,
                                                  HistoryService historyService,
                                                  HistoryNotificationTaskService historyNotificationTaskService) {
        super(taskExecutor);
        this.taskExecutor = taskExecutor;
        this.historyNotificationTaskRepository = historyNotificationTaskRepository;
        this.historyService = historyService;
        this.historyNotificationTaskService = historyNotificationTaskService;
    }

    /**
     * Overrides the base per-entity dispatch: collects a batch, bulk-loads context, groups by domainId
     * (one batch Runnable never mixes tenants), splits each domain bucket into chunks of processBatchSize
     * and submits one batch HistoryNotificationTask per chunk.
     */
    @Override
    protected String processTask(Properties properties) {
        try {
            LoggerUtils.logController(getLogSource());

            Integer batchSize = batchSizeParam.extract(properties);
            List<HistoryNotificationTaskEntity> collected = batchSize == null ? collectAll() : collectBatch(batchSize);
            if (CollectionUtils.isEmpty(collected)) {
                log.debug("No tasks were collected");
                return "";
            }

            setStatusAndSave(collected); // mutates collected in-place (status + bulk-load)
            log.info("{} history notification task(s) need to be done", collected.size());

            Integer configuredProcessBatchSize = processBatchSizeParam.extract(properties);
            int processBatchSize = (configuredProcessBatchSize == null || configuredProcessBatchSize <= 0)
                    ? Math.max(collected.size(), 1)
                    : configuredProcessBatchSize;

            // group by domainId — isolation guarantee: one Runnable = one domain
            Map<UUID, List<HistoryNotificationTaskEntity>> byDomain = new HashMap<>();
            List<HistoryNotificationTaskEntity> noDomain = new ArrayList<>();
            for (HistoryNotificationTaskEntity entity : collected) {
                UUID domainId = extractDomainId(entity);
                if (domainId == null) {
                    noDomain.add(entity);
                } else {
                    byDomain.computeIfAbsent(domainId, k -> new ArrayList<>()).add(entity);
                }
            }

            List<HistoryNotificationTaskEntity> failed = new ArrayList<>();
            for (var entry : byDomain.entrySet()) {
                dispatchChunks(entry.getKey(), entry.getValue(), processBatchSize, failed);
            }
            // entities without a domain are skipped outright — notification makes no sense for out-of-domain twins
            if (!noDomain.isEmpty()) {
                Timestamp skippedAt = Timestamp.from(Instant.now());
                for (HistoryNotificationTaskEntity task : noDomain) {
                    task.setStatusId(HistoryNotificationTaskStatus.SKIPPED)
                            .setStatusDetails("Twin is out of domain")
                            .setDoneAt(skippedAt);
                }
                historyNotificationTaskRepository.saveAll(noDomain);
                log.info("{} task(s) skipped — twin is out of domain", noDomain.size());
            }

            if (!failed.isEmpty()) {
                try {
                    revertStatusAndSave(failed);
                    log.warn("{} task(s) submission failed — status reverted, they will be recollected", failed.size());
                } catch (Exception revertEx) {
                    log.error("Failed to revert status for {} task(s)", failed.size(), revertEx);
                }
            }

            return collected.size() + " task(s) from db was processed";
        } catch (Exception e) {
            log.error("Exception: ", e);
            return "Processing tasks failed with exception: " + e;
        } finally {
            LoggerUtils.cleanMDC();
        }
    }

    private void dispatchChunks(UUID domainId, List<HistoryNotificationTaskEntity> bucket, int processBatchSize, List<HistoryNotificationTaskEntity> failed) {
        for (List<HistoryNotificationTaskEntity> partition : ListUtils.partition(bucket, processBatchSize)) {
            try {
                HistoryNotificationChunk chunk = new HistoryNotificationChunk(domainId, partition);
                HistoryNotificationTask task = applicationContext.getBean(HistoryNotificationTask.class, chunk);
                taskExecutor.execute(task);
            } catch (Exception e) {
                log.error("Task chunk ({} task(s)) submission rejected, will be reverted", partition.size(), e);
                failed.addAll(partition);
            }
        }
    }

    private UUID extractDomainId(HistoryNotificationTaskEntity entity) {
        var history = entity.getHistory();
        if (history == null || history.getTwin() == null || history.getTwin().getTwinClass() == null) {
            return null;
        }
        return history.getTwin().getTwinClass().getDomainId();
    }

    @Override
    protected Class<HistoryNotificationTask> getTaskClass() {
        return HistoryNotificationTask.class;
    }

    @Override
    protected Collection<HistoryNotificationTaskEntity> setStatusAndSave(Collection<HistoryNotificationTaskEntity> collectedEntities) {
        try {
            // order matters: history must be populated before loadHistoryActors reads task.getHistory()
            historyNotificationTaskService.loadHistory(collectedEntities);
            loadHistoryActors(collectedEntities);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
        collectedEntities.forEach(task -> task.setStatusId(HistoryNotificationTaskStatus.IN_PROGRESS));
        historyNotificationTaskRepository.saveAll(collectedEntities);
        return collectedEntities;
    }

    private void loadHistoryActors(Collection<HistoryNotificationTaskEntity> tasks) throws ServiceException {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        var historyEntities = tasks.stream().map(HistoryNotificationTaskEntity::getHistory).toList();
        historyService.loadUser(historyEntities);
        // unsafe: scheduler thread has no ApiUser / request context — the secure path would throw on the
        // request-scoped ApiUser proxy; domain isolation is guaranteed later by chunk grouping (one chunk = one domain)
        historyService.loadTwinUnsafe(historyEntities);
    }

    @Override
    protected void revertStatusAndSave(Collection<HistoryNotificationTaskEntity> entities) {
        // submission rejection is an infra failure too — count it towards the poison-pill threshold,
        // a persistently rejecting executor must not retry these tasks forever
        Timestamp failedAt = Timestamp.from(Instant.now());
        for (HistoryNotificationTaskEntity entity : entities) {
            int attempts = (entity.getAttemptCount() == null ? 0 : entity.getAttemptCount()) + 1;
            entity.setAttemptCount(attempts);
            if (attempts >= HistoryNotificationTask.MAX_BATCH_ATTEMPTS) {
                entity.setStatusId(HistoryNotificationTaskStatus.FAILED)
                        .setStatusDetails("Chunk submission failed after " + attempts + " attempts")
                        .setDoneAt(failedAt);
            } else {
                entity.setStatusId(HistoryNotificationTaskStatus.NEED_START)
                        .setStatusDetails("Chunk submission failed (attempt " + attempts + " of "
                                + HistoryNotificationTask.MAX_BATCH_ATTEMPTS + "), will retry");
            }
        }
        historyNotificationTaskRepository.saveAll(entities);
    }

    @Override
    protected List<HistoryNotificationTaskEntity> collectAll() {
        var historyTasks = historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START));
        if (CollectionUtils.isEmpty(historyTasks)) {
            return Collections.emptyList();
        }
        return historyTasks;
    }

    @Override
    protected List<HistoryNotificationTaskEntity> collectBatch(int batchSize) {
        return historyNotificationTaskRepository.findByStatusIdIn(List.of(HistoryNotificationTaskStatus.NEED_START), PageRequest.of(0, batchSize));
    }
}
