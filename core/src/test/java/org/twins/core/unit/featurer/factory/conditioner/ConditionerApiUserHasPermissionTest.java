package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerApiUserHasPermission;
import org.twins.core.service.permission.PermissionService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConditionerApiUserHasPermissionTest extends BaseUnitTest {

    @Mock
    private PermissionService permissionService;

    private ConditionerApiUserHasPermission conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerApiUserHasPermission();
        setField(conditioner, "permissionService", permissionService);
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

    private Properties props(UUID permissionId) {
        var p = new Properties();
        p.put("permissionId", permissionId.toString());
        return p;
    }

    @Nested
    class Check {

        @Test
        void check_currentUserHasPermission_returnsTrue() throws ServiceException {
            var permissionId = UUID.randomUUID();
            when(permissionService.currentUserHasPermission(permissionId)).thenReturn(true);

            assertTrue(conditioner.check(props(permissionId), null));
        }

        @Test
        void check_currentUserLacksPermission_returnsFalse() throws ServiceException {
            var permissionId = UUID.randomUUID();
            when(permissionService.currentUserHasPermission(permissionId)).thenReturn(false);

            assertFalse(conditioner.check(props(permissionId), null));
        }

        @Test
        void check_delegatesExtractedPermissionIdToService() throws ServiceException {
            // contract: the conditioner extracts permissionId from properties and delegates verbatim
            var permissionId = UUID.randomUUID();
            when(permissionService.currentUserHasPermission(permissionId)).thenReturn(true);

            conditioner.check(props(permissionId), null);

            verify(permissionService).currentUserHasPermission(permissionId);
        }
    }
}
