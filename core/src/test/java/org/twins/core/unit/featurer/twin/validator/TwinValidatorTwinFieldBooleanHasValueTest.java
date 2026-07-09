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

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinFieldBooleanHasValueTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private TwinValidatorTwinFieldBooleanHasValue validator;

    @BeforeEach
    void setUp() {
        validator = new TwinValidatorTwinFieldBooleanHasValue(twinService);
    }

    private TwinEntity twinWithBooleanField(UUID fieldId, Boolean value) {
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

        var fieldValue = new FieldValueBoolean(fieldEntity);
        if (value != null) {
            fieldValue.setValue(value);
        }

        var kit = new Kit<>(FieldValue::getTwinClassFieldId);
        kit.add(fieldValue);
        twin.setFieldValuesKit(kit);

        return twin;
    }

    @Nested
    class IsValid {

        @Test
        void isValid_booleanFieldMatchesValue_returnsValid() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = twinWithBooleanField(fieldId, true);

            var props = new Properties();
            props.put("twinClassFieldId", fieldId.toString());
            props.put("value", "true");

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twin.getId()).isValid());
            verify(twinService).loadFieldsValues(any(Collection.class));
        }

        @Test
        void isValid_booleanFieldDoesNotMatchValue_returnsInvalid() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = twinWithBooleanField(fieldId, false);

            var props = new Properties();
            props.put("twinClassFieldId", fieldId.toString());
            props.put("value", "true");

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
            props.put("twinClassFieldId", fieldId.toString());
            props.put("value", "true");

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }

        @Test
        void isValid_booleanFieldMatchesValue_inverted_returnsInvalid() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = twinWithBooleanField(fieldId, true);

            var props = new Properties();
            props.put("twinClassFieldId", fieldId.toString());
            props.put("value", "true");

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twin.getId()).isValid());
        }
    }
}
