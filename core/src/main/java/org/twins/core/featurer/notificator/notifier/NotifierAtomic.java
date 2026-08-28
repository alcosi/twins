package org.twins.core.featurer.notificator.notifier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;

import java.util.*;

/**
 * Per-event {@link Notifier}: the batch is a sequential loop over
 * {@link #notify(Set, Map, String, Properties)}. A failing event does not abort the rest of the
 * batch — failed events are collected and returned, so the caller attributes the failure per task.
 * Concrete notifiers that cannot send natively in batch extend this class (mirrors
 * {@code RecipientResolverAtomic} / {@code ContextCollectorAtomic}).
 */
@Slf4j
public abstract class NotifierAtomic extends Notifier {

    @Override
    protected final Set<NotifyEvent> notify(Properties properties, Set<NotifyEvent> notifyEvents) throws ServiceException {
        Set<NotifyEvent> failedEvents = new LinkedHashSet<>();
        for (NotifyEvent notifyEvent : notifyEvents) {
            try {
                notify(notifyEvent.recipientIds(), notifyEvent.context(), notifyEvent.eventCode(), properties);
            } catch (Exception e) {
                log.error("Notify event[{}] for {} recipient(s) failed", notifyEvent.eventCode(), notifyEvent.recipientIds().size(), e);
                failedEvents.add(notifyEvent);
            }
        }
        return failedEvents;
    }

    /**
     * Single-event convenience entry (kept for tests / non-batch callers).
     */
    protected abstract void notify(Set<UUID> recipientIds, Map<String, String> context, String eventCode, Properties properties) throws ServiceException;
}
