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
}
