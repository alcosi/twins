package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
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

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperBooleanTest extends BaseUnitTest {

    private FieldTyperBoolean fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperBoolean();
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties() {
        // Intended defaults: checkboxType defaults to TOGGLE, defaultValue to false (per @FeaturerParam defaultValue).
        var props = new Properties();
        props.setProperty("checkboxType", "TOGGLE");
        props.setProperty("defaultValue", "false");
        return props;
    }

    /**
     * dbEntity MUST carry a non-null twin: TwinChangesCollector.detectChangesHelper -> syncRelations ->
     * syncFieldKitAndInvalidate reads TwinFieldBooleanEntity.getTwin() and twin.getTwinFieldBooleanKit()
     * (ConcurrentHashMap, NPE on null). The kit is what FieldTyperSingleValue.resolveTwinFieldEntity reads from.
     */
    private TwinEntity twinWithKit(TwinClassFieldEntity classField, Boolean dbValue) {
        var twin = new TwinEntity().setId(UUID.randomUUID());
        TwinFieldBooleanEntity dbEntity = null;
        if (dbValue != null) {
            dbEntity = new TwinFieldBooleanEntity()
                    .setTwinClassField(classField)
                    .setTwinClassFieldId(classField.getId())
                    .setTwin(twin)
                    .setValue(dbValue);
        }
        twin.setTwinFieldBooleanKit(new Kit<>(
                dbValue != null ? List.of(dbEntity) : List.of(),
                TwinFieldBooleanEntity::getTwinClassFieldId));
        return twin;
    }

    private TwinFieldBooleanEntity firstInKit(TwinEntity twin) {
        return twin.getTwinFieldBooleanKit().iterator().next();
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_dbEntityPresent_returnsEntityValue() throws ServiceException {
            // Intended: when a stored boolean value exists, deserialize returns it as-is.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, true);

            FieldValueBoolean result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals(true, result.getValue());
        }

        @Test
        void deserializeValue_dbEntityNull_returnsDefaultValue() throws ServiceException {
            // Intended: when no stored record exists, the phantom default value (false here) is returned.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);

            FieldValueBoolean result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals(false, result.getValue());
        }

        @Test
        void deserializeValue_dbEntityNull_customDefaultReturnsCustomDefault() throws ServiceException {
            // Intended: defaultValue param is honored when the db record is absent.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);
            var props = properties();
            props.setProperty("defaultValue", "true");

            FieldValueBoolean result = fieldTyper.deserializeValue(props, twinField(twin, classField));

            assertEquals(true, result.getValue());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_presentValue_writesValueToEntity() throws ServiceException {
            // Intended: a present value is written through to the stored entity.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, false);
            var value = new FieldValueBoolean(classField).setValue(true);
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertEquals(true, firstInKit(twin).getValue());
        }

        @Test
        void serializeValue_sameValue_doesNotMutateEntity() throws ServiceException {
            // Intended: when newValue equals oldValue, detectValueChange reports no change and the entity is untouched.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, true);
            var value = new FieldValueBoolean(classField).setValue(true);
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertEquals(true, firstInKit(twin).getValue());
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
