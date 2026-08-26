package org.twins.core.featurer.scheduler.tasks;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.notificator.notifier.Notifier;
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
    private HistoryNotificationTaskRepository historyNotificationTaskRepository;
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
    }

    // ---------- wiring ----------

    private HistoryNotificationTask task(HistoryNotificationChunk chunk) throws Exception {
        var task = new HistoryNotificationTask(chunk);
        inject(task, "historyNotificationTaskRepository", historyNotificationTaskRepository);
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
            verify(historyNotificationTaskRepository, times(1)).saveAll(chunk.getTasks());
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
            assertNotNull(taskEntity.getDoneAt());
            // per-history ApiUser: locale source = twin creator
            verify(authService).setThreadLocalApiUser(domainId, ownerBa, creator);
            verify(notifier).notify(eq(Set.of(userId1, userId2)), eq(Map.of("TWIN_NAME", "n")), eq("TWIN_CREATED"), any());
            verify(historyNotificationTaskRepository, times(1)).saveAll(chunk.getTasks());
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

            verify(notifier, times(1)).notify(any(), any(), eq("TWIN_CREATED"), any());
            assertEquals(HistoryNotificationTaskStatus.SENT, task1.getStatusId());
            assertEquals(HistoryNotificationTaskStatus.SKIPPED, task2.getStatusId());
        }
    }

    @Nested
    class FailureIsolation {

        @Test
        void notifyFailsForFirstTask_secondStillProcessed() throws Exception {
            var recipientId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var event = channelEvent("TWIN_CREATED", null, false);
            var config1 = config(event, recipientId);
            var config2 = config(event, recipientId);
            var task1 = taskEntity(history(twin(twinClass(), null, null), null));
            var task2 = taskEntity(history(twin(twinClass(), null, null), null));
            for (var t : List.of(task1, task2)) {
                t.setResolvedRecipientsByRecipientId(Map.of(recipientId, Set.of(userId)));
            }
            var chunk = new HistoryNotificationChunk(domainId, List.of(task1, task2));
            wire(chunk, task1, config1);
            wire(chunk, task2, config2);
            notifierReady();
            doThrow(new ServiceException(ErrorCodeCommon.FEATURER_ID_UNKNOWN, "grpc down"))
                    .doNothing() // second call succeeds
                    .when(notifier).notify(any(), any(), eq("TWIN_CREATED"), any());

            task(chunk).run();

            assertEquals(HistoryNotificationTaskStatus.FAILED, task1.getStatusId());
            assertEquals(HistoryNotificationTaskStatus.SENT, task2.getStatusId());
            verify(notifier, times(2)).notify(any(), any(), eq("TWIN_CREATED"), any());
            // both statuses persisted with ONE saveAll of the whole chunk
            verify(historyNotificationTaskRepository, times(1)).saveAll(chunk.getTasks());
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
            verify(historyNotificationTaskRepository, times(1)).saveAll(chunk.getTasks());
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
            assertNotNull(task1.getDoneAt());
            verify(historyNotificationTaskRepository, times(1)).saveAll(chunk.getTasks());
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
