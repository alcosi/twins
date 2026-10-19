package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
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

import java.util.List;
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

    private TwinEntity twinWithKit(TwinClassFieldEntity classField, String value) {
        var twin = new TwinEntity().setId(UUID.randomUUID());
        var dbEntity = new TwinFieldSimpleEntity()
                .setTwinClassField(classField)
                .setTwinClassFieldId(classField.getId())
                .setTwin(twin)
                .setValue(value);
        twin.setTwinFieldSimpleKit(new Kit<>(List.of(dbEntity), TwinFieldSimpleEntity::getTwinClassFieldId));
        return twin;
    }

    private TwinEntity twinWithoutEntity(TwinClassFieldEntity classField) {
        var twin = new TwinEntity().setId(UUID.randomUUID());
        twin.setTwinFieldSimpleKit(new Kit<>(List.of(), TwinFieldSimpleEntity::getTwinClassFieldId));
        return twin;
    }

    private TwinFieldSimpleEntity firstInKit(TwinEntity twin) {
        return twin.getTwinFieldSimpleKit().iterator().next();
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_validSixDigitHex_persists() throws ServiceException {
            // Intended: a leading '#' plus exactly 6 hex digits matches HEX_PATTERN and is stored verbatim.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);
            var value = new FieldValueColorHEX(classField).setValue("#1A2B3C");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertEquals("#1A2B3C", firstInKit(twin).getValue());
            assertTrue(collector.hasChanges());
        }

        @Test
        void serializeValue_threeDigitShorthand_throws() {
            // Intended: the pattern requires exactly 6 hex digits, so 3-digit shorthand (#FFF) is rejected.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);
            var value = new FieldValueColorHEX(classField).setValue("#FFF");
            var collector = new TwinChangesCollector(false);

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(), twin, value, collector));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void serializeValue_missingHash_throws() {
            // Intended: HEX_PATTERN anchors on '#'; bare hex is invalid.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);
            var value = new FieldValueColorHEX(classField).setValue("1A2B3C");
            var collector = new TwinChangesCollector(false);

            assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(), twin, value, collector));
        }

        @Test
        void serializeValue_sevenHexDigits_throws() {
            // Intended: {6} is exact -> 7 hex digits does not match.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);
            var value = new FieldValueColorHEX(classField).setValue("#1122334");
            var collector = new TwinChangesCollector(false);

            assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(), twin, value, collector));
        }

        @Test
        void serializeValue_sameValue_doesNotMutateEntity() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, "#FFFFFF");
            var value = new FieldValueColorHEX(classField).setValue("#FFFFFF");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertFalse(collector.hasChanges());
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_dbEntityWithValue_returnsValue() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, "#000000");

            FieldValueColorHEX result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals("#000000", result.getValue());
        }

        @Test
        void deserializeValue_nullDbEntity_returnsNullValue() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithoutEntity(classField);

            FieldValueColorHEX result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

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
