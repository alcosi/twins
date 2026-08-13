package org.twins.core.featurer.notificator.recipient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.service.usergroup.UserGroupService;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class RecipientResolverUserGroupsTest extends BaseUnitTest {

    @Mock
    private UserGroupService userGroupService;

    private RecipientResolverUserGroups resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new RecipientResolverUserGroups();
        injectField(resolver, "userGroupService", userGroupService);
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

    private HistoryEntity buildHistory(UUID domainId, UUID businessAccountId) {
        var twinClass = new TwinClassEntity();
        twinClass.setDomainId(domainId);
        var twin = new TwinEntity();
        twin.setOwnerBusinessAccountId(businessAccountId);
        twin.setTwinClass(twinClass);
        var history = new HistoryEntity();
        history.setTwin(twin);
        return history;
    }

    @Nested
    class ResolveBatch {

        @Test
        void resolveBatch_addsUsersFromUserGroups() throws Exception {
            var domainId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();
            var history = buildHistory(domainId, businessAccountId);
            var groupId1 = UUID.randomUUID();
            var groupId2 = UUID.randomUUID();
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();
            var batch = new RecipientResolveBatch(domainId).add(history);
            var props = new Properties();
            props.setProperty("userGroupIds", groupId1 + "," + groupId2);

            when(userGroupService.getUsersForGroupsIn(domainId, Set.of(businessAccountId), Set.of(groupId1, groupId2)))
                    .thenReturn(Map.of(businessAccountId, Set.of(userId1, userId2)));

            resolver.resolveBatch(batch, props);

            var recipientIds = batch.getRecipientIdsByHistory().get(history);
            assertEquals(2, recipientIds.size());
            assertTrue(recipientIds.contains(userId1));
            assertTrue(recipientIds.contains(userId2));
        }

        @Test
        void resolveBatch_appendsToExistingRecipients() throws Exception {
            var domainId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();
            var history = buildHistory(domainId, businessAccountId);
            var groupId = UUID.randomUUID();
            var existingUserId = UUID.randomUUID();
            var newUserId = UUID.randomUUID();
            var batch = new RecipientResolveBatch(domainId).add(history);
            batch.getRecipientIdsByHistory().get(history).add(existingUserId);
            var props = new Properties();
            props.setProperty("userGroupIds", groupId.toString());

            when(userGroupService.getUsersForGroupsIn(domainId, Set.of(businessAccountId), Set.of(groupId)))
                    .thenReturn(Map.of(businessAccountId, Set.of(newUserId)));

            resolver.resolveBatch(batch, props);

            var recipientIds = batch.getRecipientIdsByHistory().get(history);
            assertEquals(2, recipientIds.size());
            assertTrue(recipientIds.contains(existingUserId));
            assertTrue(recipientIds.contains(newUserId));
        }

        @Test
        void resolveBatch_withEmptyResultFromService_keepsExistingRecipients() throws Exception {
            var domainId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();
            var history = buildHistory(domainId, businessAccountId);
            var groupId = UUID.randomUUID();
            var batch = new RecipientResolveBatch(domainId).add(history);
            var props = new Properties();
            props.setProperty("userGroupIds", groupId.toString());

            when(userGroupService.getUsersForGroupsIn(domainId, Set.of(businessAccountId), Set.of(groupId)))
                    .thenReturn(Map.of());

            resolver.resolveBatch(batch, props);

            assertTrue(batch.getRecipientIdsByHistory().get(history).isEmpty());
        }
    }
}
