package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldTyperPointedHeadTest extends BaseUnitTest {

    @Mock
    private TwinClassFieldService twinClassFieldService;

    @Mock
    private TwinService twinService;

    private FieldTyperPointedHead fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperPointedHead();
        setField(fieldTyper, "twinClassFieldService", twinClassFieldService);
        setField(fieldTyper, "twinService", twinService);
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

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties(UUID headFieldId) {
        var props = new Properties();
        props.setProperty("headTwinClassFieldId", headFieldId.toString());
        return props;
    }

    @Nested
    class GetHeadTwinClassFieldSafe {

        @Test
        void getHeadTwinClassFieldSafe_delegatesToService() throws ServiceException {
            // Intended: the pointed-head field id is resolved through TwinClassFieldService.findEntitySafe.
            var headFieldId = UUID.randomUUID();
            var headField = new TwinClassFieldEntity().setId(headFieldId);
            when(twinClassFieldService.findEntitySafe(headFieldId)).thenReturn(headField);

            var result = fieldTyper.getHeadTwinClassFieldSafe(properties(headFieldId));

            assertSame(headField, result);
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_headHasValue_clonesValueUnderLocalClassField() throws ServiceException {
            // Intended: when the head twin already holds a value for the pointed field, that value is
            // cloned and re-bound to THIS twin-class field (so callers see it under their own field id).
            var headFieldId = UUID.randomUUID();
            var headField = new TwinClassFieldEntity().setId(headFieldId);
            when(twinClassFieldService.findEntitySafe(headFieldId)).thenReturn(headField);

            var headValue = new FieldValueText(headField).setValue("head-value");
            var headTwin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setFieldValuesKit(new org.cambium.common.kit.Kit<>(List.of(headValue), FieldValue::getTwinClassFieldId));
            var twin = new TwinEntity().setId(UUID.randomUUID()).setHeadTwin(headTwin);
            var localField = new TwinClassFieldEntity().setId(UUID.randomUUID());

            FieldValue result = fieldTyper.deserializeValue(properties(headFieldId), twinField(twin, localField));

            assertSame(localField, result.getTwinClassField());
            assertEquals("head-value", ((FieldValueText) result).getValue());
        }

        @Test
        void deserializeValue_headHasNoValue_createsEmptyLocalValue() throws ServiceException {
            // Intended: when the head twin has no stored value for the pointed field, an empty value
            // bound to THIS twin-class field is produced via twinService.createFieldValue.
            var headFieldId = UUID.randomUUID();
            var headField = new TwinClassFieldEntity().setId(headFieldId);
            when(twinClassFieldService.findEntitySafe(headFieldId)).thenReturn(headField);

            var headTwin = new TwinEntity()
                    .setId(UUID.randomUUID())
                    .setFieldValuesKit(new org.cambium.common.kit.Kit<>(FieldValue::getTwinClassFieldId));
            var twin = new TwinEntity().setId(UUID.randomUUID()).setHeadTwin(headTwin);
            var localField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var created = new FieldValueText(localField);
            when(twinService.createFieldValue(localField)).thenReturn(created);

            FieldValue result = fieldTyper.deserializeValue(properties(headFieldId), twinField(twin, localField));

            assertSame(created, result);
        }
    }
}
