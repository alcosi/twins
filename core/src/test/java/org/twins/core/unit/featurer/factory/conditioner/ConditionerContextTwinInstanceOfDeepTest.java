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
import org.twins.core.featurer.factory.conditioner.ConditionerContextTwinInstanceOfDeep;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ConditionerContextTwinInstanceOfDeepTest extends BaseUnitTest {

    @Mock
    private TwinClassService twinClassService;

    private ConditionerContextTwinInstanceOfDeep conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerContextTwinInstanceOfDeep();
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

    // lenient: depending on its position in the chain a level exposes a different subset of these stubs
    // (the root item's own twin/class is never inspected — only its context chain is walked).
    private FactoryItem level(TwinClassEntity twinClass, boolean isInstance, UUID targetClassId, FactoryItem nextContext) throws ServiceException {
        var twin = mock(TwinEntity.class);
        lenient().when(twin.getTwinClass()).thenReturn(twinClass);
        var item = mock(FactoryItem.class);
        lenient().when(item.getTwin()).thenReturn(twin);
        lenient().when(item.checkNotMultiplyContextItem()).thenReturn(nextContext);
        lenient().when(twinClassService.isInstanceOf(eq(twinClass), eq(targetClassId))).thenReturn(isInstance);
        return item;
    }

    @Nested
    class Check {

        @Test
        void check_immediateContextIsInstanceOf_returnsTrue() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var twinClass = new TwinClassEntity();
            var twin = mock(TwinEntity.class);
            when(twin.getTwinClass()).thenReturn(twinClass);
            var contextItem = mock(FactoryItem.class);
            when(contextItem.getTwin()).thenReturn(twin);
            var factoryItem = mock(FactoryItem.class);
            when(factoryItem.checkNotMultiplyContextItem()).thenReturn(contextItem);
            when(twinClassService.isInstanceOf(eq(twinClass), eq(targetClassId))).thenReturn(true);

            assertTrue(conditioner.check(buildProperties(targetClassId), factoryItem));
        }

        @Test
        void check_noContextItem_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var factoryItem = mock(FactoryItem.class);
            when(factoryItem.checkNotMultiplyContextItem()).thenReturn(null);

            assertFalse(conditioner.check(buildProperties(targetClassId), factoryItem));
        }

        @Test
        void check_matchDeeperInChain_returnsTrue() throws ServiceException {
            // per class name "...Deep": walk the context chain until an instance is found.
            // chain: not-instance -> not-instance -> instance
            var targetClassId = UUID.randomUUID();
            // distinct ids so the three TwinClassEntity instances are not equals-equal (TwinClassEntity is
            // @Data) — otherwise the per-level isInstanceOf stubs would collide on matching arguments.
            var match = level(new TwinClassEntity().setId(UUID.randomUUID()), true, targetClassId, null);
            var mid = level(new TwinClassEntity().setId(UUID.randomUUID()), false, targetClassId, match);
            var top = level(new TwinClassEntity().setId(UUID.randomUUID()), false, targetClassId, mid);

            assertTrue(conditioner.check(buildProperties(targetClassId), top));
        }
    }
}
