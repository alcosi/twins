package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinAssigneeToCurrentUserTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    private TwinValidatorTwinAssigneeToCurrentUser validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new TwinValidatorTwinAssigneeToCurrentUser();
        setField(validator, "authService", authService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
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

    private ApiUser apiUserWithId(UUID userId) throws Exception {
        var user = new UserEntity();
        var idField = UserEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, userId);

        var apiUser = mock(ApiUser.class);
        when(apiUser.getUser()).thenReturn(user);
        return apiUser;
    }

    @Nested
    class IsValid {

        @Test
        void isValid_assigneeMatchesCurrentUser_returnsValid() throws Exception {
            var userId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setAssignerUserId(userId);

            var apiUser = apiUserWithId(userId);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = validator.isValid(new Properties(), List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_assigneeDifferentFromCurrentUser_returnsInvalid() throws Exception {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setAssignerUserId(UUID.randomUUID());

            var apiUser = apiUserWithId(UUID.randomUUID());
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = validator.isValid(new Properties(), List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_assigneeNull_returnsInvalid() throws Exception {
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            var apiUser = apiUserWithId(UUID.randomUUID());
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = validator.isValid(new Properties(), List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_assigneeMatchesCurrentUser_inverted_returnsInvalid() throws Exception {
            var userId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setAssignerUserId(userId);

            var apiUser = apiUserWithId(userId);
            when(authService.getApiUser()).thenReturn(apiUser);

            var result = validator.isValid(new Properties(), List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
