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
import org.twins.core.featurer.fieldinitializer.FieldInitializerDateCurrent;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldInitializerDateCurrentTest extends BaseUnitTest {

    @Mock
    private FeaturerService featurerService;

    @Mock
    private TwinService twinService;

    private FieldInitializerDateCurrent initializer;

    @BeforeEach
    void setUp() throws Exception {
        initializer = new FieldInitializerDateCurrent();
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
        return new FieldValueDate(twinClassField, null);
    }

    @Nested
    class TryToInitializeValue {

        @Test
        void tryToInitializeValue_createsAndSetsCurrentDate() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            var before = LocalDateTime.now().minusSeconds(1);

            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(new Properties());

            var value = buildFieldValue(twinClassField);
            when(twinService.createFieldValue(twinClassField)).thenReturn(value);

            var result = initializer.tryToInitializeValue(twin, twinClassField);

            var after = LocalDateTime.now().plusSeconds(1);
            assertNotNull(result);
            assertNotNull(result.getDate());
            assertTrue(result.getDate().isAfter(before));
            assertTrue(result.getDate().isBefore(after));
            assertTrue(result.isSystemInitialized());
        }
    }

    @Nested
    class TryToOverrideValue {

        @Test
        void tryToOverrideValue_withUndefinedValue_setsCurrentDate() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            var value = buildFieldValue(twinClassField);
            var before = LocalDateTime.now().minusSeconds(1);

            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(new Properties());

            initializer.tryToOverrideValue(twin, value);

            var after = LocalDateTime.now().plusSeconds(1);
            assertNotNull(value.getDate());
            assertTrue(value.getDate().isAfter(before));
            assertTrue(value.getDate().isBefore(after));
            assertTrue(value.isSystemInitialized());
        }

        @Test
        void tryToOverrideValue_withSystemInitializedValue_doesNotOverride() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();
            var value = buildFieldValue(twinClassField);
            var originalDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
            value.setDate(originalDate);
            value.setSystemInitialized(true);

            initializer.tryToOverrideValue(twin, value);

            assertEquals(originalDate, value.getDate());
        }
    }
}
