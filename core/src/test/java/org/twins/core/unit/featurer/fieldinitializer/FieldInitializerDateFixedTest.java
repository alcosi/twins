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
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.fieldinitializer.FieldInitializerDateFixed;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldInitializerDateFixedTest extends BaseUnitTest {

    @Mock
    private FeaturerService featurerService;

    @Mock
    private TwinService twinService;

    private FieldInitializerDateFixed initializer;

    @BeforeEach
    void setUp() throws Exception {
        initializer = new FieldInitializerDateFixed();
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
        field.setKey("dateField");
        field.setFieldInitializerParams(new HashMap<>());
        return field;
    }

    private FieldValueDate buildFieldValue(TwinClassFieldEntity twinClassField) {
        return new FieldValueDate(twinClassField, "yyyy-MM-dd HH:mm:ss");
    }

    @Nested
    class TryToInitializeValue {

        @Test
        void tryToInitializeValue_withCustomPattern_createsAndSetsDate() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "2025-01-15 10:30:00");
            twinClassField.getFieldInitializerParams().put("pattern", "yyyy-MM-dd HH:mm:ss");

            var props = new Properties();
            props.setProperty("value", "2025-01-15 10:30:00");
            props.setProperty("pattern", "yyyy-MM-dd HH:mm:ss");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            var value = buildFieldValue(twinClassField);
            when(twinService.createFieldValue(twinClassField)).thenReturn(value);

            var result = initializer.tryToInitializeValue(twin, twinClassField);

            assertNotNull(result);
            assertNotNull(result.getDate());
            assertEquals(LocalDateTime.of(2025, 1, 15, 10, 30, 0), result.getDate());
            assertTrue(result.isSystemInitialized());
        }

        @Test
        void tryToInitializeValue_withIsoPattern_createsAndSetsDate() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "2025-06-01T00:00:00");
            twinClassField.getFieldInitializerParams().put("pattern", "yyyy-MM-dd'T'HH:mm:ss");

            var props = new Properties();
            props.setProperty("value", "2025-06-01T00:00:00");
            props.setProperty("pattern", "yyyy-MM-dd'T'HH:mm:ss");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            var value = buildFieldValue(twinClassField);
            when(twinService.createFieldValue(twinClassField)).thenReturn(value);

            var result = initializer.tryToInitializeValue(twin, twinClassField);

            assertNotNull(result);
            assertNotNull(result.getDate());
            assertEquals(LocalDateTime.of(2025, 6, 1, 0, 0, 0), result.getDate());
            assertTrue(result.isSystemInitialized());
        }
    }

    @Nested
    class TryToOverrideValue {

        @Test
        void tryToOverrideValue_withUndefinedValue_setsDate() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "2025-03-20 15:45:00");
            twinClassField.getFieldInitializerParams().put("pattern", "yyyy-MM-dd HH:mm:ss");

            var value = buildFieldValue(twinClassField);

            var props = new Properties();
            props.setProperty("value", "2025-03-20 15:45:00");
            props.setProperty("pattern", "yyyy-MM-dd HH:mm:ss");
            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(props);

            initializer.tryToOverrideValue(twin, value);

            assertNotNull(value.getDate());
            assertEquals(LocalDateTime.of(2025, 3, 20, 15, 45, 0), value.getDate());
            assertTrue(value.isSystemInitialized());
        }

        @Test
        void tryToOverrideValue_withSystemInitializedValue_doesNotOverride() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            twinClassField.getFieldInitializerParams().put("value", "2025-12-31 23:59:59");
            twinClassField.getFieldInitializerParams().put("pattern", "yyyy-MM-dd HH:mm:ss");

            var value = buildFieldValue(twinClassField);
            var originalDate = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
            value.setDate(originalDate);
            value.setSystemInitialized(true);

            initializer.tryToOverrideValue(twin, value);

            assertEquals(originalDate, value.getDate());
        }
    }
}
