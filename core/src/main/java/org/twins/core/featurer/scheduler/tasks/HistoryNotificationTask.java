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
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.notificator.notifier.Notifier;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.notification.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
@Slf4j
public class HistoryNotificationTask implements Runnable {
    private final HistoryNotificationChunk chunk;
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

    private static final Cache<UUID, Set<String>> batchEventCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    /** Max consecutive infra failures (bulk-stage failure / submission rejection) before a task is terminally failed (poison-pill protection). */
    public static final int MAX_BATCH_ATTEMPTS = 3;
    private static final int ERROR_SNIPPET_MAX_LENGTH = 500;

    public HistoryNotificationTask(HistoryNotificationChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void run() {
        List<HistoryNotificationTaskEntity> tasks = chunk.getTasks();
        Throwable batchError = null;
        try {
            LoggerUtils.logController("historyNotificationTask");
            log.info("Performing batch history notification task: {} task(s)", tasks.size());

            // chunk is one domain (guaranteed by runner) — set ApiUser once so bulk validator filter
            // inside findConfigsForTasks can read domainId; the system NOTIFICATION_SCHEDULER user's
            // permissions are hardcoded (PermissionService.SYSTEM_USER_PERMISSIONS, DOMAIN_TWINS_VIEW_ALL),
            // so secure loads of the bulk phases (loadHead etc.) pass the permission branch of
            // TwinService.isEntityReadDenied in every domain. processTask re-sets ApiUser per history for the locale
            if (chunk.getDomainId() != null) {
                authService.setThreadLocalApiUser(chunk.getDomainId(), null, SystemIds.User.NOTIFICATION_SCHEDULER);
            }
            // 1. one bulk query for configs across the whole chunk + in-memory match + bulk validator filter (mutates chunk)
            historyNotificationService.findConfigsForTasks(chunk);

            // 2. bulk-load config relations ONCE for the whole chunk (was per-history before)
            Collection<HistoryNotificationEntity> allConfigs = chunk.getTasksByConfig().keySet();
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
                historyNotificationRecipientService.resolveRecipientsBatch(chunk);
                // 2c. precompute context for the whole chunk: one collectDataBatch per
                //     (contextCollectorFeaturerId, params) group per contextId; i18n resolved per locale.
                //     Results land on each task.collectedContextByContextId for processTask.
                notificationContextService.collectHistoryContextBatch(chunk);
            }

            // 3. per-history processing — config relations are already loaded; ApiUser is set per history
            //    (locale resolves to the twin creator's locale), status is set per entity, no cross-chunk throw
            for (HistoryNotificationTaskEntity task : tasks) {
                processTask(task, chunk.getConfigsByTask().getOrDefault(task, List.of()));
            }
        } catch (Throwable e) {
            batchError = e;
            log.error("Batch history notification task failed: ", e);
        } finally {
            // safety net: any task not finalized by processTask (e.g. failure during bulk-load) failed for
            // infra reasons, not its own business error — revert it to NEED_START so the next tick retries
            // (attempt_count tracks consecutive failures). Terminal FAILED only after MAX_BATCH_ATTEMPTS
            // consecutive failures, so a persistently failing chunk cannot retry forever (poison pill).
            Timestamp failedAt = Timestamp.from(Instant.now());
            for (HistoryNotificationTaskEntity task : tasks) {
                if (task.getStatusId() == null || task.getStatusId() == HistoryNotificationTaskStatus.IN_PROGRESS) {
                    int attempts = (task.getAttemptCount() == null ? 0 : task.getAttemptCount()) + 1;
                    task.setAttemptCount(attempts);
                    if (attempts >= MAX_BATCH_ATTEMPTS) {
                        task.setStatusId(HistoryNotificationTaskStatus.FAILED)
                                .setStatusDetails("Batch failed after " + attempts + " attempts: "
                                        + LoggerUtils.errorSnippet(batchError, ERROR_SNIPPET_MAX_LENGTH))
                                .setDoneAt(failedAt);
                    } else {
                        task.setStatusId(HistoryNotificationTaskStatus.NEED_START)
                                .setStatusDetails("Batch failed (attempt " + attempts + " of " + MAX_BATCH_ATTEMPTS
                                        + "), will retry: " + LoggerUtils.errorSnippet(batchError, ERROR_SNIPPET_MAX_LENGTH));
                    }
                }
            }
            authService.removeThreadLocalApiUser();
            LoggerUtils.cleanMDC();
            historyNotificationTaskRepository.saveAll(tasks);
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
                // context is precomputed at chunk level in run() (one collectDataBatch per group; i18n per locale)
                var collected = task.getCollectedContextByContextId();
                var context = collected != null ? collected.getOrDefault(channelEvent.getNotificationContextId(), Map.of()) : Map.<String, String>of();
                var notificationChannel = channelEvent.getNotificationChannel();
                var notifier = featurerService.getFeaturer(notificationChannel.getNotifierFeaturerId(), Notifier.class);
                notifier.notify(recipientIds, context, channelEvent.getEventCode(), notificationChannel.getNotifierParams());
            }

            if (recipientsCount == 0) {
                throw new NotificationSkippedException("No recipients were found for " + history.logNormal());
            }

            task
                    .setStatusId(HistoryNotificationTaskStatus.SENT)
                    .setStatusDetails(recipientsCount + " recipients were notified")
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

    private static class NotificationSkippedException extends RuntimeException {
        public NotificationSkippedException(String message) {
            super(message);
        }
    }

}
