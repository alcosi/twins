package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerHeadTwinFieldExistsAndValueFilled;
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
import static org.mockito.Mockito.when;

class ConditionerHeadTwinFieldExistsAndValueFilledTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextTwinHeadTwinDbFields lookuper;

    private ConditionerHeadTwinFieldExistsAndValueFilled conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerHeadTwinFieldExistsAndValueFilled();
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

    private Properties props(UUID fieldId) {
        var p = new Properties();
        p.put("twinClassFieldId", fieldId.toString());
        return p;
    }

    @Nested
    class Check {

        @Test
        void check_fieldValueExistsAndNotEmpty_returnsTrue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var fv = mock(FieldValue.class);
            when(fv.isNotEmpty()).thenReturn(true);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId))).thenReturn(fv);

            assertTrue(conditioner.check(props(fieldId), mock(FactoryItem.class)));
        }

        @Test
        void check_fieldValueExistsButEmpty_returnsFalse() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var fv = mock(FieldValue.class);
            when(fv.isNotEmpty()).thenReturn(false);
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId))).thenReturn(fv);

            assertFalse(conditioner.check(props(fieldId), mock(FactoryItem.class)));
        }

        @Test
        void check_lookupThrows_returnsFalse() throws ServiceException {
            // contract: missing/absent head twin field is NOT an error here — it resolves to false
            var fieldId = UUID.randomUUID();
            when(lookuper.lookupFieldValue(any(FactoryItem.class), eq(fieldId)))
                    .thenThrow(new ServiceException(org.twins.core.exception.ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "not found"));

            assertFalse(conditioner.check(props(fieldId), mock(FactoryItem.class)));
        }
    }
}
