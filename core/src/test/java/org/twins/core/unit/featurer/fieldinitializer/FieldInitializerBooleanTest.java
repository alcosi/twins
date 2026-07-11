package org.twins.core.unit.featurer.fieldinitializer;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.featurer.fieldinitializer.FieldInitializerBoolean;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldInitializerBooleanTest extends BaseUnitTest {

    @Mock
    private FeaturerService featurerService;

    @Mock
    private TwinService twinService;

    private FieldInitializerBoolean initializer;

    @BeforeEach
    void setUp() throws Exception {
        initializer = new FieldInitializerBoolean();
        setField(initializer, "featurerService", featurerService);
        setField(initializer, "twinService", twinService);
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

    private TwinClassFieldEntity buildTwinClassField() {
        var field = new TwinClassFieldEntity();
        field.setId(java.util.UUID.randomUUID());
        field.setKey("testField");
        field.setFieldInitializerParams(new HashMap<>());
        return field;
    }

    private FieldValueBoolean buildFieldValue(TwinClassFieldEntity twinClassField) {
        return new FieldValueBoolean(twinClassField);
    }

    @Nested
    class TryToInitializeValue {

        @Test
        void tryToInitializeValue_withTrue_createsAndSetsValue() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "true");

            var props = new Properties();
            props.setProperty("value", "true");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            var value = buildFieldValue(twinClassField);
            when(twinService.createFieldValue(twinClassField)).thenReturn(value);

            var result = initializer.tryToInitializeValue(twin, twinClassField);

            assertNotNull(result);
            assertTrue(result.getValue());
            assertTrue(result.isSystemInitialized());
        }

        @Test
        void tryToInitializeValue_withFalse_createsAndSetsValue() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "false");

            var props = new Properties();
            props.setProperty("value", "false");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            var value = buildFieldValue(twinClassField);
            when(twinService.createFieldValue(twinClassField)).thenReturn(value);

            var result = initializer.tryToInitializeValue(twin, twinClassField);

            assertNotNull(result);
            assertFalse(result.getValue());
            assertTrue(result.isSystemInitialized());
        }
    }

    @Nested
    class TryToOverrideValue {

        @Test
        void tryToOverrideValue_withUndefinedValue_setsValue() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "true");

            var value = buildFieldValue(twinClassField);

            var props = new Properties();
            props.setProperty("value", "true");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            initializer.tryToOverrideValue(twin, value);

            assertTrue(value.getValue());
            assertTrue(value.isSystemInitialized());
        }

        @Test
        void tryToOverrideValue_withSystemInitializedValue_doesNotOverride() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "false");

            var value = buildFieldValue(twinClassField);
            value.setValue(true);
            value.setSystemInitialized(true);

            initializer.tryToOverrideValue(twin, value);

            assertTrue(value.getValue());
        }
    }
}
