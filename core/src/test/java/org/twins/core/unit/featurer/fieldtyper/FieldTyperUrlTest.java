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
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUrl;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldTyperUrlTest extends BaseUnitTest {

    private FieldTyperUrl fieldTyper;

    @BeforeEach
    void setUp() {
        fieldTyper = new FieldTyperUrl();
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
        void serializeValue_validHttpUrl_persists() throws ServiceException {
            // Intended: a well-formed http(s) URL with a host passes UrlUtils.isValid and is stored.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, null);
            var value = new FieldValueText(classField).setValue("https://example.com/path");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), dbEntity, value, collector);

            assertEquals("https://example.com/path", dbEntity.getValue());
            assertTrue(collector.hasChanges());
        }

        @Test
        void serializeValue_nonHttpScheme_throws() {
            // Intended: UrlUtils.isValid only accepts http/https schemes -> ftp rejected.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, null);
            var value = new FieldValueText(classField).setValue("ftp://example.com/file");
            var collector = new TwinChangesCollector(false);

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(), dbEntity, value, collector));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void serializeValue_missingScheme_throws() {
            // Intended: scheme-less string is not a valid URL.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, null);
            var value = new FieldValueText(classField).setValue("example.com");
            var collector = new TwinChangesCollector(false);

            assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(), dbEntity, value, collector));
        }

        @Test
        void serializeValue_sameValue_doesNotMutateEntity() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var dbEntity = dbEntity(classField, "https://example.com");
            var value = new FieldValueText(classField).setValue("https://example.com");
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
            var dbEntity = dbEntity(classField, "https://example.com");

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField), dbEntity);

            assertEquals("https://example.com", result.getValue());
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
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsUrlDescriptor() throws ServiceException {
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties());

            assertInstanceOf(FieldDescriptorUrl.class, descriptor);
        }
    }
}
