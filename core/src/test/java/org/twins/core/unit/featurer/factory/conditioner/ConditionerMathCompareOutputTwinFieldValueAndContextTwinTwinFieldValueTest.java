package org.twins.core.unit.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.ConditionerMathCompareOutputTwinFieldValueAndContextTwinTwinFieldValue;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinUncommitedFields;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputDbFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionerMathCompareOutputTwinFieldValueAndContextTwinTwinFieldValueTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromItemOutputDbFields outputLookuper;

    @Mock
    private FieldLookuperFromContextTwinUncommitedFields contextLookuper;

    private ConditionerMathCompareOutputTwinFieldValueAndContextTwinTwinFieldValue conditioner;

    @BeforeEach
    void setUp() throws Exception {
        conditioner = new ConditionerMathCompareOutputTwinFieldValueAndContextTwinTwinFieldValue();
        setField(conditioner, "fieldLookupers", fieldLookupers);
        // lenient: the throwing tests below never resolve a field via these lookupers
        lenient().when(fieldLookupers.getFromItemOutputDbFields()).thenReturn(outputLookuper);
        lenient().when(fieldLookupers.getFromContextTwinUncommitedFields()).thenReturn(contextLookuper);
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

    private Properties props(UUID greaterFieldId, UUID comparisonFieldId, boolean equals) {
        var p = new Properties();
        p.put("greaterTwinClassField", greaterFieldId.toString());
        p.put("comparisonTwinClassField", comparisonFieldId.toString());
        p.put("equals", String.valueOf(equals));
        return p;
    }

    private FactoryItem itemWithValues(UUID greaterFieldId, FieldValue greaterValue,
                                       UUID comparisonFieldId, FieldValue comparisonValue) throws ServiceException {
        // lenient: the throwing tests below short-circuit before one of the lookups fires
        lenient().when(outputLookuper.lookupFieldValue(any(FactoryItem.class), eq(greaterFieldId))).thenReturn(greaterValue);
        lenient().when(contextLookuper.lookupFieldValue(any(FactoryItem.class), eq(comparisonFieldId))).thenReturn(comparisonValue);
        return mock(FactoryItem.class);
    }

    private FieldValueText textValue(String value) {
        var fv = mock(FieldValueText.class);
        lenient().when(fv.getValue()).thenReturn(value);
        return fv;
    }

    @Nested
    class Check {

        @Test
        void check_strictGreater_outputGreaterThanContext_returnsTrue() throws ServiceException {
            // equals=false → condition is greater > comparison
            var gf = UUID.randomUUID();
            var cf = UUID.randomUUID();
            var item = itemWithValues(gf, textValue("10"), cf, textValue("5"));

            assertTrue(conditioner.check(props(gf, cf, false), item));
        }

        @Test
        void check_strictGreater_outputEqualsContext_returnsFalse() throws ServiceException {
            var gf = UUID.randomUUID();
            var cf = UUID.randomUUID();
            var item = itemWithValues(gf, textValue("10"), cf, textValue("10"));

            assertFalse(conditioner.check(props(gf, cf, false), item));
        }

        @Test
        void check_equalTo_outputEqualsContext_returnsTrue() throws ServiceException {
            // equals=true → condition is greater >= comparison
            var gf = UUID.randomUUID();
            var cf = UUID.randomUUID();
            var item = itemWithValues(gf, textValue("10"), cf, textValue("10"));

            assertTrue(conditioner.check(props(gf, cf, true), item));
        }

        @Test
        void check_outputLessThanContext_strictReturnsFalse() throws ServiceException {
            var gf = UUID.randomUUID();
            var cf = UUID.randomUUID();
            var item = itemWithValues(gf, textValue("5"), cf, textValue("10"));

            assertFalse(conditioner.check(props(gf, cf, false), item));
        }

        @Test
        void check_greaterValueNotText_throws() throws ServiceException {
            var gf = UUID.randomUUID();
            var cf = UUID.randomUUID();
            var notText = mock(FieldValue.class);
            var item = itemWithValues(gf, notText, cf, textValue("5"));

            assertThrows(ServiceException.class, () -> conditioner.check(props(gf, cf, false), item));
        }

        @Test
        void check_comparisonValueNotText_throws() throws ServiceException {
            var gf = UUID.randomUUID();
            var cf = UUID.randomUUID();
            var notText = mock(FieldValue.class);
            var item = itemWithValues(gf, textValue("5"), cf, notText);

            assertThrows(ServiceException.class, () -> conditioner.check(props(gf, cf, false), item));
        }
    }
}
