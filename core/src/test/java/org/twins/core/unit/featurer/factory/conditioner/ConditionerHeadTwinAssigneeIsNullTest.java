package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerHeadTwinAssigneeIsNull;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerHeadTwinAssigneeIsNullTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private ConditionerHeadTwinAssigneeIsNull conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerHeadTwinAssigneeIsNull();
        setField(conditioner, "twinService", twinService);
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

    private FactoryItem itemWithTwin(TwinEntity twin) {
        var item = mock(FactoryItem.class);
        when(item.getTwin()).thenReturn(twin);
        return item;
    }

    @Nested
    class Check {

        @Test
        void check_headTwinAssignerNull_returnsTrue() throws ServiceException {
            var factoryTwin = mock(TwinEntity.class);
            var head = mock(TwinEntity.class);
            when(head.getAssignerUserId()).thenReturn(null);
            when(twinService.loadHead(factoryTwin)).thenReturn(head);

            assertTrue(conditioner.check(new Properties(), itemWithTwin(factoryTwin)));
        }

        @Test
        void check_headTwinAssignerSet_returnsFalse() throws ServiceException {
            var factoryTwin = mock(TwinEntity.class);
            var head = mock(TwinEntity.class);
            when(head.getAssignerUserId()).thenReturn(UUID.randomUUID());
            when(twinService.loadHead(factoryTwin)).thenReturn(head);

            assertFalse(conditioner.check(new Properties(), itemWithTwin(factoryTwin)));
        }

        @Test
        void check_noHeadTwinDetected_throws() throws ServiceException {
            // contract: a twin with no head twin is a hard error, not a silent true/false
            var factoryTwin = mock(TwinEntity.class);
            when(twinService.loadHead(factoryTwin)).thenReturn(null);

            assertThrows(ServiceException.class,
                    () -> conditioner.check(new Properties(), itemWithTwin(factoryTwin)));
        }
    }
}
