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
import org.twins.core.featurer.fieldinitializer.FieldInitializerUrlFixed;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldInitializerUrlFixedTest extends BaseUnitTest {

    @Mock
    private FeaturerService featurerService;

    @Mock
    private TwinService twinService;

    private FieldInitializerUrlFixed initializer;

    @BeforeEach
    void setUp() throws Exception {
        initializer = new FieldInitializerUrlFixed();
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
        field.setKey("urlField");
        field.setFieldInitializerParams(new HashMap<>());
        return field;
    }

    @Nested
    class TryToInitializeValue {

        @Test
        void tryToInitializeValue_createsAndSetsUrl() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "https://example.com/path");

            var props = new Properties();
            props.setProperty("value", "https://example.com/path");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            var value = new FieldValueText(twinClassField);
            when(twinService.createFieldValue(twinClassField)).thenReturn(value);

            var result = initializer.tryToInitializeValue(twin, twinClassField);

            assertNotNull(result);
            assertEquals("https://example.com/path", result.getValue());
            assertTrue(result.isSystemInitialized());
        }

        @Test
        void tryToInitializeValue_withLocalhostUrl() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "http://localhost:8080/api");

            var props = new Properties();
            props.setProperty("value", "http://localhost:8080/api");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            var value = new FieldValueText(twinClassField);
            when(twinService.createFieldValue(twinClassField)).thenReturn(value);

            var result = initializer.tryToInitializeValue(twin, twinClassField);

            assertNotNull(result);
            assertEquals("http://localhost:8080/api", result.getValue());
            assertTrue(result.isSystemInitialized());
        }
    }

    @Nested
    class TryToOverrideValue {

        @Test
        void tryToOverrideValue_withUndefinedValue_setsUrl() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "https://overridden.com");

            var value = new FieldValueText(twinClassField);

            var props = new Properties();
            props.setProperty("value", "https://overridden.com");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            initializer.tryToOverrideValue(twin, value);

            assertEquals("https://overridden.com", value.getValue());
            assertTrue(value.isSystemInitialized());
        }

        @Test
        void tryToOverrideValue_withSystemInitializedValue_doesNotOverride() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "https://new.com");

            var value = new FieldValueText(twinClassField);
            value.setValue("https://original.com");
            value.setSystemInitialized(true);

            initializer.tryToOverrideValue(twin, value);

            assertEquals("https://original.com", value.getValue());
        }
    }
}
