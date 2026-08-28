package org.twins.core.featurer.scheduler.tasks;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.NotificationChannelEntity;
import org.twins.core.dao.notification.NotificationChannelEventEntity;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.notificator.notifier.Notifier;
import org.twins.core.featurer.notificator.notifier.NotifyEvent;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.notification.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
@Slf4j
public class HistoryNotificationTask implements Runnable {
    private final HistoryNotificationChunk chunk;
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
    private HistoryNotificationTaskService historyNotificationTaskService;
    @Autowired
    private AuthService authService;

    /**
     * uniqueInBatch dedup, shared across chunks (static): batch id → event codes already notified in
     * that batch. The key includes the domain id — the cache is JVM-global, so equal batch ids from
     * different tenants must never suppress each other's events.
     */
    private static final Cache<String, Set<String>> batchEventCache = Caffeine.newBuilder()
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

            // 3. phase A — per-history build of notify events (recipients/context precomputed, ApiUser per
            //    history for the locale, per-task isolation for skip/business-failure statuses);
            //    phase B — ONE notify() call per notifier channel for the whole chunk. doneAt is DB-owned
            //    (trigger on the SENT transition), so identical status tuples collapse into bulk groups
            collectNotifyEvents(chunk);
            dispatchNotify(chunk);
            for (HistoryNotificationTaskEntity task : tasks) {
                // not terminal yet (null = freshly built in tests / never claimed, IN_PROGRESS = claimed):
                // survived phase A without skip/failure and phase B without a failed event → SENT.
                // read the count BEFORE mutating — @Data entities are mutable map keys (hashCode drift)
                int recipientsCount = chunk.getRecipientsByTask().getOrDefault(task, 0);
                if (task.getStatusId() == null || task.getStatusId() == HistoryNotificationTaskStatus.IN_PROGRESS) {
                    task.setStatusId(HistoryNotificationTaskStatus.SENT)
                            .setStatusDetails(recipientsCount + " recipients were notified");
                }
            }
        } catch (Throwable e) {
            batchError = e;
            log.error("Batch history notification task failed: ", e);
        } finally {
            // safety net: any task not finalized by processTask (e.g. failure during bulk-load) failed for
            // infra reasons, not its own business error — revert it to NEED_START so the next tick retries
            // (attempt_count tracks consecutive failures). Terminal FAILED only after MAX_BATCH_ATTEMPTS
            // consecutive failures, so a persistently failing chunk cannot retry forever (poison pill).
            for (HistoryNotificationTaskEntity task : tasks) {
                if (task.getStatusId() == null || task.getStatusId() == HistoryNotificationTaskStatus.IN_PROGRESS) {
                    int attempts = (task.getAttemptCount() == null ? 0 : task.getAttemptCount()) + 1;
                    task.setAttemptCount(attempts);
                    if (attempts >= MAX_BATCH_ATTEMPTS) {
                        task.setStatusId(HistoryNotificationTaskStatus.FAILED)
                                .setStatusDetails("Batch failed after " + attempts + " attempts: "
                                        + LoggerUtils.errorSnippet(batchError, ERROR_SNIPPET_MAX_LENGTH));
                    } else {
                        task.setStatusId(HistoryNotificationTaskStatus.NEED_START)
                                .setStatusDetails("Batch failed (attempt " + attempts + " of " + MAX_BATCH_ATTEMPTS
                                        + "), will retry: " + LoggerUtils.errorSnippet(batchError, ERROR_SNIPPET_MAX_LENGTH));
                    }
                }
            }
            authService.removeThreadLocalApiUser();
            LoggerUtils.cleanMDC();
            historyNotificationTaskService.updateStatuses(tasks);
        }
    }

    /**
     * Phase A: builds every task's notify events into the chunk's per-channel buckets
     * ({@code chunk.pendingByChannel} / {@code chunk.recipientsByTask}). Recipients/context are
     * precomputed at chunk level; only statuses for skip/business-failure paths are set here — SENT is
     * finalized after phase B (dispatchNotify) with the chunk-wide timestamp.
     */
    private void collectNotifyEvents(HistoryNotificationChunk chunk) {
        for (HistoryNotificationTaskEntity task : chunk.getTasks()) {
            collectNotifyEvents(chunk, task);
        }
    }

    private void collectNotifyEvents(HistoryNotificationChunk chunk, HistoryNotificationTaskEntity task) {
        List<HistoryNotificationEntity> configs = chunk.getConfigsByTask().getOrDefault(task, List.of());
        try {
            var history = task.getHistory();
            LoggerUtils.logSession(history.getHistoryBatchId());
            LoggerUtils.logPrefix("HISTORY[" + task.getId() + "]:");
            log.info("Performing history notification task: {}", task.logDetailed());
            if (CollectionUtils.isEmpty(configs)) {
                throw new NotificationSkippedException("No configs found for " + history.logNormal());
            }
            var twin = history.getTwin();
            if (twin == null || twin.getTwinClass() == null || twin.getTwinClass().getDomainId() == null) {
                throw new NotificationSkippedException("Twin is out of domain");
            }

            authService.setThreadLocalApiUser(twin.getTwinClass().getDomainId(), twin.getOwnerBusinessAccountId(), twin.getCreatedByUserId());


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
                    String dedupKey = twin.getTwinClass().getDomainId() + ":" + history.getHistoryBatchId();
                    var processedEvents = batchEventCache.get(dedupKey, k -> ConcurrentHashMap.newKeySet());
                    if (processedEvents != null && !processedEvents.add(channelEvent.getEventCode())) {
                        log.info("Notification for event {} in batch {} skipped due to uniqueInBatch flag", channelEvent.getEventCode(), history.getHistoryBatchId());
                        continue;
                    }
                }

                // configs are already validator-filtered in HistoryNotificationService.findConfigsForTasks.
                // recipients are precomputed at chunk level in run() (one resolveBatch per resolver group).
                var recipientIds = resolveRecipientIds(task, entry.getValue());
                if (recipientIds.isEmpty()) {
                    continue;
                }

                recipientsCount += recipientIds.size();
                addNotifyEventsByLocale(chunk, task, channelEvent, recipientIds);
            }

            if (recipientsCount == 0) {
                throw new NotificationSkippedException("No recipients were found for " + history.logNormal());
            }
            chunk.getRecipientsByTask().put(task, recipientsCount);
        } catch (NotificationSkippedException e) {
            log.info(e.getMessage());
            task.setStatusId(HistoryNotificationTaskStatus.SKIPPED).setStatusDetails(e.getMessage());
        } catch (Throwable e) {
            // notify itself no longer runs here (phase B, dispatchNotify) — only building can fail
            log.error("Exception: ", e);
            task.setStatusId(HistoryNotificationTaskStatus.FAILED).setStatusDetails(e.getMessage());
        } finally {
            authService.removeThreadLocalApiUser();
        }
    }

    /** Union of the recipient user ids resolved for these configs (recipients are precomputed at chunk level). */
    private static Set<UUID> resolveRecipientIds(HistoryNotificationTaskEntity task, List<HistoryNotificationEntity> configs) {
        Set<UUID> recipientIds = new HashSet<>();
        Map<UUID, Set<UUID>> resolvedByRecipient = task.getResolvedRecipientsByRecipientId();
        if (resolvedByRecipient == null) {
            return recipientIds;
        }
        for (HistoryNotificationEntity config : configs) {
            recipientIds.addAll(resolvedByRecipient.getOrDefault(config.getHistoryNotificationRecipientId(), Set.of()));
        }
        return recipientIds;
    }

    /**
     * Splits one channel event's recipients by locale and adds a NotifyEvent per locale group: the
     * context template is precomputed at chunk level and still carries {@code #i18n=<uuid>}
     * placeholders, so it is materialized for each group's locale — every recipient gets the
     * notification in their own language (the null-locale group's placeholders become empty strings).
     */
    private void addNotifyEventsByLocale(HistoryNotificationChunk chunk, HistoryNotificationTaskEntity task,
                                         NotificationChannelEventEntity channelEvent, Set<UUID> recipientIds) {
        Map<String, String> template = contextTemplate(task, channelEvent);
        for (var localeGroup : chunk.splitRecipientsByLocale(recipientIds).entrySet()) {
            var context = notificationContextService.materializeContext(template, chunk, localeGroup.getKey());
            chunk.addPendingEvent(channelEvent.getNotificationChannel(),
                    new NotifyEvent(task, localeGroup.getValue(), context, channelEvent.getEventCode()));
        }
    }

    /** The task's collected context template for the channel event's context (empty when none was collected). */
    private static Map<String, String> contextTemplate(HistoryNotificationTaskEntity task, NotificationChannelEventEntity channelEvent) {
        var collected = task.getCollectedContextByContextId();
        return collected != null ? collected.getOrDefault(channelEvent.getNotificationContextId(), Map.of()) : Map.of();
    }

    /**
     * Phase B: one notify() batch per notifier channel across the whole chunk. Per-event failures come
     * back as the failed-event set (see Notifier) and are attributed to their tasks; a channel-level
     * throw fails every task that contributed events to the channel. Tasks not marked terminal here or
     * in phase A are finalized as SENT by the caller.
     */
    private void dispatchNotify(HistoryNotificationChunk chunk) {
        for (var entry : chunk.getPendingByChannel().entrySet()) {
            NotificationChannelEntity channel = entry.getKey();
            List<NotifyEvent> pending = entry.getValue();
            Set<NotifyEvent> failedEvents;
            try {
                Notifier notifier = featurerService.getFeaturer(channel.getNotifierFeaturerId(), Notifier.class);
                failedEvents = notifier.notify(channel.getNotifierParams(), new LinkedHashSet<>(pending));
            } catch (Exception e) {
                log.error("Notify failed for the whole channel, {} event(s) affected", pending.size(), e);
                for (NotifyEvent event : pending) {
                    event.task().setStatusId(HistoryNotificationTaskStatus.FAILED)
                            .setStatusDetails("Notify failed: " + LoggerUtils.errorSnippet(e, ERROR_SNIPPET_MAX_LENGTH));
                }
                continue;
            }
            if (CollectionUtils.isEmpty(failedEvents)) {
                continue;
            }
            for (NotifyEvent event : pending) {
                if (failedEvents.contains(event)) {
                    event.task().setStatusId(HistoryNotificationTaskStatus.FAILED)
                            .setStatusDetails("Notify failed (see notifier log)");
                }
            }
        }
    }

    private static class NotificationSkippedException extends RuntimeException {
        public NotificationSkippedException(String message) {
            super(message);
        }
    }

}
