package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorSecret;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FieldTyperSecretTest extends BaseUnitTest {

    @Mock
    private StandardPBEStringEncryptor secretEncryptor;

    private FieldTyperSecret fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperSecret();
        setField(fieldTyper, "secretEncryptor", secretEncryptor);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private TwinField twinField(TwinEntity twin, TwinClassFieldEntity classField) {
        return new TwinField(twin, classField);
    }

    private Properties properties() {
        // regexp is a required param for Secret (no optional/defaultValue).
        var props = new Properties();
        props.setProperty("regexp", ".+");
        return props;
    }

    private TwinEntity twinWithKit(TwinClassFieldEntity classField, String value) {
        var twin = new TwinEntity().setId(UUID.randomUUID());
        var dbEntity = new TwinFieldSimpleNonIndexedEntity()
                .setTwinClassField(classField)
                .setTwinClassFieldId(classField.getId())
                .setTwin(twin)
                .setValue(value);
        twin.setTwinFieldSimpleNonIndexedKit(new Kit<>(List.of(dbEntity), TwinFieldSimpleNonIndexedEntity::getTwinClassFieldId));
        return twin;
    }

    private TwinEntity twinWithoutEntity(TwinClassFieldEntity classField) {
        var twin = new TwinEntity().setId(UUID.randomUUID());
        twin.setTwinFieldSimpleNonIndexedKit(new Kit<>(List.of(), TwinFieldSimpleNonIndexedEntity::getTwinClassFieldId));
        return twin;
    }

    private TwinFieldSimpleNonIndexedEntity firstInKit(TwinEntity twin) {
        return twin.getTwinFieldSimpleNonIndexedKit().iterator().next();
    }

    @Nested
    class SerializeValue {

        @Test
        void serializeValue_matchingValue_encryptsAndPersists() throws ServiceException {
            // Intended: plaintext is validated against regexp, encrypted, then the ciphertext is stored.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);
            var value = new FieldValueText(classField).setValue("p@ssw0rd");
            var collector = new TwinChangesCollector(false);
            when(secretEncryptor.encrypt("p@ssw0rd")).thenReturn("ENC:CIPHER");

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertEquals("ENC:CIPHER", firstInKit(twin).getValue());
            assertTrue(collector.hasChanges());
        }

        @Test
        void serializeValue_valueViolatingRegexp_throws() {
            // Intended: validation happens BEFORE encryption; mismatched value never reaches the encryptor.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, null);
            var value = new FieldValueText(classField).setValue("short");
            var collector = new TwinChangesCollector(false);
            var props = properties();
            props.setProperty("regexp", "^.{8,}$"); // at least 8 chars

            var ex = assertThrows(ServiceException.class,
                    () -> fieldTyper.serializeValue(props, twin, value, collector));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT.getCode(), ex.getErrorCode());
            verifyNoInteractions(secretEncryptor);
        }

        @Test
        void serializeValue_sameCiphertext_doesNotMutateEntity() throws ServiceException {
            // Intended: when the encrypted form equals the already-stored value, no change is recorded.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, "ENC:SAME");
            var value = new FieldValueText(classField).setValue("plain");
            var collector = new TwinChangesCollector(false);
            when(secretEncryptor.encrypt("plain")).thenReturn("ENC:SAME");

            fieldTyper.serializeValue(properties(), twin, value, collector);

            assertEquals("ENC:SAME", firstInKit(twin).getValue());
            assertFalse(collector.hasChanges());
        }
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_dbEntityPresent_decryptsValue() throws ServiceException {
            // Intended: stored ciphertext is decrypted back to plaintext.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithKit(classField, "ENC:CIPHER");
            when(secretEncryptor.decrypt("ENC:CIPHER")).thenReturn("p@ssw0rd");

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals("p@ssw0rd", result.getValue());
        }

        @Test
        void deserializeValue_nullDbEntity_returnsNullValue() throws ServiceException {
            // Intended: no stored secret -> plaintext value is null.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = twinWithoutEntity(classField);

            FieldValueText result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertNull(result.getValue());
            verifyNoInteractions(secretEncryptor);
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_propagatesRegexp() throws ServiceException {
            var classField = new TwinClassFieldEntity();
            var props = properties();
            props.setProperty("regexp", "^[0-9]+$");

            var descriptor = fieldTyper.getFieldDescriptor(classField, props);

            assertInstanceOf(FieldDescriptorSecret.class, descriptor);
            assertEquals("^[0-9]+$", ((FieldDescriptorSecret) descriptor).regExp());
        }
    }
}
