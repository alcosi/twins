package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.enums.twinclass.FieldCheckboxType;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperBooleanV1Test extends BaseUnitTest {

    private FieldTyperBooleanV1 fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperBooleanV1();
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties() {
        // Intended defaults: checkboxType defaults to TOGGLE (per @FeaturerParam defaultValue).
        var props = new Properties();
        props.setProperty("checkboxType", "TOGGLE");
        props.setProperty("defaultValue", "false");
        return props;
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_dbEntityPresent_returnsEntityValue() throws ServiceException {
            // Intended: when a stored boolean value exists, deserialize returns it as-is.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var dbEntity = new TwinFieldBooleanEntity().setValue(true).setTwinClassField(classField);

            FieldValueBoolean result = fieldTyper.deserializeValue(properties(), twinField(twin, classField), dbEntity);

            assertEquals(true, result.getValue());
        }

        @Test
        void deserializeValue_dbEntityNull_returnsDefaultValue() throws ServiceException {
            // Intended: when no stored record exists, the phantom default value (false here) is returned.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());

            FieldValueBoolean result = fieldTyper.deserializeValue(properties(), twinField(twin, classField), null);

            assertEquals(false, result.getValue());
        }

        @Test
        void deserializeValue_dbEntityNull_customDefaultReturnsCustomDefault() throws ServiceException {
            // Intended: defaultValue param is honored when the db record is absent.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var props = properties();
            props.setProperty("defaultValue", "true");

            FieldValueBoolean result = fieldTyper.deserializeValue(props, twinField(twin, classField), null);

            assertEquals(true, result.getValue());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_presentValue_writesValueToEntity() throws ServiceException {
            // Intended: a present value is written through to the stored entity.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = new TwinFieldBooleanEntity()
                    .setValue(false)
                    .setTwinClassField(classField)
                    .setTwin(new TwinEntity().setId(UUID.randomUUID()));
            var value = new FieldValueBoolean(classField).setValue(true);
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), dbEntity, value, collector);

            assertEquals(true, dbEntity.getValue());
        }

        @Test
        void serializeValue_undefinedValue_fallsBackToDefault() throws ServiceException {
            // Intended: when the value is not present (json omitted), the default is used and persisted.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = new TwinFieldBooleanEntity()
                    .setValue(null)
                    .setTwinClassField(classField)
                    .setTwin(new TwinEntity().setId(UUID.randomUUID()));
            var value = new FieldValueBoolean(classField); // state UNDEFINED, no setValue call
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), dbEntity, value, collector);

            // defaultValue=false; oldValue(null) differs -> written
            assertEquals(false, dbEntity.getValue());
        }

        @Test
        void serializeValue_sameValue_doesNotMutateEntity() throws ServiceException {
            // Intended: when newValue equals oldValue, detectValueChange reports no change and the entity is untouched.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = new TwinFieldBooleanEntity().setValue(true).setTwinClassField(classField);
            var value = new FieldValueBoolean(classField).setValue(true);
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), dbEntity, value, collector);

            assertEquals(true, dbEntity.getValue());
            assertFalse(collector.hasChanges());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsCheckboxTypeFromParams() throws ServiceException {
            // Intended: checkboxType is extracted from the typer params and propagated onto the descriptor.
            var classField = new TwinClassFieldEntity();
            var props = properties();
            props.setProperty("checkboxType", "STANDARD");

            var descriptor = fieldTyper.getFieldDescriptor(classField, props);

            assertInstanceOf(FieldDescriptorBoolean.class, descriptor);
            assertEquals(FieldCheckboxType.STANDARD, ((FieldDescriptorBoolean) descriptor).checkboxType());
        }
    }
}
