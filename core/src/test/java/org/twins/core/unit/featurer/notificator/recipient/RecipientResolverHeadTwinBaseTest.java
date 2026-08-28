package org.twins.core.featurer.notificator.recipient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

class RecipientResolverHeadTwinBaseTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private RecipientResolverHeadTwinBase resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new RecipientResolverHeadTwinBase();
        injectField(resolver, "twinService", twinService);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try { return clazz.getDeclaredField(fieldName); }
            catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private HistoryEntity buildHistory(TwinEntity twin) {
        var history = new HistoryEntity();
        history.setTwin(twin);
        return history;
    }

    private TwinEntity buildTwinWithHeadTwin() {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        var headTwin = new TwinEntity();
        headTwin.setCreatedByUserId(UUID.randomUUID());
        headTwin.setAssignerUserId(UUID.randomUUID());
        twin.setHeadTwin(headTwin);
        return twin;
    }

    @Nested
    class ResolveHeadTwinCreator {

        @Test
        void resolve_withHeadTwinCreatorEnabled_addsHeadTwinCreator() throws Exception {
            var twin = buildTwinWithHeadTwin();
            var history = buildHistory(twin);
            var recipientIds = new HashSet<UUID>();
            var props = new Properties();
            props.setProperty("resolveHeadTwinCreator", "true");

            resolver.resolve(history, recipientIds, props);

            assertEquals(1, recipientIds.size());
            assertTrue(recipientIds.contains(twin.getHeadTwin().getCreatedByUserId()));
        }

        @Test
        void resolve_withHeadTwinCreatorDisabled_doesNotAdd() throws Exception {
            var twin = buildTwinWithHeadTwin();
            var history = buildHistory(twin);
            var recipientIds = new HashSet<UUID>();
            var props = new Properties();

            resolver.resolve(history, recipientIds, props);

            assertTrue(recipientIds.isEmpty());
        }
    }

    @Nested
    class ResolveHeadTwinAssignee {

        @Test
        void resolve_withHeadTwinAssigneeEnabled_addsHeadTwinAssignee() throws Exception {
            var twin = buildTwinWithHeadTwin();
            var history = buildHistory(twin);
            var recipientIds = new HashSet<UUID>();
            var props = new Properties();
            props.setProperty("resolveHeadTwinAssignee", "true");

            resolver.resolve(history, recipientIds, props);

            assertEquals(1, recipientIds.size());
            assertTrue(recipientIds.contains(twin.getHeadTwin().getAssignerUserId()));
        }
    }

    @Nested
    class ResolveWithoutCachedHeadTwin {

        @Test
        void resolveBatch_headTwinNull_loadedInBulkByHook() throws Exception {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            var headTwin = new TwinEntity();
            headTwin.setCreatedByUserId(UUID.randomUUID());
            var history = buildHistory(twin);
            var batch = new RecipientResolveBatch(UUID.randomUUID()).add(history);
            var props = new Properties();
            props.setProperty("resolveHeadTwinCreator", "true");

            // beforeResolve hook bulk-loads head twins for the whole batch (mocked: single twin)
            doAnswer(invocation -> {
                Collection<TwinEntity> batchTwins = invocation.getArgument(0);
                for (TwinEntity t : batchTwins) {
                    t.setHeadTwin(headTwin);
                }
                return null;
            }).when(twinService).loadHead(List.of(twin));

            resolver.resolveBatch(batch, props);

            var recipientIds = batch.getRecipientIdsByHistory().get(history);
            assertEquals(1, recipientIds.size());
            assertTrue(recipientIds.contains(headTwin.getCreatedByUserId()));
            verify(twinService).loadHead(List.of(twin));
        }

        @Test
        void resolveBatch_headTwinNotLoaded_doesNotAdd() throws Exception {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            var history = buildHistory(twin);
            var batch = new RecipientResolveBatch(UUID.randomUUID()).add(history);
            var props = new Properties();
            props.setProperty("resolveHeadTwinCreator", "true");

            resolver.resolveBatch(batch, props);

            assertTrue(batch.getRecipientIdsByHistory().get(history).isEmpty());
        }
    }

    @Nested
    class ResolveBothFlags {

        @Test
        void resolve_bothEnabled_addsBothCreatorAndAssignee() throws Exception {
            var twin = buildTwinWithHeadTwin();
            var history = buildHistory(twin);
            var recipientIds = new HashSet<UUID>();
            var props = new Properties();
            props.setProperty("resolveHeadTwinCreator", "true");
            props.setProperty("resolveHeadTwinAssignee", "true");

            resolver.resolve(history, recipientIds, props);

            assertEquals(2, recipientIds.size());
            assertTrue(recipientIds.contains(twin.getHeadTwin().getCreatedByUserId()));
            assertTrue(recipientIds.contains(twin.getHeadTwin().getAssignerUserId()));
        }
    }
}
