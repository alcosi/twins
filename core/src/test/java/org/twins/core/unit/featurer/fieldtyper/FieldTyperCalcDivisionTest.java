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

class FieldTyperCalcDivisionTest extends BaseUnitTest {

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldTyperCalcDivision fieldTyper;

    private UUID firstFieldId;
    private UUID secondFieldId;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperCalcDivision();
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

    private Properties properties(String divisionByZeroResult) {
        var props = new Properties();
        props.setProperty("firstFieldId", firstFieldId.toString());
        props.setProperty("secondFieldId", secondFieldId.toString());
        props.setProperty("decimalPlaces", "2");
        props.setProperty("roundingMode", "HALF_UP");
        props.setProperty("divisionByZeroResult", divisionByZeroResult);
        return props;
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_twoOperands_returnsFirstDividedBySecond() throws ServiceException {
            // Division is non-commutative: result MUST be first / second, never second / first.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties("<n/a>");

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("10"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("4"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("2.50", result.getValue());
        }

        @Test
        void deserializeValue_preservesOperandOrderAsymmetricValues() throws ServiceException {
            // 9 / 2 = 4.50; reversed would be 0.22. Guards against accidental operand swap.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties("<n/a>");

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("9"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("2"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("4.50", result.getValue());
        }

        @Test
        void deserializeValue_divisionByZero_returnsFallbackResult() throws ServiceException {
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties("n/a");

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("10"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("0"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("n/a", result.getValue());
        }

        @Test
        void deserializeValue_divisionByZero_returnsCustomFallbackResult() throws ServiceException {
            // Confirms the fallback value comes verbatim from the divisionByZeroResul param.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties("DIV_BY_ZERO");

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("10"));
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("0"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("DIV_BY_ZERO", result.getValue());
        }

        @Test
        void deserializeValue_nullFirst_treatedAsZero() throws ServiceException {
            // 0 / 5 = 0.00 (second operand non-zero, so it goes through the divide path).
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties("<n/a>");

            // first operand resolves to null -> coerced to ZERO; 0 / 5 = 0.00
            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn((BigDecimal) null);
            when(twinClassFieldService.getDecimalValue(twin, secondFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("5"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("0.00", result.getValue());
        }

        @Test
        void deserializeValue_nullSecond_treatedAsZero_returnsFallback() throws ServiceException {
            // null second -> 0 -> division by zero -> fallback.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties("zero");

            when(twinClassFieldService.getDecimalValue(twin, firstFieldId, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("10"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("zero", result.getValue());
        }
    }
}
