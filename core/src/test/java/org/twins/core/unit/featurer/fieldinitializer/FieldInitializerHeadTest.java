package org.twins.core.unit.featurer.fieldinitializer;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.featurer.fieldinitializer.FieldInitializerHead;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldInitializerHeadTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FieldInitializerHead initializer;

    @BeforeEach
    void setUp() throws Exception {
        initializer = new FieldInitializerHead();
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

    private TwinEntity buildTwin(UUID headTwinId) {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        twin.setHeadTwinId(headTwinId);
        return twin;
    }

    private TwinClassFieldEntity buildTwinClassField(UUID fieldId) {
        var field = new TwinClassFieldEntity();
        field.setId(fieldId);
        field.setKey("headField");
        return field;
    }

    private FieldValueText buildFieldValue(TwinClassFieldEntity twinClassField) {
        return new FieldValueText(twinClassField);
    }
}
