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
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldTyperCalcMultiplicationTest extends BaseUnitTest {

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldTyperCalcMultiplication fieldTyper;

    private UUID firstFieldId;
    private UUID secondFieldId;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperCalcMultiplication();
        setField(fieldTyper, "twinClassFieldService", twinClassFieldService);
        firstFieldId = UUID.randomUUID();
        secondFieldId = UUID.randomUUID();
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

    private Properties properties(boolean replaceZeroWithOne) {
        var props = new Properties();
        props.setProperty("firstFieldId", firstFieldId.toString());
        props.setProperty("secondFieldId", secondFieldId.toString());
        props.setProperty("decimalPlaces", "2");
        props.setProperty("roundingMode", "HALF_UP");
        props.setProperty("replaceZeroWithOne", String.valueOf(replaceZeroWithOne));
        return props;
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_twoOperands_returnsFirstTimesSecond() throws ServiceException {
            // Multiplication is commutative; result is the product, rounded to scale.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties(false);

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("3"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("4"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("12.00", result.getValue());
        }

        @Test
        void deserializeValue_decimalOperands_roundedToScale() throws ServiceException {
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties(false);

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("2.5"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("2.5"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("6.25", result.getValue());
        }

        @Test
        void deserializeValue_zeroOperand_replaceDisabled_yieldsZero() throws ServiceException {
            // Without replaceZeroWithOne, a zero operand zeroes the whole product.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties(false);

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("0"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("7"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("0.00", result.getValue());
        }

        @Test
        void deserializeValue_zeroOperand_replaceEnabled_treatsZeroAsOne() throws ServiceException {
            // With replaceZeroWithOne, a zero operand becomes 1 so the product survives.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties(true);

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("0"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("7"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("7.00", result.getValue());
        }

        @Test
        void deserializeValue_nullOperand_replaceDisabled_treatedAsZero() throws ServiceException {
            // With replace disabled, null is coerced to ZERO (no swap to ONE) -> 0 * 7 = 0.00.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties(false);

            // first operand resolves to null -> prepare() coerces to ZERO; 0 * 7 = 0.00
            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn((BigDecimal) null);
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("7"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("0.00", result.getValue());
        }

        @Test
        void deserializeValue_bothZero_replaceEnabled_yieldsOne() throws ServiceException {
            // Both operands 0 with replace -> 1 * 1 = 1.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties(true);

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("0"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("0"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("1.00", result.getValue());
        }
    }
}
