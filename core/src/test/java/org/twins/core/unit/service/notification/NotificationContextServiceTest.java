package org.twins.core.unit.service.notification;

import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.notificator.context.ContextCollector;
import org.twins.core.featurer.notificator.context.ContextCollectorBatch;
import org.twins.core.service.domain.DomainUserService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.notification.HistoryNotificationChunk;
import org.twins.core.service.notification.NotificationChannelEventService;
import org.twins.core.service.notification.NotificationContextCollectorService;
import org.twins.core.service.notification.NotificationContextService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Chunk-level context precompute over OVERLAPPING data: contexts sharing collector configs,
 * one task matched by several configs with different channel events / contexts, per-history
 * locale i18n. Verifies chunk-global grouping (one collectDataBatch per unique
 * (featurerId, params)), per-context merge and per-locale i18n substitution.
 */
class NotificationContextServiceTest extends BaseUnitTest {

    private static final int COLLECTOR_FEATURER_ID = FeaturerTwins.ID_4904;

    @Mock
    private FeaturerService featurerService;
    @Mock
    private NotificationContextRepository notificationContextRepository;
    @Mock
    private NotificationContextCollectorService notificationContextCollectorService;
    @Mock
    private I18nService i18nService;
    @Mock
    private DomainUserService domainUserService;
    @Mock
    private NotificationChannelEventService notificationChannelEventService;
    @Mock
    private ContextCollector collector;

    private NotificationContextService service;
    private UUID domainId;

    /** Counts collectDataBatch invocations per params marker (collectKey) across the chunk. */
    private final Map<String, Integer> collectorRunsByParam = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        service = new NotificationContextService(
                featurerService, notificationContextRepository, notificationContextCollectorService,
                i18nService, domainUserService, notificationChannelEventService);
        domainId = UUID.randomUUID();
        when(featurerService.getFeaturer(eq(COLLECTOR_FEATURER_ID), eq(ContextCollector.class))).thenReturn(collector);
        collectorRunsByParam.clear();
    }

    // ---------- test data builders ----------

    private TwinEntity twin(UUID createdByUserId) {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        twin.setOwnerBusinessAccountId(UUID.randomUUID());
        twin.setCreatedByUserId(createdByUserId);
        return twin;
    }

    private HistoryEntity history(TwinEntity twin) {
        var history = new HistoryEntity();
        history.setTwinId(twin.getId());
        history.setTwin(twin);
        return history;
    }

    private HistoryNotificationTaskEntity task(HistoryEntity history) {
        // id matters: entities are used as map keys and @Data equals collapses id-less instances
        return new HistoryNotificationTaskEntity()
                .setId(UUID.randomUUID())
                .setHistory(history);
    }

    /** Collector identified by a "collectKey" param; the mocked collector puts value:<key> into the context. */
    private NotificationContextCollectorEntity ctxCollector(String collectKey) {
        var collector = new NotificationContextCollectorEntity();
        collector.setId(UUID.randomUUID());
        collector.setContextCollectorFeaturerId(COLLECTOR_FEATURER_ID);
        collector.setContextCollectorParams(new HashMap<>(Map.of("collectKey", collectKey)));
        return collector;
    }

    private NotificationChannelEventEntity channelEvent(UUID contextId, NotificationContextCollectorEntity... collectors) {
        var event = new NotificationChannelEventEntity();
        event.setId(UUID.randomUUID());
        event.setNotificationContextId(contextId);
        event.setCollectors(new Kit<>(NotificationContextCollectorEntity::getId));
        for (var collector : collectors) {
            event.getCollectors().add(collector);
        }
        return event;
    }

    private HistoryNotificationEntity config(NotificationChannelEventEntity event) {
        var config = new HistoryNotificationEntity();
        config.setNotificationChannelEventId(event.getId());
        config.setNotificationChannelEvent(event);
        return config;
    }

    private void wire(HistoryNotificationChunk chunk, HistoryNotificationEntity config, HistoryNotificationTaskEntity... tasks) {
        chunk.getTasksByConfig().put(config, new LinkedHashSet<>(List.of(tasks)));
        for (HistoryNotificationTaskEntity task : tasks) {
            chunk.getConfigsByTask().computeIfAbsent(task, _ -> new ArrayList<>()).add(config);
        }
    }

    private void collectorWritesByKey() throws Exception {
        doAnswer(invocation -> {
            ContextCollectorBatch batch = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            HashMap<String, String> params = invocation.getArgument(1);
            String key = params.get("collectKey");
            collectorRunsByParam.merge(key, 1, Integer::sum);
            for (var entry : batch.getContextByHistory().entrySet()) {
                entry.getValue().put(key, "value:" + key);
            }
            return null;
        }).when(collector).collectDataBatch(any(ContextCollectorBatch.class), any(HashMap.class));
    }

    private HistoryNotificationChunk chunkOf(HistoryNotificationTaskEntity... tasks) {
        return new HistoryNotificationChunk(domainId, List.of(tasks));
    }

    // ---------- scenarios ----------

    @Nested
    class OverlappingContexts {

        @Test
        void contextsSharingCollector_groupRunsOnce_mergedIntoBoth() throws Exception {
            // manager context (ctxA) and worker context (ctxB) share the COMMON collector config;
            // ctxB additionally has EXTRA. Task3 is matched by BOTH configs → needs both contexts.
            var ctxA = UUID.randomUUID();
            var ctxB = UUID.randomUUID();
            var eventA = channelEvent(ctxA, ctxCollector("COMMON"));
            var eventB = channelEvent(ctxB, ctxCollector("COMMON"), ctxCollector("EXTRA"));
            var configA = config(eventA);
            var configB = config(eventB);
            var taskA = task(history(twin(null)));
            var taskB = task(history(twin(null)));
            var taskBoth = task(history(twin(null)));
            var chunk = chunkOf(taskA, taskB, taskBoth);
            wire(chunk, configA, taskA, taskBoth);
            wire(chunk, configB, taskB, taskBoth);
            collectorWritesByKey();

            service.collectHistoryContextBatch(chunk);

            // COMMON ran ONCE for the whole chunk despite living in two contexts; EXTRA once
            assertEquals(1, collectorRunsByParam.get("COMMON"));
            assertEquals(1, collectorRunsByParam.get("EXTRA"));
            verify(collector, times(2)).collectDataBatch(any(ContextCollectorBatch.class), any(HashMap.class));

            // taskA: only ctxA, only COMMON's contribution
            assertEquals(Map.of("COMMON", "value:COMMON"), taskA.getCollectedContextByContextId().get(ctxA));
            assertNull(taskA.getCollectedContextByContextId().get(ctxB));

            // taskB: only ctxB, union of COMMON + EXTRA
            assertEquals(Map.of("COMMON", "value:COMMON", "EXTRA", "value:EXTRA"),
                    taskB.getCollectedContextByContextId().get(ctxB));
            assertNull(taskB.getCollectedContextByContextId().get(ctxA));

            // taskBoth: both contexts, each merged from its own groups
            assertEquals(Map.of("COMMON", "value:COMMON"), taskBoth.getCollectedContextByContextId().get(ctxA));
            assertEquals(Map.of("COMMON", "value:COMMON", "EXTRA", "value:EXTRA"),
                    taskBoth.getCollectedContextByContextId().get(ctxB));
        }

        @Test
        void severalTasksOneContext_oneRunPerGroup_allTasksFilled() throws Exception {
            var ctxId = UUID.randomUUID();
            var event = channelEvent(ctxId, ctxCollector("TWIN"));
            var configTwin = config(event);
            var task1 = task(history(twin(null)));
            var task2 = task(history(twin(null)));
            var chunk = chunkOf(task1, task2);
            wire(chunk, configTwin, task1, task2);
            collectorWritesByKey();

            service.collectHistoryContextBatch(chunk);

            assertEquals(1, collectorRunsByParam.get("TWIN"));
            assertEquals(Map.of("TWIN", "value:TWIN"), task1.getCollectedContextByContextId().get(ctxId));
            assertEquals(Map.of("TWIN", "value:TWIN"), task2.getCollectedContextByContextId().get(ctxId));
        }
    }

    @Nested
    class I18nPerLocale {

        @Test
        void sharedI18nId_resolvedPerHistoryCreatorLocale() throws Exception {
            // one collector group over two histories; the same i18n id must resolve to DIFFERENT
            // translations per history — each history's locale is its twin creator's locale
            var ctxId = UUID.randomUUID();
            var event = channelEvent(ctxId, ctxCollector("TWIN_CLASS"));
            var configCtx = config(event);
            var userEn = UUID.randomUUID();
            var userDe = UUID.randomUUID();
            var taskEn = task(history(twin(userEn)));
            var taskDe = task(history(twin(userDe)));
            var chunk = chunkOf(taskEn, taskDe);
            wire(chunk, configCtx, taskEn, taskDe);
            var i18nId = UUID.randomUUID();
            doAnswer(invocation -> {
                ContextCollectorBatch batch = invocation.getArgument(0);
                for (var historyEntry : batch.getContextByHistory().entrySet()) {
                    batch.addI18n(historyEntry.getKey(), "TWIN_CLASS_NAME", i18nId);
                }
                return null;
            }).when(collector).collectDataBatch(any(ContextCollectorBatch.class), any(HashMap.class));
            when(domainUserService.getLocaleMap(eq(domainId), any())).thenReturn(Map.of(
                    userEn, Locale.ENGLISH,
                    userDe, Locale.GERMAN));
            when(i18nService.translateToLocale(eq(Set.of(i18nId)), eq(Locale.ENGLISH)))
                    .thenReturn(Map.of(i18nId, "Name-EN"));
            when(i18nService.translateToLocale(eq(Set.of(i18nId)), eq(Locale.GERMAN)))
                    .thenReturn(Map.of(i18nId, "Name-DE"));

            service.collectHistoryContextBatch(chunk);

            // one bulk translate per locale — shared across the whole chunk
            verify(i18nService, times(1)).translateToLocale(eq(Set.of(i18nId)), eq(Locale.ENGLISH));
            verify(i18nService, times(1)).translateToLocale(eq(Set.of(i18nId)), eq(Locale.GERMAN));
            assertEquals("Name-EN", taskEn.getCollectedContextByContextId().get(ctxId).get("TWIN_CLASS_NAME"));
            assertEquals("Name-DE", taskDe.getCollectedContextByContextId().get(ctxId).get("TWIN_CLASS_NAME"));
        }
    }
}
