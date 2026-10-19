package org.twins.core.featurer.notificator.recipient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.user.UserService;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class RecipientResolverUsersBaseTest extends BaseUnitTest {

    @Mock
    private UserService userService;

    private RecipientResolverUsersBase resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new RecipientResolverUsersBase();
        injectField(resolver, "userService", userService);
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

    private HistoryEntity buildHistory(UUID businessAccountId) {
        var twin = new TwinEntity();
        twin.setOwnerBusinessAccountId(businessAccountId);
        var history = new HistoryEntity();
        history.setTwin(twin);
        return history;
    }

    @Nested
    class ResolveBatch {

        @Test
        void resolveBatch_filtersUsersByBatchDomainAndAddsToRecipients() throws Exception {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();
            var history = buildHistory(businessAccountId);
            var batch = new RecipientResolveBatch(domainId).add(history);
            var props = new Properties();
            props.setProperty("userIds", userId1 + "," + userId2);

            when(userService.filterUsersByBusinessAccountAndDomainIn(
                    Set.of(userId1, userId2), Set.of(businessAccountId), domainId))
                    .thenReturn(Map.of(businessAccountId, Set.of(userId1)));

            resolver.resolveBatch(batch, props);

            var recipientIds = batch.getRecipientIdsByHistory().get(history);
            assertEquals(1, recipientIds.size());
            assertTrue(recipientIds.contains(userId1));
        }

        @Test
        void resolveBatch_appendsToExistingRecipients() throws Exception {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var existingUserId = UUID.randomUUID();
            var history = buildHistory(businessAccountId);
            var batch = new RecipientResolveBatch(domainId).add(history);
            batch.getRecipientIdsByHistory().get(history).add(existingUserId);
            var props = new Properties();
            props.setProperty("userIds", userId.toString());

            when(userService.filterUsersByBusinessAccountAndDomainIn(
                    Set.of(userId), Set.of(businessAccountId), domainId))
                    .thenReturn(Map.of(businessAccountId, Set.of(userId)));

            resolver.resolveBatch(batch, props);

            var recipientIds = batch.getRecipientIdsByHistory().get(history);
            assertEquals(2, recipientIds.size());
            assertTrue(recipientIds.contains(existingUserId));
            assertTrue(recipientIds.contains(userId));
        }
    }
}
