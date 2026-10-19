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
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUrl;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.List;
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
        void serializeValue_validHttpUrl_persists() throws ServiceException {
            // Intended: a well-formed http(s) URL with a host passes UrlUtils.isValid and is stored.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);
            var value = new FieldValueText(classField).setValue("https://example.com/path");
            var collector = new TwinChangesCollector(false);

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertEquals("https://example.com/path", firstInKit(twin).getValue());
            assertTrue(collector.hasChanges());
        }

        @Test
        void serializeValue_nonHttpScheme_throws() {
            // Intended: UrlUtils.isValid only accepts http/https schemes -> ftp rejected.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);
            var value = new FieldValueText(classField).setValue("ftp://example.com/file");
            var collector = new TwinChangesCollector(false);

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(), twin, value, collector));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void serializeValue_missingScheme_throws() {
            // Intended: scheme-less string is not a valid URL.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);
            var value = new FieldValueText(classField).setValue("example.com");
            var collector = new TwinChangesCollector(false);

            assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(properties(), twin, value, collector));
        }

        @Test
        void serializeValue_sameValue_doesNotMutateEntity() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, "https://example.com");
            var value = new FieldValueText(classField).setValue("https://example.com");
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
            var twin = twinWithKit(classField, "https://example.com");

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals("https://example.com", result.getValue());
        }

        @Test
        void deserializeValue_nullDbEntity_returnsNullValue() throws ServiceException {
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithoutEntity(classField);

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

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
