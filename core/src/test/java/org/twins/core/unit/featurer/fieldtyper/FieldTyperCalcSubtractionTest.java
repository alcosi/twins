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

class FieldTyperCalcSubtractionTest extends BaseUnitTest {

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldTyperCalcSubtraction fieldTyper;

    private UUID firstFieldId;
    private UUID secondFieldId;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperCalcSubtraction();
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

    private Properties properties() {
        var props = new Properties();
        props.setProperty("firstFieldId", firstFieldId.toString());
        props.setProperty("secondFieldId", secondFieldId.toString());
        props.setProperty("decimalPlaces", "2");
        props.setProperty("roundingMode", "HALF_UP");
        return props;
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_twoOperands_returnsFirstMinusSecond() throws ServiceException {
            // Subtraction is non-commutative: result MUST be first - second, never second - first.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("10"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("3"));

            var result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("7.00", result.getValue());
        }

        @Test
        void deserializeValue_negativeResult_returnsNegativeFirstMinusSecond() throws ServiceException {
            // Confirms order is first - second (3 - 10 = -7), not the absolute value.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("3"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("10"));

            var result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("-7.00", result.getValue());
        }

        @Test
        void deserializeValue_nullFirstOperand_treatedAsZero() throws ServiceException {
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            // first operand resolves to null -> coerced to ZERO; 0 - 5 = -5.00
            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn((BigDecimal) null);
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("5"));

            var result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("-5.00", result.getValue());
        }

        @Test
        void deserializeValue_nullSecondOperand_treatedAsZero() throws ServiceException {
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("5"));

            var result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("5.00", result.getValue());
        }

        @Test
        void deserializeValue_bothNull_returnsZero() throws ServiceException {
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            // Both operands resolve to the BigDecimal.ZERO default; no stubs needed since the
            // service returns the default value argument unchanged.

            var result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("0.00", result.getValue());
        }

        @Test
        void deserializeValue_preservesOperandOrderAsymmetricValues() throws ServiceException {
            // A second guard against accidental operand swap: 100 - 1 = 99.00.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("100"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("1"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("99.00", result.getValue());
        }
    }
}
