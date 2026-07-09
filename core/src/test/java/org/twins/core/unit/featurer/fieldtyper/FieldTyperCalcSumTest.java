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

class FieldTyperCalcSumTest extends BaseUnitTest {

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldTyperCalcSum fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperCalcSum();
        setField(fieldTyper, "twinClassFieldService", twinClassFieldService);
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

    private Properties properties(UUID... fieldIds) {
        var props = new Properties();
        props.setProperty("decimalPlaces", "2");
        props.setProperty("roundingMode", "HALF_UP");
        if (fieldIds.length > 0) {
            var joined = new StringBuilder();
            for (int i = 0; i < fieldIds.length; i++) {
                if (i > 0) {
                    joined.append(",");
                }
                joined.append(fieldIds[i].toString());
            }
            props.setProperty("fieldIds", joined.toString());
        }
        return props;
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_multipleFields_returnsSumOfAll() throws ServiceException {
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var f1 = UUID.randomUUID();
            var f2 = UUID.randomUUID();
            var f3 = UUID.randomUUID();
            var props = properties(f1, f2, f3);

            when(twinClassFieldService.getDecimalValue(twin, f1, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("1.5"));
            when(twinClassFieldService.getDecimalValue(twin, f2, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("2.5"));
            when(twinClassFieldService.getDecimalValue(twin, f3, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("6"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("10.00", result.getValue());
        }

        @Test
        void deserializeValue_callsServiceOncePerFieldId() throws ServiceException {
            // A sum over N fields looks each up exactly once; verify the per-id lookup, not a bulk call.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var f1 = UUID.randomUUID();
            var f2 = UUID.randomUUID();
            var props = properties(f1, f2);

            when(twinClassFieldService.getDecimalValue(twin, f1, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("3"));
            when(twinClassFieldService.getDecimalValue(twin, f2, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("4"));

            fieldTyper.deserializeValue(props, twinField(twin, classField));

            verify(twinClassFieldService).getDecimalValue(twin, f1, BigDecimal.ZERO);
            verify(twinClassFieldService).getDecimalValue(twin, f2, BigDecimal.ZERO);
            verifyNoMoreInteractions(twinClassFieldService);
        }

        @Test
        void deserializeValue_singleField_returnsThatValue() throws ServiceException {
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var f1 = UUID.randomUUID();
            var props = properties(f1);

            when(twinClassFieldService.getDecimalValue(twin, f1, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("42"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("42.00", result.getValue());
        }

        @Test
        void deserializeValue_emptyFieldSet_returnsZero() throws ServiceException {
            // Empty operand set -> no lookups -> sum stays at BigDecimal.ZERO.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("0.00", result.getValue());
            verifyNoInteractions(twinClassFieldService);
        }

        @Test
        void deserializeValue_missingValueTreatedAsZeroDoesNotBreakSum() throws ServiceException {
            // If a looked-up value resolves to null/default, the sum must still converge (uses ZERO default).
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var f1 = UUID.randomUUID();
            var f2 = UUID.randomUUID();
            var props = properties(f1, f2);

            when(twinClassFieldService.getDecimalValue(twin, f1, BigDecimal.ZERO))
                    .thenReturn(new BigDecimal("5"));
            // f2 resolves to its ZERO default (the real service returns the default for missing values);
            // the sum converges: 5 + 0 = 5.00
            when(twinClassFieldService.getDecimalValue(twin, f2, BigDecimal.ZERO))
                    .thenReturn(BigDecimal.ZERO);

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("5.00", result.getValue());
        }
    }
}
