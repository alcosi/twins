package org.twins.core.featurer.notificator.notifier;

import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Batch loop contract of {@link NotifierAtomic}: every event is attempted, a failing event does not
 * abort the rest, failed events are returned for per-task attribution by the caller.
 */
class NotifierAtomicTest extends BaseUnitTest {

    static class FakeAtomicNotifier extends NotifierAtomic {
        int attempted = 0;
        final UUID failingRecipient;

        FakeAtomicNotifier(UUID failingRecipient) {
            this.failingRecipient = failingRecipient;
        }

        @Override
        protected void notify(Set<UUID> recipientIds, Map<String, String> context, String eventCode, Properties properties) {
            attempted++;
            if (recipientIds.contains(failingRecipient)) {
                throw new IllegalStateException("grpc down");
            }
        }
    }

    @Test
    void failingEventDoesNotAbortBatch_andIsReturnedAsFailed() throws Exception {
        var failingRecipient = UUID.randomUUID();
        var notifier = new FakeAtomicNotifier(failingRecipient);
        var event1 = new NotifyEvent(null, Set.of(UUID.randomUUID()), Map.of(), "E1");
        var event2 = new NotifyEvent(null, Set.of(failingRecipient), Map.of(), "E2");
        var event3 = new NotifyEvent(null, Set.of(UUID.randomUUID()), Map.of(), "E3");

        var failed = notifier.notify(new Properties(), new LinkedHashSet<>(List.of(event1, event2, event3)));

        assertEquals(3, notifier.attempted);          // loop continued past the failure
        assertEquals(Set.of(event2), failed);         // exactly the failed event is reported back
    }

    @Test
    void allEventsSent_emptyFailedSet() throws Exception {
        var notifier = new FakeAtomicNotifier(UUID.randomUUID());
        var events = new LinkedHashSet<>(List.of(
                new NotifyEvent(null, Set.of(UUID.randomUUID()), Map.of(), "E1"),
                new NotifyEvent(null, Set.of(UUID.randomUUID()), Map.of(), "E2")));

        var failed = notifier.notify(new Properties(), events);

        assertEquals(Set.of(), failed);
        assertEquals(2, notifier.attempted);
    }
}
