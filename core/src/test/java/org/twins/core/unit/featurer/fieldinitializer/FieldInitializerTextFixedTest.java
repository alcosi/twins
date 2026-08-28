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
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.fieldinitializer.FieldInitializerTextFixed;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldInitializerTextFixedTest extends BaseUnitTest {

    @Mock
    private FeaturerService featurerService;

    @Mock
    private TwinService twinService;

    private FieldInitializerTextFixed initializer;

    @BeforeEach
    void setUp() throws Exception {
        initializer = new FieldInitializerTextFixed();
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
        field.setId(UUID.randomUUID());
        field.setKey("textField");
        field.setFieldInitializerParams(new HashMap<>());
        return field;
    }

    @Nested
    class TryToInitializeValue {

        @Test
        void tryToInitializeValue_createsAndSetsValue() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "hello world");

            var props = new Properties();
            props.setProperty("value", "hello world");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            var value = new FieldValueText(twinClassField);
            when(twinService.createFieldValue(twinClassField)).thenReturn(value);

            var result = initializer.tryToInitializeValue(twin, twinClassField);

            assertNotNull(result);
            assertEquals("hello world", result.getValue());
            assertTrue(result.isSystemInitialized());
        }

        @Test
        void tryToInitializeValue_withEmptyValue() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "");

            var props = new Properties();
            props.setProperty("value", "");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            var value = new FieldValueText(twinClassField);
            when(twinService.createFieldValue(twinClassField)).thenReturn(value);

            var result = initializer.tryToInitializeValue(twin, twinClassField);

            assertNotNull(result);
            assertEquals("", result.getValue());
            assertTrue(result.isSystemInitialized());
        }
    }

    @Nested
    class TryToOverrideValue {

        @Test
        void tryToOverrideValue_withUndefinedValue_setsValue() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "overridden");

            var value = new FieldValueText(twinClassField);

            var props = new Properties();
            props.setProperty("value", "overridden");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            initializer.tryToOverrideValue(twin, value);

            assertEquals("overridden", value.getValue());
            assertTrue(value.isSystemInitialized());
        }

        @Test
        void tryToOverrideValue_withSystemInitializedValue_doesNotOverride() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "new value");

            var value = new FieldValueText(twinClassField);
            value.setValue("original");
            value.setSystemInitialized(true);

            initializer.tryToOverrideValue(twin, value);

            assertEquals("original", value.getValue());
        }
    }
}
