package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerApiUserIsAssignee;
import org.twins.core.service.auth.AuthService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerApiUserIsAssigneeTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private ApiUser apiUser;

    private ConditionerApiUserIsAssignee conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerApiUserIsAssignee();
        setField(conditioner, "authService", authService);
        when(authService.getApiUser()).thenReturn(apiUser);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
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

    private FactoryItem itemWithAssigner(UUID assignerUserId) {
        var twin = mock(TwinEntity.class);
        when(twin.getAssignerUserId()).thenReturn(assignerUserId);
        var item = mock(FactoryItem.class);
        when(item.getTwin()).thenReturn(twin);
        return item;
    }

    @Nested
    class Check {

        @Test
        void check_apiUserIsAssignee_returnsTrue() throws ServiceException {
            var userId = UUID.randomUUID();
            when(apiUser.getUserId()).thenReturn(userId);

            assertTrue(conditioner.check(new Properties(), itemWithAssigner(userId)));
        }

        @Test
        void check_apiUserDiffersFromAssignee_returnsFalse() throws ServiceException {
            when(apiUser.getUserId()).thenReturn(UUID.randomUUID());

            assertFalse(conditioner.check(new Properties(), itemWithAssigner(UUID.randomUUID())));
        }

        @Test
        void check_assignerUserIdNull_returnsFalse() throws ServiceException {
            // contract: null assignerUserId cannot equal a real api user id → false
            when(apiUser.getUserId()).thenReturn(UUID.randomUUID());

            assertFalse(conditioner.check(new Properties(), itemWithAssigner(null)));
        }
    }
}
