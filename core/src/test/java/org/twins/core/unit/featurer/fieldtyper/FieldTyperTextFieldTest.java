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
import org.twins.core.enums.twinclass.FieldTextEditorType;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperTextFieldTest extends BaseUnitTest {

    private FieldTyperTextField fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperTextField();
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties() {
        // Defaults mirror the @FeaturerParam defaultValue annotations.
        var props = new Properties();
        props.setProperty("regexp", "(?s).*");
        props.setProperty("editorType", "PLAIN");
        props.setProperty("unique", "false");
        return props;
    }

    /**
     * dbEntity MUST carry a non-null twin: TwinChangesCollector.detectChangesHelper -> syncRelations ->
     * syncFieldKitAndInvalidate reads TwinFieldSimpleEntity.getTwin() and twin.getTwinFieldSimpleKit()
     * (ConcurrentHashMap, NPE on null). The kit is what FieldTyperSingleValue.resolveTwinFieldEntity reads from.
     */
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
    class DeserializeValue {

        @Test
        void deserializeValue_dbEntityWithValue_returnsValue() throws ServiceException {
            // Intended: stored text round-trips back unchanged.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, "hello");

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals("hello", result.getValue());
        }

        @Test
        void deserializeValue_nullDbEntity_returnsNullValue() throws ServiceException {
            // Intended: no stored row -> value is null (not empty string).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithoutEntity(classField);

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertNull(result.getValue());
        }

        @Test
        void deserializeValue_dbEntityWithNullValue_returnsNullValue() throws ServiceException {
            // Intended: row exists but value column null -> treated as absent.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertNull(result.getValue());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_newValue_writesValueToEntity() throws ServiceException {
            // Intended: changed value is written through to the stored entity.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, "old");
            var value = new FieldValueText(classField).setValue("new");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertEquals("new", firstInKit(twin).getValue());
            assertTrue(collector.hasChanges());
        }

        @Test
        void serializeValue_sameValue_doesNotMutateEntity() throws ServiceException {
            // Intended: newValue == oldValue -> collectIfChangedWithNullifySupport returns false -> no write.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, "same");
            var value = new FieldValueText(classField).setValue("same");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertEquals("same", firstInKit(twin).getValue());
            assertFalse(collector.hasChanges());
        }

        @Test
        void serializeValue_nullNewValue_clearsEntity() throws ServiceException {
            // Intended: CLEARED value -> FieldTyperSimple.onCleared -> detectValueChange(null) -> nullify
            // (the row is kept, value column set to null).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, "was-here");
            var value = new FieldValueText(classField).setValue(null); // state CLEARED, value null
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertNull(firstInKit(twin).getValue());
            assertTrue(collector.hasChanges());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_propagatesRegexpAndEditorType() throws ServiceException {
            // Intended: regexp + editorType are extracted from params and set on the descriptor.
            var classField = new TwinClassFieldEntity();
            var props = properties();
            props.setProperty("regexp", "^[a-z]+$");
            props.setProperty("editorType", "MARKDOWN_GITHUB");

            var descriptor = fieldTyper.getFieldDescriptor(classField, props);

            assertInstanceOf(FieldDescriptorText.class, descriptor);
            assertEquals("^[a-z]+$", ((FieldDescriptorText) descriptor).regExp());
            assertEquals(FieldTextEditorType.MARKDOWN_GITHUB, ((FieldDescriptorText) descriptor).editorType());
        }

        @Test
        void getFieldDescriptor_uniqueTrueMarksBackendValidated() throws ServiceException {
            // Intended: unique=true flips backendValidated on (uniqueness enforced server-side).
            var classField = new TwinClassFieldEntity();
            var props = properties();
            props.setProperty("unique", "true");

            var descriptor = fieldTyper.getFieldDescriptor(classField, props);

            assertTrue(((FieldDescriptorText) descriptor).backendValidated());
        }

        @Test
        void getFieldDescriptor_uniqueFalseLeavesBackendValidatedOff() throws ServiceException {
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties());

            assertFalse(((FieldDescriptorText) descriptor).backendValidated());
        }
    }
}
