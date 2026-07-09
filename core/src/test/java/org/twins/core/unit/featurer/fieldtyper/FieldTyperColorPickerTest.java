package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorColorPicker;
import org.twins.core.featurer.fieldtyper.value.FieldValueColorHEX;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperColorPickerTest extends BaseUnitTest {

    private FieldTyperColorPicker fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperColorPicker();
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties() {
        return new Properties();
    }

    private TwinFieldSimpleEntity dbEntity(TwinClassFieldEntity classField, String value) {
        return new TwinFieldSimpleEntity()
                .setTwinClassField(classField)
                .setTwinClassFieldId(classField.getId())
                .setValue(value)
                .setTwin(new TwinEntity().setId(UUID.randomUUID()));
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_validSixDigitHex_persists() throws ServiceException {
            // Intended: a leading '#' plus exactly 6 hex digits matches HEX_PATTERN and is stored verbatim.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, null);
            var value = new FieldValueColorHEX(classField).setValue("#1A2B3C");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), dbEntity, value, collector);

            assertEquals("#1A2B3C", dbEntity.getValue());
            assertTrue(collector.hasChanges());
        }

        @Test
        void serializeValue_threeDigitShorthand_throws() {
            // Intended: the pattern requires exactly 6 hex digits, so 3-digit shorthand (#FFF) is rejected.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, null);
            var value = new FieldValueColorHEX(classField).setValue("#FFF");
            var collector = new TwinChangesCollector(false);

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(), dbEntity, value, collector));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void serializeValue_missingHash_throws() {
            // Intended: HEX_PATTERN anchors on '#'; bare hex is invalid.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, null);
            var value = new FieldValueColorHEX(classField).setValue("1A2B3C");
            var collector = new TwinChangesCollector(false);

            assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(), dbEntity, value, collector));
        }

        @Test
        void serializeValue_sevenHexDigits_throws() {
            // Intended: {6} is exact -> 7 hex digits does not match.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, null);
            var value = new FieldValueColorHEX(classField).setValue("#1122334");
            var collector = new TwinChangesCollector(false);

            assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(), dbEntity, value, collector));
        }

        @Test
        void serializeValue_sameValue_doesNotMutateEntity() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, "#FFFFFF");
            var value = new FieldValueColorHEX(classField).setValue("#FFFFFF");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), dbEntity, value, collector);

            assertFalse(collector.hasChanges());
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_dbEntityWithValue_returnsValue() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, "#000000");

            FieldValueColorHEX result = fieldTyper.deserializeValue(properties(), twinField(twin, classField), dbEntity);

            assertEquals("#000000", result.getValue());
        }

        @Test
        void deserializeValue_nullDbEntity_returnsNullValue() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());

            FieldValueColorHEX result = fieldTyper.deserializeValue(properties(), twinField(twin, classField), null);

            assertNull(result.getValue());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsColorPickerDescriptor() throws ServiceException {
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties());

            assertInstanceOf(FieldDescriptorColorPicker.class, descriptor);
        }
    }
}
