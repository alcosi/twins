package org.twins.core.featurer.notificator.recipient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.businessaccount.BusinessAccountUserService;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RecipientResolverBusinessAccountTest extends BaseUnitTest {

    @Mock
    private BusinessAccountUserService businessAccountUserService;

    private RecipientResolverBusinessAccount resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new RecipientResolverBusinessAccount();
        injectField(resolver, "businessAccountUserService", businessAccountUserService);
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

    private HistoryEntity buildHistory(UUID ownerBusinessAccountId) {
        var twin = new TwinEntity();
        twin.setOwnerBusinessAccountId(ownerBusinessAccountId);
        var history = new HistoryEntity();
        history.setTwin(twin);
        return history;
    }

    @Nested
    class ResolveBatch {

        @Test
        void resolveBatch_withBusinessAccount_addsAllUsers() throws Exception {
            var businessAccountId = UUID.randomUUID();
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();
            var history = buildHistory(businessAccountId);
            var batch = new RecipientResolveBatch(UUID.randomUUID()).add(history);

            when(businessAccountUserService.findUserIdsByBusinessAccountIdIn(Set.of(businessAccountId)))
                    .thenReturn(Map.of(businessAccountId, Set.of(userId1, userId2)));

            resolver.resolveBatch(batch, new Properties());

            var recipientIds = batch.getRecipientIdsByHistory().get(history);
            assertEquals(2, recipientIds.size());
            assertTrue(recipientIds.contains(userId1));
            assertTrue(recipientIds.contains(userId2));
            verify(businessAccountUserService).findUserIdsByBusinessAccountIdIn(Set.of(businessAccountId));
        }

        @Test
        void resolveBatch_withNullBusinessAccount_doesNotAddAnyUsers() throws Exception {
            var history = buildHistory(null);
            var batch = new RecipientResolveBatch(UUID.randomUUID()).add(history);

            resolver.resolveBatch(batch, new Properties());

            assertTrue(batch.getRecipientIdsByHistory().get(history).isEmpty());
            verifyNoInteractions(businessAccountUserService);
        }

        @Test
        void resolveBatch_preservesExistingRecipients() throws Exception {
            var businessAccountId = UUID.randomUUID();
            var existingUserId = UUID.randomUUID();
            var newUserId = UUID.randomUUID();
            var history = buildHistory(businessAccountId);
            var batch = new RecipientResolveBatch(UUID.randomUUID()).add(history);
            batch.getRecipientIdsByHistory().get(history).add(existingUserId);

            when(businessAccountUserService.findUserIdsByBusinessAccountIdIn(Set.of(businessAccountId)))
                    .thenReturn(Map.of(businessAccountId, Set.of(newUserId)));

            resolver.resolveBatch(batch, new Properties());

            var recipientIds = batch.getRecipientIdsByHistory().get(history);
            assertEquals(2, recipientIds.size());
            assertTrue(recipientIds.contains(existingUserId));
            assertTrue(recipientIds.contains(newUserId));
        }
    }
}
