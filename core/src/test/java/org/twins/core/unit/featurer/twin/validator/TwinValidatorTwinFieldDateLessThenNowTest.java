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
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinFieldDateLessThenNowTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private TwinValidatorTwinFieldDateLessThenNow validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new TwinValidatorTwinFieldDateLessThenNow();
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

    private TwinEntity twinWithDateField(UUID fieldId, LocalDateTime date) {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());

        var fieldEntity = new TwinClassFieldEntity();
        try {
            var idField = TwinClassFieldEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(fieldEntity, fieldId);
        } catch (Exception e) {
            fail("Failed to set id on TwinClassFieldEntity: " + e.getMessage());
        }

        var fieldValue = new FieldValueDate(fieldEntity, null);
        fieldValue.setDate(date);

        var kit = new Kit<>(FieldValue::getTwinClassFieldId);
        kit.add(fieldValue);
        twin.setFieldValuesKit(kit);

        return twin;
    }

    @Nested
    class IsValid {

        @Test
        void isValid_dateInPast_returnsValid() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = twinWithDateField(fieldId, LocalDateTime.now().minusDays(1));

            var props = new Properties();
            props.put("twinClassFieldDateId", fieldId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
            verify(twinService).loadFieldsValues(any(Collection.class));
        }

        @Test
        void isValid_dateInFuture_returnsInvalid() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = twinWithDateField(fieldId, LocalDateTime.now().plusDays(1));

            var props = new Properties();
            props.put("twinClassFieldDateId", fieldId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_fieldNull_returnsInvalid() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twin.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));

            var props = new Properties();
            props.put("twinClassFieldDateId", fieldId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_dateInPast_inverted_returnsInvalid() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = twinWithDateField(fieldId, LocalDateTime.now().minusDays(1));

            var props = new Properties();
            props.put("twinClassFieldDateId", fieldId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
