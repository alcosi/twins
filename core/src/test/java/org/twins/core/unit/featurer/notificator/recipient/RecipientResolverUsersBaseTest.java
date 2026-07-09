package org.twins.core.featurer.notificator.recipient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserService;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipientResolverUsersBaseTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    private RecipientResolverUsersBase resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new RecipientResolverUsersBase();
        injectField(resolver, "authService", authService);
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
    class Resolve {

        @Test
        void resolve_filtersUsersAndAddsToRecipients() throws Exception {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();
            var history = buildHistory(businessAccountId);
            var recipientIds = new HashSet<UUID>();
            var props = new Properties();
            props.setProperty("userIds", userId1 + "," + userId2);

            var apiUser = mock(ApiUser.class);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(userService.filterUsersByBusinessAccountAndDomain(
                    Set.of(userId1, userId2), businessAccountId, domainId))
                    .thenReturn(Set.of(userId1));

            resolver.resolve(history, recipientIds, props);

            assertEquals(1, recipientIds.size());
            assertTrue(recipientIds.contains(userId1));
        }

        @Test
        void resolve_appendsToExistingRecipients() throws Exception {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var existingUserId = UUID.randomUUID();
            var history = buildHistory(businessAccountId);
            var recipientIds = new HashSet<>(Set.of(existingUserId));
            var props = new Properties();
            props.setProperty("userIds", userId.toString());

            var apiUser = mock(ApiUser.class);
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getDomainId()).thenReturn(domainId);
            when(userService.filterUsersByBusinessAccountAndDomain(
                    Set.of(userId), businessAccountId, domainId))
                    .thenReturn(Set.of(userId));

            resolver.resolve(history, recipientIds, props);

            assertEquals(2, recipientIds.size());
            assertTrue(recipientIds.contains(existingUserId));
            assertTrue(recipientIds.contains(userId));
        }
    }
}
