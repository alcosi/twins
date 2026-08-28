package org.twins.core.featurer.scheduler.tasks;

import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.NotificationChannelEntity;
import org.twins.core.dao.notification.NotificationChannelEventEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.notificator.notifier.Notifier;
import org.twins.core.featurer.notificator.notifier.NotifyEvent;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.notification.*;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Batch Runnable contract of {@link HistoryNotificationTask}: per-entity statuses, one saveAll in
 * finally, partial failure isolation (one task failing does not abort the chunk), ApiUser set per
 * history (locale) + removed, uniqueInBatch dedup across tasks of one history batch.
 * The chunk's configs/recipients/context are pre-wired on entities — the bulk-load/precompute
 * services are mocked as no-ops, so the test exercises the notify loop only.
 */
class HistoryNotificationTaskTest extends BaseUnitTest {

    private static final int NOTIFIER_FEATURER_ID = 7777;

    @Mock
    private HistoryNotificationTaskService historyNotificationTaskService;
    @Mock
    private FeaturerService featurerService;
    @Mock
    private NotificationContextService notificationContextService;
    @Mock
    private NotificationChannelEventService notificationChannelEventService;
    @Mock
    private HistoryNotificationRecipientService historyNotificationRecipientService;
    @Mock
    private HistoryNotificationService historyNotificationService;
    @Mock
    private AuthService authService;
    @Mock
    private Notifier notifier;

    private UUID domainId;

    @BeforeEach
    void setUp() throws Exception {
        domainId = UUID.randomUUID();
    }

    /** Stubbed only in tests that reach the notify loop (strict stubs reject unused ones). */
    private void notifierReady() throws Exception {
        when(featurerService.getFeaturer(eq(NOTIFIER_FEATURER_ID), eq(Notifier.class))).thenReturn(notifier);
        // pass-through: the real materialize copies the template (per-locale substitution is covered
        // by NotificationContextServiceTest.materializeContext_perLocale_...)
        when(notificationContextService.materializeContext(any(), any(), any())).thenAnswer(inv -> new HashMap<>(inv.getArgument(0)));
    }

    // ---------- wiring ----------

    private HistoryNotificationTask task(HistoryNotificationChunk chunk) throws Exception {
        var task = new HistoryNotificationTask(chunk);
        inject(task, "historyNotificationTaskService", historyNotificationTaskService);
        inject(task, "featurerService", featurerService);
        inject(task, "notificationContextService", notificationContextService);
        inject(task, "notificationChannelEventService", notificationChannelEventService);
        inject(task, "historyNotificationRecipientService", historyNotificationRecipientService);
        inject(task, "historyNotificationService", historyNotificationService);
        inject(task, "authService", authService);
        return task;
    }

    private void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    // ---------- test data ----------

    private TwinClassEntity twinClass() {
        var twinClass = new TwinClassEntity();
        twinClass.setId(UUID.randomUUID());
        twinClass.setDomainId(domainId);
        return twinClass;
    }

    private TwinEntity twin(TwinClassEntity twinClass, UUID ownerBa, UUID createdBy) {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        twin.setTwinClass(twinClass);
        twin.setOwnerBusinessAccountId(ownerBa);
        twin.setCreatedByUserId(createdBy);
        return twin;
    }

    private HistoryEntity history(TwinEntity twin, UUID historyBatchId) {
        var history = new HistoryEntity();
        history.setTwinId(twin.getId());
        history.setTwin(twin);
        // LoggerUtils.logSession(UUID) NPEs on null — in production the DB trigger always sets the batch id
        history.setHistoryBatchId(historyBatchId != null ? historyBatchId : UUID.randomUUID());
        return history;
    }

    private HistoryNotificationTaskEntity taskEntity(HistoryEntity history) {
        return new HistoryNotificationTaskEntity()
                .setId(UUID.randomUUID())
                .setHistory(history);
    }

    private NotificationChannelEventEntity channelEvent(String eventCode, UUID contextId, boolean uniqueInBatch) {
        var channel = new NotificationChannelEntity();
        channel.setNotifierFeaturerId(NOTIFIER_FEATURER_ID);
        channel.setNotifierParams(new HashMap<>());
        var event = new NotificationChannelEventEntity();
        event.setId(UUID.randomUUID());
        event.setEventCode(eventCode);
        event.setNotificationContextId(contextId);
        event.setUniqueInBatch(uniqueInBatch);
        event.setNotificationChannel(channel);
        return event;
    }

    private HistoryNotificationEntity config(NotificationChannelEventEntity event, UUID recipientId) {
        var config = new HistoryNotificationEntity();
        config.setId(UUID.randomUUID());
        config.setNotificationChannelEventId(event.getId());
        config.setNotificationChannelEvent(event);
        config.setHistoryNotificationRecipientId(recipientId);
        return config;
    }

    private void wire(HistoryNotificationChunk chunk, HistoryNotificationTaskEntity taskEntity, HistoryNotificationEntity config) {
        chunk.getTasksByConfig().put(config, new LinkedHashSet<>(List.of(taskEntity)));
        chunk.getConfigsByTask().computeIfAbsent(taskEntity, _ -> new ArrayList<>()).add(config);
    }

    // ---------- scenarios ----------

    @Nested
    class NotifyLoop {

        @Test
        void noConfigs_skipped_saveAllOnce() throws Exception {
            var taskEntity = taskEntity(history(twin(twinClass(), null, null), null));
            var chunk = new HistoryNotificationChunk(domainId, List.of(taskEntity));

            task(chunk).run();

            assertEquals(HistoryNotificationTaskStatus.SKIPPED, taskEntity.getStatusId());
            assertTrue(taskEntity.getStatusDetails().contains("No configs found"));
            verify(historyNotificationTaskService, times(1)).updateStatuses(chunk.getTasks());
            verifyNoInteractions(notifier);
            // chunk-wide ApiUser is the system NOTIFICATION_SCHEDULER user (global DOMAIN_TWINS_VIEW_ALL
            // grant — secure bulk loads pass the permission branch); per-history re-set follows for the
            // locale — here ownerBa/creator are null, so it lands on (domainId, null, null)
            verify(authService, atLeastOnce()).setThreadLocalApiUser(domainId, null, SystemIds.User.NOTIFICATION_SCHEDULER);
            verify(authService, atLeastOnce()).removeThreadLocalApiUser();
        }

        @Test
        void recipientsAndContext_notifiedAndSent_apiUserPerHistory() throws Exception {
            var ownerBa = UUID.randomUUID();
            var creator = UUID.randomUUID();
            var recipientId = UUID.randomUUID();
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();
            var contextId = UUID.randomUUID();
            var event = channelEvent("TWIN_CREATED", contextId, false);
            var config = config(event, recipientId);
            var taskEntity = taskEntity(history(twin(twinClass(), ownerBa, creator), null));
            taskEntity.setResolvedRecipientsByRecipientId(Map.of(recipientId, Set.of(userId1, userId2)));
            taskEntity.setCollectedContextByContextId(Map.of(contextId, Map.of("TWIN_NAME", "n")));
            var chunk = new HistoryNotificationChunk(domainId, List.of(taskEntity));
            wire(chunk, taskEntity, config);
            notifierReady();

            task(chunk).run();

            assertEquals(HistoryNotificationTaskStatus.SENT, taskEntity.getStatusId());
            assertTrue(taskEntity.getStatusDetails().contains("2 recipients were notified"));
            // doneAt is DB-owned (trigger on the SENT transition) — never set by the application
            assertNull(taskEntity.getDoneAt());
            // per-history ApiUser: locale source = twin creator
            verify(authService).setThreadLocalApiUser(domainId, ownerBa, creator);
            // one batched notify per channel: single event carries recipients + context + event code
            verify(notifier).notify(any(), argThat(events -> events.size() == 1
                    && events.iterator().next().recipientIds().equals(Set.of(userId1, userId2))
                    && events.iterator().next().context().equals(Map.of("TWIN_NAME", "n"))
                    && events.iterator().next().eventCode().equals("TWIN_CREATED")));
            verify(historyNotificationTaskService, times(1)).updateStatuses(chunk.getTasks());
        }

        @Test
        void recipientsSplitByLocale_perLocaleContextAndEvent() throws Exception {
            // the bug fix (TWINS-836): context must be translated in the RECIPIENT's locale, not the
            // twin creator's — one NotifyEvent per (task, channel, locale group of recipients)
            var recipientId = UUID.randomUUID();
            var userEn = UUID.randomUUID();
            var userDe = UUID.randomUUID();
            var userNoLocale = UUID.randomUUID();
            var i18nId = UUID.randomUUID();
            var ctxId = UUID.randomUUID();
            var event = channelEvent("TWIN_CREATED", ctxId, false);
            var config = config(event, recipientId);
            var taskEntity = taskEntity(history(twin(twinClass(), null, null), null));
            taskEntity.setResolvedRecipientsByRecipientId(Map.of(recipientId, Set.of(userEn, userDe, userNoLocale)));
            taskEntity.setCollectedContextByContextId(Map.of(ctxId, Map.of("TWIN_CLASS_NAME", "#i18n=" + i18nId)));
            var chunk = new HistoryNotificationChunk(domainId, List.of(taskEntity));
            wire(chunk, taskEntity, config);
            chunk.getLocaleByRecipient().put(userEn, Locale.ENGLISH);
            chunk.getLocaleByRecipient().put(userDe, Locale.GERMAN); // userNoLocale deliberately absent -> null-locale group
            chunk.getI18nTranslationsByLocale().put(Locale.ENGLISH, Map.of(i18nId, "Name-EN"));
            chunk.getI18nTranslationsByLocale().put(Locale.GERMAN, Map.of(i18nId, "Name-DE"));
            notifierReady();

            task(chunk).run();

            assertEquals(HistoryNotificationTaskStatus.SENT, taskEntity.getStatusId());
            assertTrue(taskEntity.getStatusDetails().contains("3 recipients were notified"));
            // three locale groups -> three events in ONE channel batch, each with its own recipient set
            // (per-locale context values are covered by NotificationContextServiceTest)
            verify(notifier).notify(any(), argThat(events -> events.size() == 3
                    && events.stream().anyMatch(e -> e.recipientIds().equals(Set.of(userEn)))
                    && events.stream().anyMatch(e -> e.recipientIds().equals(Set.of(userDe)))
                    && events.stream().anyMatch(e -> e.recipientIds().equals(Set.of(userNoLocale)))));
        }

        @Test
        void twoTasksOfDifferentBusinessAccounts_sameChannel_eventsStayIsolated() throws Exception {
            // BA isolation contract (NotifyEvent): one chunk = one domain but many business accounts,
            // and one notify() batch carries their events together — each event must keep its OWN
            // recipient set and its OWN context, so nothing crosses business accounts
            var ba1 = UUID.randomUUID();
            var ba2 = UUID.randomUUID();
            var recipientId = UUID.randomUUID(); // same recipient config matched by both tasks
            var ba1User = UUID.randomUUID();
            var ba2User = UUID.randomUUID();
            var ctxId = UUID.randomUUID();
            var event = channelEvent("TWIN_CREATED", ctxId, false);
            var config = config(event, recipientId);
            var taskBa1 = taskEntity(history(twin(twinClass(), ba1, null), null));
            var taskBa2 = taskEntity(history(twin(twinClass(), ba2, null), null));
            taskBa1.setResolvedRecipientsByRecipientId(Map.of(recipientId, Set.of(ba1User)));
            taskBa2.setResolvedRecipientsByRecipientId(Map.of(recipientId, Set.of(ba2User)));
            taskBa1.setCollectedContextByContextId(Map.of(ctxId, Map.of("TWIN_NAME", "ba1-secret")));
            taskBa2.setCollectedContextByContextId(Map.of(ctxId, Map.of("TWIN_NAME", "ba2-secret")));
            var chunk = new HistoryNotificationChunk(domainId, List.of(taskBa1, taskBa2));
            wire(chunk, taskBa1, config);
            wire(chunk, taskBa2, config);
            notifierReady();

            task(chunk).run();

            assertEquals(HistoryNotificationTaskStatus.SENT, taskBa1.getStatusId());
            assertEquals(HistoryNotificationTaskStatus.SENT, taskBa2.getStatusId());
            // ONE channel batch of TWO events: disjoint recipient sets, each context only its own task's
            verify(notifier).notify(any(), argThat(events -> events.size() == 2
                    && events.stream().anyMatch(e -> e.recipientIds().equals(Set.of(ba1User))
                            && "ba1-secret".equals(e.context().get("TWIN_NAME"))
                            && !e.context().containsValue("ba2-secret"))
                    && events.stream().anyMatch(e -> e.recipientIds().equals(Set.of(ba2User))
                            && "ba2-secret".equals(e.context().get("TWIN_NAME"))
                            && !e.context().containsValue("ba1-secret"))));
        }

        @Test
        void noRecipients_skipped() throws Exception {
            var event = channelEvent("TWIN_CREATED", null, false);
            var config = config(event, UUID.randomUUID());
            var taskEntity = taskEntity(history(twin(twinClass(), null, null), null));
            taskEntity.setResolvedRecipientsByRecipientId(Map.of(config.getHistoryNotificationRecipientId(), Set.of()));
            var chunk = new HistoryNotificationChunk(domainId, List.of(taskEntity));
            wire(chunk, taskEntity, config);

            task(chunk).run();

            assertEquals(HistoryNotificationTaskStatus.SKIPPED, taskEntity.getStatusId());
            assertTrue(taskEntity.getStatusDetails().contains("No recipients"));
            verifyNoInteractions(notifier);
        }

        @Test
        void uniqueInBatch_secondTaskOfSameBatchSkippedForSameEvent() throws Exception {
            // both tasks share the history batch and the unique-in-batch event → only the first is notified
            var historyBatchId = UUID.randomUUID(); // static cache key — unique per test run
            var recipientId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var event = channelEvent("TWIN_CREATED", null, true);
            var config = config(event, recipientId);
            var task1 = taskEntity(history(twin(twinClass(), null, null), historyBatchId));
            var task2 = taskEntity(history(twin(twinClass(), null, null), historyBatchId));
            for (var t : List.of(task1, task2)) {
                t.setResolvedRecipientsByRecipientId(Map.of(recipientId, Set.of(userId)));
            }
            var chunk = new HistoryNotificationChunk(domainId, List.of(task1, task2));
            chunk.getTasksByConfig().put(config, new LinkedHashSet<>(List.of(task1, task2)));
            chunk.getConfigsByTask().put(task1, List.of(config));
            chunk.getConfigsByTask().put(task2, List.of(config));
            notifierReady();

            task(chunk).run();

            verify(notifier, times(1)).notify(any(), argThat(events ->
                    events.size() == 1 && events.iterator().next().eventCode().equals("TWIN_CREATED")));
            assertEquals(HistoryNotificationTaskStatus.SENT, task1.getStatusId());
            assertEquals(HistoryNotificationTaskStatus.SKIPPED, task2.getStatusId());
        }
    }

    @Nested
    class FailureIsolation {

        @Test
        void notifyFailsForFirstTask_secondStillProcessed() throws Exception {
            // distinct recipients per task → distinct events in the shared channel batch; the notifier
            // reports the failed EVENT back, the worker attributes it to the contributing task only
            var recipientId = UUID.randomUUID();
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();
            var event = channelEvent("TWIN_CREATED", null, false);
            var config1 = config(event, recipientId);
            var config2 = config(event, recipientId);
            var task1 = taskEntity(history(twin(twinClass(), null, null), null));
            var task2 = taskEntity(history(twin(twinClass(), null, null), null));
            task1.setResolvedRecipientsByRecipientId(Map.of(recipientId, Set.of(userId1)));
            task2.setResolvedRecipientsByRecipientId(Map.of(recipientId, Set.of(userId2)));
            var chunk = new HistoryNotificationChunk(domainId, List.of(task1, task2));
            wire(chunk, task1, config1);
            wire(chunk, task2, config2);
            notifierReady();
            when(notifier.notify(any(), argThat(events -> events != null && events.size() == 2)))
                    .thenAnswer(invocation -> {
                        Set<NotifyEvent> events = invocation.getArgument(1);
                        return events.stream()
                                .filter(e -> e.recipientIds().contains(userId1))
                                .collect(java.util.stream.Collectors.toSet());
                    });

            task(chunk).run();

            assertEquals(HistoryNotificationTaskStatus.FAILED, task1.getStatusId());
            assertTrue(task1.getStatusDetails().contains("Notify failed"));
            assertEquals(HistoryNotificationTaskStatus.SENT, task2.getStatusId());
            // ONE batched notify for the whole channel, both statuses persisted with ONE bulk update
            verify(notifier, times(1)).notify(any(), any());
            verify(historyNotificationTaskService, times(1)).updateStatuses(chunk.getTasks());
        }

        @Test
        void bulkLoadThrows_revertsUnprocessedTasksToNeedStartForRetry() throws Exception {
            var event = channelEvent("TWIN_CREATED", null, false);
            var config = config(event, UUID.randomUUID());
            var task1 = taskEntity(history(twin(twinClass(), null, null), null));
            var task2 = taskEntity(history(twin(twinClass(), null, null), null));
            var chunk = new HistoryNotificationChunk(domainId, List.of(task1, task2));
            wire(chunk, task1, config);
            wire(chunk, task2, config);
            doThrow(new RuntimeException("db down"))
                    .when(historyNotificationService)
                    .loadNotificationChannelEvent(org.mockito.ArgumentMatchers.<java.util.Collection<HistoryNotificationEntity>>any());

            task(chunk).run();

            // infra failure of the bulk stage is not a business error of these tasks — retry, not terminal FAILED
            assertEquals(HistoryNotificationTaskStatus.NEED_START, task1.getStatusId());
            assertEquals(HistoryNotificationTaskStatus.NEED_START, task2.getStatusId());
            assertEquals(1, task1.getAttemptCount());
            assertTrue(task1.getStatusDetails().contains("attempt 1 of 3"));
            assertTrue(task1.getStatusDetails().contains("db down"));
            assertNull(task1.getDoneAt());
            verify(historyNotificationTaskService, times(1)).updateStatuses(chunk.getTasks());
            verify(authService).removeThreadLocalApiUser();
            verifyNoInteractions(notifier);
        }

        @Test
        void bulkLoadThrows_afterMaxBatchAttempts_escalatesToFailed() throws Exception {
            var event = channelEvent("TWIN_CREATED", null, false);
            var config = config(event, UUID.randomUUID());
            var task1 = taskEntity(history(twin(twinClass(), null, null), null));
            // two batch attempts already recorded by previous runs (attempt_count column)
            task1.setStatusId(HistoryNotificationTaskStatus.IN_PROGRESS);
            task1.setAttemptCount(2);
            var chunk = new HistoryNotificationChunk(domainId, List.of(task1));
            wire(chunk, task1, config);
            doThrow(new RuntimeException("db down"))
                    .when(historyNotificationService)
                    .loadNotificationChannelEvent(org.mockito.ArgumentMatchers.<java.util.Collection<HistoryNotificationEntity>>any());

            task(chunk).run();

            // third consecutive batch failure — poison-pill protection kicks in
            assertEquals(HistoryNotificationTaskStatus.FAILED, task1.getStatusId());
            assertEquals(3, task1.getAttemptCount());
            assertTrue(task1.getStatusDetails().contains("after 3 attempts"));
            verify(historyNotificationTaskService, times(1)).updateStatuses(chunk.getTasks());
        }

        @Test
        void twinOutOfDomain_taskSkipped() throws Exception {
            var twinClass = twinClass();
            twinClass.setDomainId(null); // out of domain — processTask guard must skip without notify
            var event = channelEvent("TWIN_CREATED", null, false);
            var config = config(event, UUID.randomUUID());
            var taskEntity = taskEntity(history(twin(twinClass, null, null), null));
            taskEntity.setResolvedRecipientsByRecipientId(Map.of(config.getHistoryNotificationRecipientId(), Set.of(UUID.randomUUID())));
            var chunk = new HistoryNotificationChunk(domainId, List.of(taskEntity));
            wire(chunk, taskEntity, config);

            task(chunk).run();

            assertEquals(HistoryNotificationTaskStatus.SKIPPED, taskEntity.getStatusId());
            assertEquals("Twin is out of domain", taskEntity.getStatusDetails());
            verifyNoInteractions(notifier);
        }
    }
}
