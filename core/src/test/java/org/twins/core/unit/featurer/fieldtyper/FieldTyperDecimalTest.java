package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;

class FieldTyperDecimalTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FieldTyperDecimal fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperDecimal();
        setField(fieldTyper, "twinService", twinService);
        // lenient: only deserializeValue calls loadTwinFields; validate/getFieldDescriptor do not.
        lenient().doNothing().when(twinService).loadTwinFields(any(TwinEntity.class));
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

    private Properties properties() {
        // Defaults mirror @FeaturerParam defaultValue annotations on FieldTyperNumeric.
        var props = new Properties();
        props.setProperty("min", "-2147483648");
        props.setProperty("max", "2147483647");
        props.setProperty("step", "1");
        props.setProperty("thousandSeparator", " ");
        props.setProperty("decimalSeparator", ".");
        props.setProperty("decimalPlaces", "2");
        props.setProperty("round", "true");
        return props;
    }

    private TwinFieldDecimalEntity decimalEntity(TwinClassFieldEntity classField, BigDecimal value) {
        return new TwinFieldDecimalEntity()
                .setTwinClassFieldId(classField.getId())
                .setTwinClassField(classField)
                .setValue(value)
                .setTwin(new TwinEntity().setId(UUID.randomUUID()));
    }

    private Properties propertiesWithIncrement() {
        // Opt-in to the +/- delta path (mirrors a field configured with allowIncrementValue=true).
        var props = properties();
        props.setProperty("allowIncrementValue", "true");
        return props;
    }

    private TwinEntity twinWithDecimalField(TwinClassFieldEntity classField, BigDecimal value) {
        var twin = new TwinEntity().setId(UUID.randomUUID());
        twin.setTwinFieldDecimalKit(new Kit<>(
                List.of(decimalEntity(classField, value)),
                TwinFieldDecimalEntity::getTwinClassFieldId));
        return twin;
    }

    private TwinEntity twinWithoutDecimalField(TwinClassFieldEntity classField) {
        // New field with no stored row yet — resolveTwinFieldEntity returns null (null-safe path).
        var twin = new TwinEntity().setId(UUID.randomUUID());
        twin.setTwinFieldDecimalKit(new Kit<>(List.of(), TwinFieldDecimalEntity::getTwinClassFieldId));
        return twin;
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_storedValue_rendersAtConfiguredScale() throws ServiceException {
            // Intended: stored BigDecimal is rescaled to decimalPlaces (2) and rendered as plain text.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            twin.setTwinFieldDecimalKit(new Kit<>(
                    List.of(decimalEntity(classField, new BigDecimal("15.5"))),
                    TwinFieldDecimalEntity::getTwinClassFieldId));

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals("15.50", result.getValue());
        }

        @Test
        void deserializeValue_noStoredRow_returnsNullText() throws ServiceException {
            // Intended: no stored entity -> value text is null.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            twin.setTwinFieldDecimalKit(new Kit<>(List.of(), TwinFieldDecimalEntity::getTwinClassFieldId));

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertNull(result.getValue());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_propagatesAllNumericParams() throws ServiceException {
            // Intended: min/max/step/separators/decimalPlaces/round are all forwarded to the descriptor.
            var classField = new TwinClassFieldEntity();
            var props = properties();
            props.setProperty("min", "0");
            props.setProperty("max", "100");
            props.setProperty("decimalPlaces", "3");

            var descriptor = fieldTyper.getFieldDescriptor(classField, props);

            assertInstanceOf(FieldDescriptorNumeric.class, descriptor);
            var numeric = (FieldDescriptorNumeric) descriptor;
            assertEquals(0.0, numeric.min());
            assertEquals(100.0, numeric.max());
            assertEquals(3, numeric.decimalPlaces());
            assertEquals(".", numeric.decimalSeparator());
            assertEquals(" ", numeric.thousandSeparator());
            assertTrue(numeric.round());
        }
    }

    @Nested
    class Validate {

        @Test
        void validate_wellFormedNumberWithinRange_isValid() throws ServiceException {
            // Intended: "12.5" with decimalPlaces=2, min/max wide -> valid.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField).setValue("12.5");

            var result = fieldTyper.validate(properties(), twin, value);

            assertTrue(result.isValid());
        }

        @Test
        void validate_valueBelowMin_isInvalid() throws ServiceException {
            // Intended: value below the configured min is rejected.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var props = properties();
            props.setProperty("min", "10");
            var value = new FieldValueText(classField).setValue("5");

            var result = fieldTyper.validate(props, twin, value);

            assertFalse(result.isValid());
        }

        @Test
        void validate_valueAboveMax_isInvalid() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var props = properties();
            props.setProperty("max", "10");
            var value = new FieldValueText(classField).setValue("50");

            var result = fieldTyper.validate(props, twin, value);

            assertFalse(result.isValid());
        }

        @Test
        void validate_tooManyDecimalPlacesWithRoundDisabled_isInvalid() throws ServiceException {
            // Intended: round=false + value exceeding decimalPlaces -> rejected (not silently truncated).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var props = properties();
            props.setProperty("decimalPlaces", "2");
            props.setProperty("round", "false");
            var value = new FieldValueText(classField).setValue("1.234");

            var result = fieldTyper.validate(props, twin, value);

            assertFalse(result.isValid());
        }

        @Test
        void validate_tooManyDecimalPlacesWithRoundEnabled_isValid() throws ServiceException {
            // Intended: round=true truncates 1.234 to 1.23 in range -> valid.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var props = properties();
            props.setProperty("decimalPlaces", "2");
            var value = new FieldValueText(classField).setValue("1.234");

            var result = fieldTyper.validate(props, twin, value);

            assertTrue(result.isValid());
        }

        @Test
        void validate_nonNumeric_isInvalid() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField).setValue("abc");

            var result = fieldTyper.validate(properties(), twin, value);

            assertFalse(result.isValid());
        }

        @Test
        void validate_customThousandSeparator_isValid() throws ServiceException {
            // Intended: thousand separators (main + extra) are stripped before parsing.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var props = properties();
            props.setProperty("thousandSeparator", ",");
            props.setProperty("decimalSeparator", ".");
            var value = new FieldValueText(classField).setValue("1,234.5");

            var result = fieldTyper.validate(props, twin, value);

            assertTrue(result.isValid());
        }
    }

    @Nested
    class ProcessIncrementedValue {

        @Test
        void positiveDelta_addsToCurrentValue() throws ServiceException {
            // Intended: "+5" folds into current 10 -> 15, scaled to decimalPlaces=2.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var entity = decimalEntity(classField, new BigDecimal("10.00"));
            var result = fieldTyper.processIncrementedValue(propertiesWithIncrement(), entity, new BigDecimal("5"));
            assertEquals(0, new BigDecimal("15.00").compareTo(result));
        }

        @Test
        void negativeDelta_subtractsFromCurrentValue() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var entity = decimalEntity(classField, new BigDecimal("10.00"));
            var result = fieldTyper.processIncrementedValue(propertiesWithIncrement(), entity, new BigDecimal("-3"));
            assertEquals(0, new BigDecimal("7.00").compareTo(result));
        }

        @Test
        void nullCurrentValue_deltaBecomesStartingValue() throws ServiceException {
            // Intended: field with no value yet -> result is the delta alone, scaled.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var entity = decimalEntity(classField, null);
            var result = fieldTyper.processIncrementedValue(propertiesWithIncrement(), entity, new BigDecimal("5"));
            assertEquals(0, new BigDecimal("5.00").compareTo(result));
        }

        @Test
        void fractionalDelta_scalesResultToDecimalPlaces() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var entity = decimalEntity(classField, new BigDecimal("1.20"));
            var result = fieldTyper.processIncrementedValue(propertiesWithIncrement(), entity, new BigDecimal("1.25"));
            assertEquals(0, new BigDecimal("2.45").compareTo(result));
        }

        @Test
        void roundEnabled_truncatesExcessPrecision() throws ServiceException {
            // Intended: round=true (default) truncates 1.005 -> 1.00 at decimalPlaces=2.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var entity = decimalEntity(classField, new BigDecimal("1.00"));
            var result = fieldTyper.processIncrementedValue(propertiesWithIncrement(), entity, new BigDecimal("0.005"));
            assertEquals(0, new BigDecimal("1.00").compareTo(result));
        }

        @Test
        void resultAboveMax_throws() {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var props = propertiesWithIncrement();
            props.setProperty("max", "10");
            var entity = decimalEntity(classField, new BigDecimal("8.00"));
            assertThrows(ServiceException.class,
                    () -> fieldTyper.processIncrementedValue(props, entity, new BigDecimal("5")));
        }

        @Test
        void resultBelowMin_throws() {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var props = propertiesWithIncrement();
            props.setProperty("min", "0");
            var entity = decimalEntity(classField, new BigDecimal("2.00"));
            assertThrows(ServiceException.class,
                    () -> fieldTyper.processIncrementedValue(props, entity, new BigDecimal("-5")));
        }
    }

    @Nested
    class ValidateIncrement {

        @Test
        void allowIncrementOff_signedValueTreatedAsAbsolute() throws ServiceException {
            // Intended: default allowIncrementValue=false -> "+5" parsed as absolute 5 (no delta folding).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithDecimalField(classField, new BigDecimal("100.00"));
            var value = new FieldValueText(classField).setValue("+5");
            assertTrue(fieldTyper.validate(properties(), twin, value).isValid());
        }

        @Test
        void resultWithinRange_isValid() throws ServiceException {
            // Intended: current=2, +5 -> result 7 within max=10 -> valid (validates the RESULT, not the delta).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var props = propertiesWithIncrement();
            props.setProperty("max", "10");
            var twin = twinWithDecimalField(classField, new BigDecimal("2.00"));
            var value = new FieldValueText(classField).setValue("+5");
            assertTrue(fieldTyper.validate(props, twin, value).isValid());
        }

        @Test
        void resultExceedsMax_isInvalid() throws ServiceException {
            // REGRESSION (panel B4): validate previously said VALID for "+5" (5<=10), ignoring current+delta=13>10.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var props = propertiesWithIncrement();
            props.setProperty("max", "10");
            var twin = twinWithDecimalField(classField, new BigDecimal("8.00"));
            var value = new FieldValueText(classField).setValue("+5");
            assertFalse(fieldTyper.validate(props, twin, value).isValid());
        }

        @Test
        void bareZero_isAbsoluteSet_notIncrement() throws ServiceException {
            // REGRESSION (panel B3): "0" must zero the field, not be a no-op increment (validated via absolute path).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithDecimalField(classField, new BigDecimal("42.50"));
            var value = new FieldValueText(classField).setValue("0");
            assertTrue(fieldTyper.validate(propertiesWithIncrement(), twin, value).isValid());
        }

        @Test
        void signedZero_isAbsoluteSet_notIncrement() throws ServiceException {
            // Intended: "+0"/"-0" are zero deltas -> absolute set, not no-op increment.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithDecimalField(classField, new BigDecimal("42.50"));
            assertTrue(fieldTyper.validate(propertiesWithIncrement(), twin, new FieldValueText(classField).setValue("+0")).isValid());
            assertTrue(fieldTyper.validate(propertiesWithIncrement(), twin, new FieldValueText(classField).setValue("-0")).isValid());
        }

        @Test
        void unsignedNumber_isAbsolute_notIncrement() throws ServiceException {
            // Intended: "5" (no sign) does not match the increment pattern -> absolute path.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithDecimalField(classField, new BigDecimal("100.00"));
            var value = new FieldValueText(classField).setValue("5");
            assertTrue(fieldTyper.validate(propertiesWithIncrement(), twin, value).isValid());
        }

        @Test
        void incrementOnNewField_withinRange_isValid() throws ServiceException {
            // Intended: no stored row (entity==null) -> delta is the starting value, range-checked. No NPE.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var props = propertiesWithIncrement();
            props.setProperty("min", "0");
            var twin = twinWithoutDecimalField(classField);
            var value = new FieldValueText(classField).setValue("+5");
            assertTrue(fieldTyper.validate(props, twin, value).isValid());
        }

        @Test
        void incrementOnNewField_belowMin_isInvalid() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var props = propertiesWithIncrement();
            props.setProperty("min", "0");
            var twin = twinWithoutDecimalField(classField);
            var value = new FieldValueText(classField).setValue("-5");
            assertFalse(fieldTyper.validate(props, twin, value).isValid());
        }
    }
}
