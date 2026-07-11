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
import org.twins.core.featurer.fieldinitializer.FieldInitializerNull;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldInitializerNullTest extends BaseUnitTest {

    @Mock
    private FeaturerService featurerService;

    @Mock
    private TwinService twinService;

    private FieldInitializerNull initializer;

    @BeforeEach
    void setUp() throws Exception {
        initializer = new FieldInitializerNull();
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
        field.setKey("nullField");
        field.setFieldInitializerParams(new HashMap<>());
        return field;
    }

    @Nested
    class TryToInitializeValue {

        @Test
        void tryToInitializeValue_callsCreateFieldValueThenReturnsNull() throws ServiceException {
            var twin = new TwinEntity();
            var twinClassField = buildTwinClassField();

            when(featurerService.extractProperties(eq(initializer), any(HashMap.class))).thenReturn(new Properties());

            var result = initializer.tryToInitializeValue(twin, twinClassField);

            assertNull(result);
            verify(featurerService).extractProperties(eq(initializer), any(HashMap.class));
        }
    }
}
