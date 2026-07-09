package org.twins.core.featurer.notificator.recipient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.space.SpaceRoleUserService;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipientResolverSpaceRolesTest extends BaseUnitTest {

    @Mock
    private SpaceRoleUserService spaceRoleUserService;

    private RecipientResolverSpaceRoles resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new RecipientResolverSpaceRoles();
        injectField(resolver, "spaceRoleUserService", spaceRoleUserService);
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

    private HistoryEntity buildHistory() {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        var history = new HistoryEntity();
        history.setTwin(twin);
        return history;
    }

    @Nested
    class Resolve {

        @Test
        void resolve_addsUsersFromSpaceRoles() throws Exception {
            var history = buildHistory();
            var twinId = history.getTwin().getId();
            var roleId1 = UUID.randomUUID();
            var roleId2 = UUID.randomUUID();
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();
            var recipientIds = new HashSet<UUID>();
            var props = new Properties();
            props.setProperty("spaceRoleIds", roleId1 + "," + roleId2);

            when(spaceRoleUserService.getUsers(twinId, Set.of(roleId1, roleId2)))
                    .thenReturn(Set.of(userId1, userId2));

            resolver.resolve(history, recipientIds, props);

            assertEquals(2, recipientIds.size());
            assertTrue(recipientIds.contains(userId1));
            assertTrue(recipientIds.contains(userId2));
        }

        @Test
        void resolve_appendsToExistingRecipients() throws Exception {
            var history = buildHistory();
            var twinId = history.getTwin().getId();
            var roleId = UUID.randomUUID();
            var existingUserId = UUID.randomUUID();
            var newUserId = UUID.randomUUID();
            var recipientIds = new HashSet<>(Set.of(existingUserId));
            var props = new Properties();
            props.setProperty("spaceRoleIds", roleId.toString());

            when(spaceRoleUserService.getUsers(twinId, Set.of(roleId)))
                    .thenReturn(Set.of(newUserId));

            resolver.resolve(history, recipientIds, props);

            assertEquals(2, recipientIds.size());
            assertTrue(recipientIds.contains(existingUserId));
            assertTrue(recipientIds.contains(newUserId));
        }

        @Test
        void resolve_withEmptyResultFromService_keepsExistingRecipients() throws Exception {
            var history = buildHistory();
            var twinId = history.getTwin().getId();
            var roleId = UUID.randomUUID();
            var recipientIds = new HashSet<UUID>();
            var props = new Properties();
            props.setProperty("spaceRoleIds", roleId.toString());

            when(spaceRoleUserService.getUsers(twinId, Set.of(roleId)))
                    .thenReturn(Set.of());

            resolver.resolve(history, recipientIds, props);

            assertTrue(recipientIds.isEmpty());
        }
    }
}
