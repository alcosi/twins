package org.twins.core.service.notification;

import lombok.Getter;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.NotificationChannelEntity;
import org.twins.core.featurer.notificator.notifier.NotifyEvent;

import java.util.*;

/**
 * Single container for a notification chunk: created by the runner with a domain id + the chunk's tasks,
 * then populated by {@code findConfigsForTasks} with the matched/validated config projections, by
 * {@code resolveRecipientsBatch} / {@code collectHistoryContextBatch} with the precomputed recipients
 * and per-locale i18n state, and by the worker's phase A with the pending notify events.
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
    /** recipient user id → locale (for per-recipient-locale context translation). Populated by collectHistoryContextBatch. */
    private final Map<UUID, Locale> localeByRecipient = new HashMap<>();
    /** i18n id translations per locale for ALL i18n ids collected in the chunk. Populated by collectHistoryContextBatch. */
    private final Map<Locale, Map<UUID, String>> i18nTranslationsByLocale = new HashMap<>();

    public HistoryNotificationChunk(UUID domainId, List<HistoryNotificationTaskEntity> tasks) {
        this.domainId = domainId;
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    /**
     * Union of all resolved recipient user ids across the chunk's tasks — every user that will receive
     * a notification from this chunk (their locales drive the context translation).
     */
    public Set<UUID> getRecipientUserIds() {
        Set<UUID> recipientIds = new HashSet<>();
        for (HistoryNotificationTaskEntity task : tasks) {
            Map<UUID, Set<UUID>> resolved = task.getResolvedRecipientsByRecipientId();
            if (resolved == null) {
                continue;
            }
            for (Set<UUID> userIds : resolved.values()) {
                if (userIds != null) {
                    recipientIds.addAll(userIds);
                }
            }
        }
        return recipientIds;
    }

    /**
     * Splits recipient ids into locale groups by {@link #localeByRecipient}: each group becomes its own
     * NotifyEvent with the context materialized for the group's locale. Recipients without a locale
     * (or the whole chunk, when it collected no i18n and locales were never loaded) form the single
     * null-locale group.
     */
    public Map<Locale, Set<UUID>> splitRecipientsByLocale(Set<UUID> recipientIds) {
        Map<Locale, Set<UUID>> recipientsByLocale = new LinkedHashMap<>();
        for (UUID recipientId : recipientIds) {
            recipientsByLocale.computeIfAbsent(localeByRecipient.get(recipientId), _ -> new LinkedHashSet<>()).add(recipientId);
        }
        return recipientsByLocale;
    }

    /** Adds an event to its channel's pending batch (phase A accumulator; phase B sends one batch per channel). */
    public void addPendingEvent(NotificationChannelEntity channel, NotifyEvent event) {
        pendingByChannel.computeIfAbsent(channel, _ -> new ArrayList<>()).add(event);
    }
}
