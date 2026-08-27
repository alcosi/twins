package org.twins.core.service.notification;

import lombok.Getter;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.NotificationChannelEntity;
import org.twins.core.featurer.notificator.notifier.NotifyEvent;

import java.util.*;

/**
 * Single container for a notification chunk: created by the runner with a domain id + the chunk's tasks,
 * then populated by {@code findConfigsForTasks} with the matched/validated config projections, and by
 * the worker's phase A with the pending notify events. Carries lazy helpers (createdByUserIds) reused
 * across the batch stages.
 */
@Getter
public class HistoryNotificationChunk {
    /** chunk-wide domain id (chunk = one domain). */
    private final UUID domainId;
    /** the chunk's tasks, set by the runner. */
    private final List<HistoryNotificationTaskEntity> tasks;
    /** task → valid configs (used by processTask). Populated by findConfigsForTasks. */
    private final Map<HistoryNotificationTaskEntity, List<HistoryNotificationEntity>> configsByTask = new HashMap<>();
    /** config → tasks that matched AND passed validation (inverse projection, used by recipient precompute). */
    private final Map<HistoryNotificationEntity, LinkedHashSet<HistoryNotificationTaskEntity>> tasksByConfig = new HashMap<>();
    /** channel → notify events of the whole chunk (phase A accumulator; phase B sends one batch per channel). */
    private final Map<NotificationChannelEntity, List<NotifyEvent>> pendingByChannel = new LinkedHashMap<>();
    /** task → total recipient count across its events (for the SENT status details). */
    private final Map<HistoryNotificationTaskEntity, Integer> recipientsByTask = new HashMap<>();
    /** lazy: distinct twin-creator user ids across the chunk (for bulk locale resolution). */
    private Set<UUID> createdByUserIds;

    public HistoryNotificationChunk(UUID domainId, List<HistoryNotificationTaskEntity> tasks) {
        this.domainId = domainId;
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    /**
     * Distinct non-null {@code twin.createdByUserId} values across the chunk's tasks (lazy, cached).
     */
    public Set<UUID> getCreatedByUserIds() {
        if (createdByUserIds == null) {
            createdByUserIds = new HashSet<>();
            for (HistoryNotificationTaskEntity task : tasks) {
                if (task.getHistory() != null && task.getHistory().getTwin() != null) {
                    UUID userId = task.getHistory().getTwin().getCreatedByUserId();
                    if (userId != null) {
                        createdByUserIds.add(userId);
                    }
                }
            }
        }
        return createdByUserIds;
    }
}
