package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerHeadTwinFieldValueEquals;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinHeadTwinDbFields;
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

class ConditionerHeadTwinFieldValueEqualsTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextTwinHeadTwinDbFields lookuper;

    private ConditionerHeadTwinFieldValueEquals conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerHeadTwinFieldValueEquals();
        setField(conditioner, "fieldLookupers", fieldLookupers);
        when(fieldLookupers.getFromContextTwinHeadTwinDbFields()).thenReturn(lookuper);
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
        void check_headTwinFieldValueEqualsParam_returnsTrue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var fv = mock(FieldValue.class);
            when(fv.hasValue("expected")).thenReturn(true);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId))).thenReturn(fv);

            assertTrue(conditioner.check(props(fieldId, "expected"), mock(FactoryItem.class)));
        }

        @Test
        void check_headTwinFieldValueDiffersFromParam_returnsFalse() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var fv = mock(FieldValue.class);
            when(fv.hasValue("expected")).thenReturn(false);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId))).thenReturn(fv);

            assertFalse(conditioner.check(props(fieldId, "expected"), mock(FactoryItem.class)));
        }

        @Test
        void check_usesHeadTwinLookuper() throws ServiceException {
            // contract: value is resolved from the head twin DB fields
            var fieldId = UUID.randomUUID();
            var fv = mock(FieldValue.class);
            when(fv.hasValue("expected")).thenReturn(true);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId))).thenReturn(fv);

            conditioner.check(props(fieldId, "expected"), mock(FactoryItem.class));

            verify(fieldLookupers).getFromContextTwinHeadTwinDbFields();
        }
    }
}
