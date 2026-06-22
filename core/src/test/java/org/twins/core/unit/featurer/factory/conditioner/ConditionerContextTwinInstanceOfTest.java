package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerContextTwinInstanceOf;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ConditionerContextTwinInstanceOfTest extends BaseUnitTest {

    @Mock
    private TwinClassService twinClassService;

    private ConditionerContextTwinInstanceOf conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerContextTwinInstanceOf();
        setField(conditioner, "twinClassService", twinClassService);
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

    private Properties buildProperties(UUID classId) {
        var props = new Properties();
        props.put("instanceOfTwinClassId", classId.toString());
        return props;
    }

    private FactoryItem buildItem(TwinClassEntity twinClass) throws ServiceException {
        var twin = mock(TwinEntity.class);
        when(twin.getTwinClass()).thenReturn(twinClass);
        var contextItem = mock(FactoryItem.class);
        when(contextItem.getTwin()).thenReturn(twin);
        var factoryItem = mock(FactoryItem.class);
        when(factoryItem.checkNotMultiplyContextItem()).thenReturn(contextItem);
        return factoryItem;
    }

    @Nested
    class Check {

        @Test
        void check_contextTwinIsInstanceOf_returnsTrue() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var twinClass = new TwinClassEntity();
            when(twinClassService.isInstanceOf(eq(twinClass), eq(targetClassId))).thenReturn(true);

            assertTrue(conditioner.check(buildProperties(targetClassId), buildItem(twinClass)));
        }

        @Test
        void check_contextTwinIsNotInstanceOf_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var twinClass = new TwinClassEntity();
            when(twinClassService.isInstanceOf(eq(twinClass), eq(targetClassId))).thenReturn(false);

            assertFalse(conditioner.check(buildProperties(targetClassId), buildItem(twinClass)));
        }

        @Test
        void check_noContextItem_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var factoryItem = mock(FactoryItem.class);
            when(factoryItem.checkNotMultiplyContextItem()).thenReturn(null);

            assertFalse(conditioner.check(buildProperties(targetClassId), factoryItem));
        }
    }
}
