package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.enums.twinclass.FieldTextEditorType;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperTextNonIndexedFieldTest extends BaseUnitTest {

    private FieldTyperTextNonIndexedField fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperTextNonIndexedField();
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties() {
        var props = new Properties();
        props.setProperty("regexp", "(?s).*");
        props.setProperty("editorType", "PLAIN");
        return props;
    }

    private TwinFieldSimpleNonIndexedEntity dbEntity(TwinClassFieldEntity classField, String value) {
        // twin non-null required: markForInvalidate keys by TwinFieldSimpleNonIndexedEntity.getTwin().
        return new TwinFieldSimpleNonIndexedEntity()
                .setTwinClassField(classField)
                .setTwinClassFieldId(classField.getId())
                .setValue(value)
                .setTwin(new TwinEntity().setId(UUID.randomUUID()));
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_dbEntityWithValue_returnsValue() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, "long body text");

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField), dbEntity);

            assertEquals("long body text", result.getValue());
        }

        @Test
        void deserializeValue_nullDbEntity_returnsNullValue() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField), null);

            assertNull(result.getValue());
        }
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_newValue_writesValueToEntity() throws ServiceException {
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
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, "same");
            var value = new FieldValueText(classField).setValue("same");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), dbEntity, value, collector);

            assertFalse(collector.hasChanges());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_propagatesRegexpAndEditorType() throws ServiceException {
            var classField = new TwinClassFieldEntity();
            var props = properties();
            props.setProperty("regexp", "^x+$");
            props.setProperty("editorType", "HTML");

            var descriptor = fieldTyper.getFieldDescriptor(classField, props);

            assertInstanceOf(FieldDescriptorText.class, descriptor);
            assertEquals("^x+$", ((FieldDescriptorText) descriptor).regExp());
            assertEquals(FieldTextEditorType.HTML, ((FieldDescriptorText) descriptor).editorType());
        }
    }
}
