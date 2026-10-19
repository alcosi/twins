package org.twins.core.featurer.notificator.notifier;

import org.twins.core.dao.notification.HistoryNotificationTaskEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * One notification to send: the originating task, the resolved recipient ids, the collected context
 * and the channel event code. Notifiers receive the whole event set of their channel per chunk —
 * see {@link Notifier#notify}. The task reference is the event's source: the worker uses it for
 * failure attribution (failed events → their tasks), notifiers may read task data they need over time.
 * <p><b>Isolation contract:</b> a notifier MUST deliver {@link #context} only to {@link #recipientIds}
 * of the SAME event. One notify batch mixes events of several business accounts of the domain —
 * merging recipient sets or reusing one event's context for another's recipients leaks notifications
 * across business accounts.
 */
public record NotifyEvent(HistoryNotificationTaskEntity task, Set<UUID> recipientIds, Map<String, String> context, String eventCode) {
}
