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
import org.twins.core.enums.twinclass.FieldTextEditorType;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

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

    private TwinFieldSimpleEntity dbEntity(TwinClassFieldEntity classField, String value) {
        // twin MUST be non-null: serializeValue -> collectIfChangedWithNullifySupport ->
        // markForInvalidate keys invalidationMap by TwinFieldSimpleEntity.getTwin() (ConcurrentHashMap, NPE on null).
        return new TwinFieldSimpleEntity()
                .setTwinClassField(classField)
                .setTwinClassFieldId(classField.getId())
                .setValue(value)
                .setTwin(new TwinEntity().setId(UUID.randomUUID()));
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_dbEntityWithValue_returnsValue() throws ServiceException {
            // Intended: stored text round-trips back unchanged.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, "hello");

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField), dbEntity);

            assertEquals("hello", result.getValue());
        }

        @Test
        void deserializeValue_nullDbEntity_returnsNullValue() throws ServiceException {
            // Intended: no stored row -> value is null (not empty string).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField), null);

            assertNull(result.getValue());
        }

        @Test
        void deserializeValue_dbEntityWithNullValue_returnsNullValue() throws ServiceException {
            // Intended: row exists but value column null -> treated as absent.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, null);

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField), dbEntity);

            assertNull(result.getValue());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_newValue_writesValueToEntity() throws ServiceException {
            // Intended: changed value is written through to the stored entity.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, "old");
            var value = new FieldValueText(classField).setValue("new");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), dbEntity, value, collector);

            assertEquals("new", dbEntity.getValue());
            assertTrue(collector.hasChanges());
        }

        @Test
        void serializeValue_sameValue_doesNotMutateEntity() throws ServiceException {
            // Intended: newValue == oldValue -> collectIfChangedWithNullifySupport returns false -> no write.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, "same");
            var value = new FieldValueText(classField).setValue("same");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), dbEntity, value, collector);

            assertEquals("same", dbEntity.getValue());
            assertFalse(collector.hasChanges());
        }

        @Test
        void serializeValue_nullNewValue_clearsEntity() throws ServiceException {
            // Intended: WithNullifySupport variant treats null as a real change -> writes null (clear).
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, "was-here");
            var value = new FieldValueText(classField).setValue(null); // state CLEARED, value null
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), dbEntity, value, collector);

            assertNull(dbEntity.getValue());
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
