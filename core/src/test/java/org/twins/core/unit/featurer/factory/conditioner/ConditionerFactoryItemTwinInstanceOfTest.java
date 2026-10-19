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
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinInstanceOf;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ConditionerFactoryItemTwinInstanceOfTest extends BaseUnitTest {

    @Mock
    private TwinClassService twinClassService;

    private ConditionerFactoryItemTwinInstanceOf conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerFactoryItemTwinInstanceOf();
        setField(conditioner, "twinClassService", twinClassService);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        var field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + name);
    }

    private Properties props(UUID classId) {
        var p = new Properties();
        p.put("instanceOfTwinClassId", classId.toString());
        return p;
    }

    private FactoryItem item(TwinClassEntity twinClass) {
        var twin = new TwinEntity().setTwinClass(twinClass);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Check {

        @Test
        void check_isInstanceOf_returnsTrue() throws ServiceException {
            // contract: delegates to twinClassService.isInstanceOf(outputTwin.twinClass, configuredClassId).
            var targetClassId = UUID.randomUUID();
            var twinClass = new TwinClassEntity();
            when(twinClassService.isInstanceOf(eq(twinClass), eq(targetClassId))).thenReturn(true);

            assertTrue(conditioner.check(props(targetClassId), item(twinClass)));
        }

        @Test
        void check_isNotInstanceOf_returnsFalse() throws ServiceException {
            var targetClassId = UUID.randomUUID();
            var twinClass = new TwinClassEntity();
            when(twinClassService.isInstanceOf(eq(twinClass), eq(targetClassId))).thenReturn(false);

            assertFalse(conditioner.check(props(targetClassId), item(twinClass)));
        }

        @Test
        void check_passesOutputTwinClass_notClassId() throws ServiceException {
            // contract: the entity (twinClass), not the id, is the first argument.
            var targetClassId = UUID.randomUUID();
            var twinClass = new TwinClassEntity();
            when(twinClassService.isInstanceOf(eq(twinClass), eq(targetClassId))).thenReturn(true);

            conditioner.check(props(targetClassId), item(twinClass));

            var verification = org.mockito.Mockito.verify(twinClassService);
            verification.isInstanceOf(twinClass, targetClassId);
        }
    }
}
