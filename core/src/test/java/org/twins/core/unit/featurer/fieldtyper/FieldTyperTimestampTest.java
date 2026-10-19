package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinService;
import org.cambium.common.kit.Kit;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

class FieldTyperTimestampTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private I18nService i18nService;

    private FieldTyperTimestamp fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperTimestamp();
        setField(fieldTyper, "twinService", twinService);
        setField(fieldTyper, "i18nService", i18nService);
        // lenient: only deserializeValue calls loadTwinFields; validate/getFieldDescriptor do not.
        lenient().doNothing().when(twinService).loadTwinFields(any(TwinEntity.class));
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
        // pattern default from FieldTyperDateTime interface.
        var props = new Properties();
        props.setProperty("pattern", "yyyy-MM-dd'T'HH:mm:ss");
        props.setProperty("hoursPast", "-1");
        props.setProperty("hoursFuture", "-1");
        return props;
    }

    private TwinFieldTimestampEntity tsEntity(TwinClassFieldEntity classField, LocalDateTime value) {
        // Kit is keyed by TwinClassFieldEntity::getId (the typer looks up .get(classField.getId())).
        return new TwinFieldTimestampEntity()
                .setTwinClassFieldId(classField.getId())
                .setTwinClassField(classField)
                .setValue(value != null ? Timestamp.valueOf(value) : null)
                .setTwin(new TwinEntity().setId(UUID.randomUUID()));
    }

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_storedValue_returnsParsedDate() throws ServiceException {
            // Intended: stored Timestamp is converted back to LocalDateTime; pattern controls formatting only.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var stored = LocalDateTime.of(2026, 6, 21, 10, 15, 30);
            twin.setTwinFieldTimestampKit(new Kit<>(
                    List.of(tsEntity(classField, stored)),
                    TwinFieldTimestampEntity::getTwinClassFieldId));

            FieldValueDate result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertEquals(stored, result.getDate());
        }

        @Test
        void deserializeValue_noStoredRow_leavesValueUndefined() throws ServiceException {
            // Intended: no stored entity -> undefine() is called, getDate() is null.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            twin.setTwinFieldTimestampKit(new Kit<>(List.of(), TwinFieldTimestampEntity::getTwinClassFieldId));

            FieldValueDate result = fieldTyper.deserializeValue(properties(), twinField(twin, classField));

            assertNull(result.getDate());
            assertTrue(result.isUndefined());
        }
    }

    @Nested
    class GetFieldDescriptor {

        @Test
        void getFieldDescriptor_returnsDateDescriptorWithPattern() throws ServiceException {
            // Intended: pattern is propagated; backendValidated is false (validation is client-side here).
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties());

            assertInstanceOf(FieldDescriptorDate.class, descriptor);
            assertEquals("yyyy-MM-dd'T'HH:mm:ss", ((FieldDescriptorDate) descriptor).pattern());
            assertFalse(((FieldDescriptorDate) descriptor).backendValidated());
        }

        @Test
        void getFieldDescriptor_negativeLimits_yieldNullBeforeAfterBounds() throws ServiceException {
            // Intended: hoursPast/hoursFuture default -1 -> disabled (no before/after restriction is meaningful).
            // beforeDate = now - (-1)h = now+1h; afterDate = now + (-1)h = now-1h. Both still computed.
            var classField = new TwinClassFieldEntity();

            var descriptor = fieldTyper.getFieldDescriptor(classField, properties());

            assertInstanceOf(FieldDescriptorDate.class, descriptor);
            // Sanity: the descriptor is built without throwing.
            assertNotNull(((FieldDescriptorDate) descriptor).beforeDate());
            assertNotNull(((FieldDescriptorDate) descriptor).afterDate());
        }
    }

    @Nested
    class Validate {

        @Test
        void validate_valueWithinPastWindow_isValid() throws ServiceException {
            // Intended: hoursPast=24 allows any date within the last 24h (and future); now is always valid.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var props = properties();
            props.setProperty("hoursPast", "24");
            var value = new FieldValueDate(classField, "yyyy-MM-dd'T'HH:mm:ss")
                    .setDate(LocalDateTime.now());

            var result = fieldTyper.validate(props, twin, value);

            assertTrue(result.isValid());
        }

        @Test
        void validate_valueTooFarInPast_isInvalid() throws ServiceException {
            // Intended: a date older than hoursPast is rejected.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var props = properties();
            props.setProperty("hoursPast", "1");
            var value = new FieldValueDate(classField, "yyyy-MM-dd'T'HH:mm:ss")
                    .setDate(LocalDateTime.now().minusDays(2));

            var result = fieldTyper.validate(props, twin, value);

            assertFalse(result.isValid());
        }

        @Test
        void validate_valueTooFarInFuture_isInvalid() throws ServiceException {
            // Intended: a date later than hoursFuture is rejected.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var props = properties();
            props.setProperty("hoursFuture", "1");
            var value = new FieldValueDate(classField, "yyyy-MM-dd'T'HH:mm:ss")
                    .setDate(LocalDateTime.now().plusDays(2));

            var result = fieldTyper.validate(props, twin, value);

            assertFalse(result.isValid());
        }

        @Test
        void validate_disabledBounds_alwaysValid() throws ServiceException {
            // Intended: negative hoursPast/hoursFuture disable the check -> anything is valid.
            var classField = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var value = new FieldValueDate(classField, "yyyy-MM-dd'T'HH:mm:ss")
                    .setDate(LocalDateTime.now().minusYears(10));

            var result = fieldTyper.validate(properties(), twin, value);

            assertTrue(result.isValid());
        }
    }
}
