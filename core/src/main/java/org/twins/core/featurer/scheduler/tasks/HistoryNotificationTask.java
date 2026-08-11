package org.twins.core.featurer.scheduler.tasks;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.featurer.notificator.notifier.Notifier;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.notification.HistoryNotificationRecipientService;
import org.twins.core.service.notification.HistoryNotificationService;
import org.twins.core.service.notification.NotificationChannelEventService;
import org.twins.core.service.notification.NotificationContextService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
@Slf4j
public class HistoryNotificationTask implements Runnable {
    private final List<HistoryNotificationTaskEntity> tasks;
    @Autowired
    private HistoryNotificationTaskRepository historyNotificationTaskRepository;
    @Autowired
    private FeaturerService featurerService;
    @Autowired
    private NotificationContextService notificationContextService;
    @Autowired
    private NotificationChannelEventService notificationChannelEventService;
    @Autowired
    private HistoryNotificationRecipientService historyNotificationRecipientService;
    @Autowired
    private HistoryNotificationService historyNotificationService;
    @Autowired
    private AuthService authService;

    // per-history context cache: historyId -> (contextId -> context). Keyed by historyId because the
    // collected context depends on the HistoryEntity, not just on contextId (different histories with
    // the same contextId produce different contexts).
    private final Map<UUID, Map<UUID, Map<String, String>>> contextCache = new HashMap<>();
    private static final Cache<UUID, Set<String>> batchEventCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    public HistoryNotificationTask(List<HistoryNotificationTaskEntity> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void run() {
        try {
            LoggerUtils.logController("historyNotificationTask");
            log.info("Performing batch history notification task: {} task(s)", tasks.size());

            // chunk is one domain (guaranteed by runner) — set ApiUser once so bulk validator filter
            // inside findConfigsForTasks can read domainId; processTask re-sets it per history for the locale
            setChunkApiUser(tasks);
            // 1. one bulk query for configs across the whole chunk + in-memory match + bulk validator filter
            Map<HistoryNotificationTaskEntity, List<HistoryNotificationEntity>> configsByTask =
                    historyNotificationService.findConfigsForTasks(tasks);

            // 2. bulk-load config relations ONCE for the whole chunk (was per-history before)
            Collection<HistoryNotificationEntity> allConfigs = new LinkedHashSet<>();
            configsByTask.values().forEach(allConfigs::addAll);
            if (!allConfigs.isEmpty()) {
                historyNotificationService.loadNotificationChannelEvent(allConfigs);
                historyNotificationService.loadHistoryNotificationRecipient(allConfigs);
                historyNotificationRecipientService.loadCollectors(allConfigs.stream()
                        .map(HistoryNotificationEntity::getHistoryNotificationRecipient)
                        .filter(Objects::nonNull)
                        .toList());
                notificationChannelEventService.loadNotificationChannel(allConfigs.stream()
                        .map(HistoryNotificationEntity::getNotificationChannelEvent)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList());
                // 2b. precompute recipients for the whole chunk: one resolveBatch per
                //     (resolverFeaturerId, canonical params) group → beforeResolve preloads once per group.
                //     Results land on each task.resolvedRecipientsByRecipientId for processTask.
                historyNotificationRecipientService.resolveRecipientsBatch(configsByTask);
            }

            // 3. per-history processing — config relations are already loaded; ApiUser is set per history
            //    (locale resolves to the twin creator's locale), status is set per entity, no cross-chunk throw
            for (HistoryNotificationTaskEntity task : tasks) {
                processTask(task, configsByTask.getOrDefault(task, List.of()));
            }
        } catch (Throwable e) {
            log.error("Batch history notification task failed: ", e);
        } finally {
            // safety net: any task not finalized by processTask (e.g. failure during bulk-load) -> FAILED
            Timestamp failedAt = Timestamp.from(Instant.now());
            for (HistoryNotificationTaskEntity task : tasks) {
                if (task.getStatusId() == null || task.getStatusId() == HistoryNotificationTaskStatus.IN_PROGRESS) {
                    task.setStatusId(HistoryNotificationTaskStatus.FAILED)
                            .setStatusDetails("Batch processing failed before this task was processed")
                            .setDoneAt(failedAt);
                }
            }
            authService.removeThreadLocalApiUser();
            LoggerUtils.cleanMDC();
            historyNotificationTaskRepository.saveAll(tasks);
        }
    }

    private void setChunkApiUser(List<HistoryNotificationTaskEntity> tasks) {
        for (HistoryNotificationTaskEntity task : tasks) {
            var history = task.getHistory();
            if (history != null && history.getTwin() != null && history.getTwin().getTwinClass() != null) {
                UUID domainId = history.getTwin().getTwinClass().getDomainId();
                if (domainId != null) {
                    authService.setThreadLocalApiUser(domainId, null, null);
                    return;
                }
            }
        }
    }

    private void processTask(HistoryNotificationTaskEntity task, List<HistoryNotificationEntity> configs) {
        try {
            var history = task.getHistory();
            LoggerUtils.logSession(history.getHistoryBatchId());
            LoggerUtils.logPrefix("HISTORY[" + task.getId() + "]:");
            log.info("Performing history notification task: {}", task.logDetailed());

            var twin = history.getTwin();
            if (twin == null || twin.getTwinClass() == null || twin.getTwinClass().getDomainId() == null) {
                throw new NotificationSkippedException("Twin is out of domain");
            }

            authService.setThreadLocalApiUser(twin.getTwinClass().getDomainId(), twin.getOwnerBusinessAccountId(), twin.getCreatedByUserId());

            if (CollectionUtils.isEmpty(configs)) {
                throw new NotificationSkippedException("No configs found for " + history.logNormal());
            }

            var notificationConfigsGroupedByChannelEvent = new KitGroupedObj<>(
                    configs,
                    HistoryNotificationEntity::getId,
                    HistoryNotificationEntity::getNotificationChannelEventId,
                    HistoryNotificationEntity::getNotificationChannelEvent
            );

            var recipientsCount = 0;
            for (var entry : notificationConfigsGroupedByChannelEvent.getGroupedMap().entrySet()) {
                var channelEvent = notificationConfigsGroupedByChannelEvent.getGroupingObject(entry.getKey());

                if (channelEvent.isUniqueInBatch()) {
                    var processedEvents = batchEventCache.get(history.getHistoryBatchId(), k -> ConcurrentHashMap.newKeySet());
                    if (processedEvents != null && !processedEvents.add(channelEvent.getEventCode())) {
                        log.info("Notification for event {} in batch {} skipped due to uniqueInBatch flag", channelEvent.getEventCode(), history.getHistoryBatchId());
                        continue;
                    }
                }

                var recipientIds = new HashSet<UUID>();
                var resolvedByRecipient = task.getResolvedRecipientsByRecipientId();
                for (var config : entry.getValue()) {
                    // configs are already validator-filtered in HistoryNotificationService.findConfigsForTasks.
                    // recipients are precomputed at chunk level in run() (one resolveBatch per resolver group).
                    if (resolvedByRecipient != null) {
                        recipientIds.addAll(resolvedByRecipient.getOrDefault(config.getHistoryNotificationRecipientId(), Set.of()));
                    }
                }

                if (recipientIds.isEmpty()) {
                    continue;
                }

                recipientsCount += recipientIds.size();
                var context = getContext(channelEvent.getNotificationContextId(), history);
                var notificationChannel = channelEvent.getNotificationChannel();
                var notifier = featurerService.getFeaturer(notificationChannel.getNotifierFeaturerId(), Notifier.class);
                notifier.notify(recipientIds, context, channelEvent.getEventCode(), notificationChannel.getNotifierParams());
            }

            if (recipientsCount == 0) {
                throw new NotificationSkippedException("No recipients were found for " + history.logNormal());
            }

            task
                    .setStatusId(HistoryNotificationTaskStatus.SENT)
                    .setStatusDetails(STR."\{recipientsCount} recipients were notified")
                    .setDoneAt(Timestamp.from(Instant.now()));
        } catch (NotificationSkippedException e) {
            log.info(e.getMessage());
            task
                    .setStatusId(HistoryNotificationTaskStatus.SKIPPED)
                    .setStatusDetails(e.getMessage());
        } catch (ServiceException e) {
            log.error(e.log());
            task
                    .setStatusId(HistoryNotificationTaskStatus.FAILED)
                    .setStatusDetails(e.log());
        } catch (Throwable e) {
            log.error("Exception: ", e);
            task
                    .setStatusId(HistoryNotificationTaskStatus.FAILED)
                    .setStatusDetails(e.getMessage());
        } finally {
            authService.removeThreadLocalApiUser();
        }
    }

    private Map<String, String> getContext(UUID contextId, HistoryEntity history) throws ServiceException {
        Map<UUID, Map<String, String>> byHistory = contextCache.computeIfAbsent(history.getId(), k -> new HashMap<>());
        if (byHistory.containsKey(contextId)) {
            return byHistory.get(contextId);
        }
        var context = notificationContextService.collectHistoryContext(contextId, history);
        byHistory.put(contextId, context);
        return context;
    }

    private static class NotificationSkippedException extends RuntimeException {
        public NotificationSkippedException(String message) {
            super(message);
        }
    }
}
