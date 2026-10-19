package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twin.TwinFieldDecimalIncrement;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperDecimalIncrementTest extends BaseUnitTest {

    private FieldTyperDecimalIncrement fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperDecimalIncrement();
    }

    private Properties properties() {
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

    private TwinFieldDecimalEntity decimalEntity(TwinClassFieldEntity classField) {
        // Not used by serialize (increment ignores the stored entity) but required by the signature.
        return new TwinFieldDecimalEntity()
                .setTwinClassFieldId(classField.getId())
                .setTwinClassField(classField);
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_positiveIncrement_recordsPositiveDelta() throws ServiceException {
            // Intended: "+5" parses to delta +5 and is recorded against the twin + class field.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField).setValue("+5");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, decimalEntity(classField), value, collector);

            var increments = collector.getSaveEntities(TwinFieldDecimalIncrement.class);
            assertEquals(1, increments.size());
            var inc = increments.iterator().next();
            assertEquals(new BigDecimal("5"), inc.getDelta());
            assertEquals(twin.getId(), inc.getTwinId());
            assertEquals(classField.getId(), inc.getTwinClassFieldId());
        }

        @Test
        void serializeValue_negativeIncrement_recordsNegativeDelta() throws ServiceException {
            // Intended: "-3" parses to delta -3 (sign preserved).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField).setValue("-3");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, decimalEntity(classField), value, collector);

            var increments = collector.getSaveEntities(TwinFieldDecimalIncrement.class);
            assertEquals(1, increments.size());
            assertEquals(new BigDecimal("-3"), increments.iterator().next().getDelta());
        }

        @Test
        void serializeValue_fractionalIncrement_recordsFractionalDelta() throws ServiceException {
            // Intended: "+1.5" is a valid increment with fractional delta.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField).setValue("+1.5");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, decimalEntity(classField), value, collector);

            var increments = collector.getSaveEntities(TwinFieldDecimalIncrement.class);
            assertEquals(1, increments.size());
            assertEquals(new BigDecimal("1.5"), increments.iterator().next().getDelta());
        }

        @Test
        void serializeValue_undefinedValue_recordsNothing() throws ServiceException {
            // Intended: undefined value (never set) short-circuits, no increment recorded.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField); // UNDEFINED
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, decimalEntity(classField), value, collector);

            assertTrue(collector.getSaveEntities(TwinFieldDecimalIncrement.class).isEmpty());
        }
    }

    @Nested
    class Validate {

        @Test
        void validate_positiveFormat_isValid() throws ServiceException {
            // Intended: "+N" matches INCREMENT_PATTERN.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField).setValue("+10");

            var result = fieldTyper.validate(properties(), twin, value);

            assertTrue(result.isValid());
        }

        @Test
        void validate_negativeFormat_isValid() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField).setValue("-10");

            var result = fieldTyper.validate(properties(), twin, value);

            assertTrue(result.isValid());
        }

        @Test
        void validate_zero_isValid() throws ServiceException {
            // Intended: bare "0" is allowed by the pattern.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField).setValue("0");

            var result = fieldTyper.validate(properties(), twin, value);

            assertTrue(result.isValid());
        }

        @Test
        void validate_emptyValue_isValid() throws ServiceException {
            // Intended: empty/null value short-circuits to VALID (no increment requested).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField).setValue("");

            var result = fieldTyper.validate(properties(), twin, value);

            assertTrue(result.isValid());
        }

        @Test
        void validate_barePositiveNumber_isInvalid() throws ServiceException {
            // Intended: "5" (no sign) does NOT match INCREMENT_PATTERN (requires sign, except "0").
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueText(classField).setValue("5");

            var result = fieldTyper.validate(properties(), twin, value);

            assertFalse(result.isValid());
        }

        @Test
        void validate_deltaOutOfMinRange_isInvalid() throws ServiceException {
            // Intended: a syntactically valid increment whose delta violates min/max is rejected.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var props = properties();
            props.setProperty("min", "0");
            var value = new FieldValueText(classField).setValue("-100");

            var result = fieldTyper.validate(props, twin, value);

            assertFalse(result.isValid());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsNumericDescriptorWithDecimalPlaces() throws ServiceException {
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties());

            assertInstanceOf(FieldDescriptorNumeric.class, descriptor);
            assertEquals(2, ((FieldDescriptorNumeric) descriptor).decimalPlaces());
        }
    }
}
