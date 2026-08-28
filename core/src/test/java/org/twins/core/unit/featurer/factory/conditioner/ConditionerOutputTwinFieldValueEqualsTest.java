package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerOutputTwinFieldValueEquals;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputDbFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConditionerOutputTwinFieldValueEqualsTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromItemOutputDbFields lookuper;

    private ConditionerOutputTwinFieldValueEquals conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerOutputTwinFieldValueEquals();
        setField(conditioner, "fieldLookupers", fieldLookupers);
        when(fieldLookupers.getFromItemOutputDbFields()).thenReturn(lookuper);
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

    private Properties props(UUID fieldId, String value) {
        var p = new Properties();
        p.put("twinClassFieldId", fieldId.toString());
        p.put("value", value);
        return p;
    }

    @Nested
    class Check {

        @Test
        void check_outputFieldValueEqualsParam_returnsTrue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var fv = mock(FieldValue.class);
            when(fv.hasValue("expected")).thenReturn(true);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId))).thenReturn(fv);

            assertTrue(conditioner.check(props(fieldId, "expected"), mock(FactoryItem.class)));
        }

        @Test
        void check_outputFieldValueDiffersFromParam_returnsFalse() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var fv = mock(FieldValue.class);
            when(fv.hasValue("expected")).thenReturn(false);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId))).thenReturn(fv);

            assertFalse(conditioner.check(props(fieldId, "expected"), mock(FactoryItem.class)));
        }

        @Test
        void check_usesItemOutputDbLookuper() throws ServiceException {
            // contract: value is resolved from the item OUTPUT DB fields
            var fieldId = UUID.randomUUID();
            var fv = mock(FieldValue.class);
            when(fv.hasValue("expected")).thenReturn(true);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId))).thenReturn(fv);

            conditioner.check(props(fieldId, "expected"), mock(FactoryItem.class));

            verify(fieldLookupers).getFromItemOutputDbFields();
        }
    }
}
