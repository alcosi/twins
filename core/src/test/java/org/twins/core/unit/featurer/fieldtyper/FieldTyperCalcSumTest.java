package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

    // Stubs findEntitiesSafe to return operand entities for the given field ids, so calculate() can
    // resolve each TwinClassFieldEntity and feed it to resolveDependentDecimalValue.
    private void stubOperandEntities(UUID... fieldIds) throws ServiceException {
        List<TwinClassFieldEntity> entities = new ArrayList<>();
        for (UUID id : fieldIds) {
            entities.add(new TwinClassFieldEntity().setId(id));
        }
        when(twinClassFieldService.findEntitiesSafe(any())).thenReturn(new Kit<>(entities, TwinClassFieldEntity::getId));
    }

    // Stubs the operand value read. A non-null value makes resolveDependentDecimalValue short-circuit
    // before touching storage/featurerService, keeping this a focused unit test of the sum formula.
    private void stubDecimalValue(TwinEntity twin, UUID fieldId, BigDecimal value) throws ServiceException {
        when(twinClassFieldService.getDecimalValue(twin, fieldId, null)).thenReturn(value);
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

            stubOperandEntities(f1, f2, f3);
            stubDecimalValue(twin, f1, new BigDecimal("1.5"));
            stubDecimalValue(twin, f2, new BigDecimal("2.5"));
            stubDecimalValue(twin, f3, new BigDecimal("6"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("10.00", result.getValue());
        }

        @Test
        void deserializeValue_callsServiceOncePerFieldId() throws ServiceException {
            // A sum over N fields resolves each operand exactly once; verify the per-id lookup plus the
            // single findEntitiesSafe call, with no other service interactions.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var f1 = UUID.randomUUID();
            var f2 = UUID.randomUUID();
            var props = properties(f1, f2);

            stubOperandEntities(f1, f2);
            stubDecimalValue(twin, f1, new BigDecimal("3"));
            stubDecimalValue(twin, f2, new BigDecimal("4"));

            fieldTyper.deserializeValue(props, twinField(twin, classField));

            verify(twinClassFieldService).findEntitiesSafe(any());
            verify(twinClassFieldService).getDecimalValue(twin, f1, null);
            verify(twinClassFieldService).getDecimalValue(twin, f2, null);
            verifyNoMoreInteractions(twinClassFieldService);
        }

        @Test
        void deserializeValue_singleField_returnsThatValue() throws ServiceException {
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var f1 = UUID.randomUUID();
            var props = properties(f1);

            stubOperandEntities(f1);
            stubDecimalValue(twin, f1, new BigDecimal("42"));

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("42.00", result.getValue());
        }

        @Test
        void deserializeValue_emptyFieldSet_returnsZero() throws ServiceException {
            // Empty operand set -> findEntitiesSafe returns an empty kit -> no per-id lookups -> sum is 0.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var props = properties();

            stubOperandEntities();

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("0.00", result.getValue());
            verify(twinClassFieldService).findEntitiesSafe(any());
            verifyNoMoreInteractions(twinClassFieldService);
        }

        @Test
        void deserializeValue_zeroOperandConvergesToCorrectSum() throws ServiceException {
            // A zero-valued operand resolves non-null, so it is added as-is without breaking the sum.
            var twin = new TwinEntity();
            var classField = new TwinClassFieldEntity();
            classField.setId(UUID.randomUUID());
            var f1 = UUID.randomUUID();
            var f2 = UUID.randomUUID();
            var props = properties(f1, f2);

            stubOperandEntities(f1, f2);
            stubDecimalValue(twin, f1, new BigDecimal("5"));
            stubDecimalValue(twin, f2, BigDecimal.ZERO);

            FieldValueText result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals("5.00", result.getValue());
        }
    }
}
