package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinFieldNotNullTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private TwinValidatorTwinFieldNotNull validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new TwinValidatorTwinFieldNotNull();
        setField(validator, "twinService", twinService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try { return clazz.getDeclaredField(fieldName); }
            catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    @Nested
    class IsValid {

        @Test
        void isValid_systemFieldNotNull_returnsValid() throws ServiceException {
            var systemFieldId = UUID.fromString("00000000-0000-0000-0011-000000000003"); // TWIN_CLASS_FIELD_TWIN_NAME
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setName("test-name");

            var props = new Properties();
            props.put("twinClassFieldIds", systemFieldId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
            verify(twinService, never()).loadFieldsValues(any(Collection.class));
        }

        @Test
        void isValid_systemFieldNull_returnsInvalid() throws ServiceException {
            var systemFieldId = UUID.fromString("00000000-0000-0000-0011-000000000003"); // TWIN_CLASS_FIELD_TWIN_NAME
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setName(null);

            var props = new Properties();
            props.put("twinClassFieldIds", systemFieldId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_dynamicFieldNotNull_returnsValid() throws Exception {
            var dynamicFieldId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());

            var fieldEntity = new TwinClassFieldEntity();
            var idField = TwinClassFieldEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(fieldEntity, dynamicFieldId);

            var fieldValue = new FieldValueBoolean(fieldEntity);
            fieldValue.setValue(true);
            var kit = new Kit<>(FieldValue::getTwinClassFieldId);
            kit.add(fieldValue);
            twin.setFieldValuesKit(kit);

            var props = new Properties();
            props.put("twinClassFieldIds", dynamicFieldId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
            verify(twinService).loadFieldsValues(List.of(twin));
        }

        @Test
        void isValid_dynamicFieldNull_returnsInvalid() throws ServiceException {
            var dynamicFieldId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));

            var props = new Properties();
            props.put("twinClassFieldIds", dynamicFieldId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_mixedFields_allNotNull_returnsValid() throws Exception {
            var systemFieldId = UUID.fromString("00000000-0000-0000-0011-000000000003"); // TWIN_CLASS_FIELD_TWIN_NAME
            var dynamicFieldId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setName("test-name");

            var fieldEntity = new TwinClassFieldEntity();
            var idField = TwinClassFieldEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(fieldEntity, dynamicFieldId);

            var fieldValue = new FieldValueBoolean(fieldEntity);
            fieldValue.setValue(true);
            var kit = new Kit<>(FieldValue::getTwinClassFieldId);
            kit.add(fieldValue);
            twin.setFieldValuesKit(kit);

            var props = new Properties();
            props.put("twinClassFieldIds", systemFieldId + ", " + dynamicFieldId);

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_systemFieldNotNull_inverted_returnsInvalid() throws ServiceException {
            var systemFieldId = UUID.fromString("00000000-0000-0000-0011-000000000003"); // TWIN_CLASS_FIELD_TWIN_NAME
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setName("test-name");

            var props = new Properties();
            props.put("twinClassFieldIds", systemFieldId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
