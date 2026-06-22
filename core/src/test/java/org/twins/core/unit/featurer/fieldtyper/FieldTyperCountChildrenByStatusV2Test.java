package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldTyperCountChildrenByStatusV2Test extends BaseUnitTest {

    @Mock
    private TwinFieldSimpleRepository twinFieldSimpleRepository;

    private FieldTyperCountChildrenByStatusV2 fieldTyper;

    @BeforeEach
    void setUp() throws Exception {
        fieldTyper = new FieldTyperCountChildrenByStatusV2();
        setField(fieldTyper, "twinFieldSimpleRepository", twinFieldSimpleRepository);
    }

    // V2 persists the computed count into the twin's own simple (text) field on serializeValue, then
    // deserializeValue parses it back via parseTwinFieldValue and renders Long.toString(). The
    // repository is only touched on the serialize path (getCountResult), not on deserialize.
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

    @Nested
    class DeserializeValue {

        @Test
        void deserializeValue_returnsPersistedCountAsString() throws ServiceException {
            // parseTwinFieldValue parses the stored text to Long, then .toString() -> same digits.
            var twinFieldSimpleEntity = new TwinFieldSimpleEntity();
            twinFieldSimpleEntity.setValue("42");
            var classField = new TwinClassFieldEntity();
            var twinField = new TwinField(null, classField);

            FieldValueText result = fieldTyper.deserializeValue(new Properties(), twinField, twinFieldSimpleEntity);

            assertEquals("42", result.getValue());
        }

        @Test
        void deserializeValue_nullEntityValue_rendersZero() throws ServiceException {
            // parseTwinFieldValue returns 0L for a null/empty stored value (no NPE).
            var twinFieldSimpleEntity = new TwinFieldSimpleEntity();
            var classField = new TwinClassFieldEntity();
            var twinField = new TwinField(null, classField);

            FieldValueText result = fieldTyper.deserializeValue(new Properties(), twinField, twinFieldSimpleEntity);

            assertEquals("0", result.getValue());
        }

        @Test
        void deserializeValue_emptyEntityValue_rendersZero() throws ServiceException {
            // parseTwinFieldValue treats empty string as 0 too (ObjectUtils.isEmpty guard).
            var twinFieldSimpleEntity = new TwinFieldSimpleEntity();
            twinFieldSimpleEntity.setValue("");
            var classField = new TwinClassFieldEntity();
            var twinField = new TwinField(null, classField);

            FieldValueText result = fieldTyper.deserializeValue(new Properties(), twinField, twinFieldSimpleEntity);

            assertEquals("0", result.getValue());
        }
    }
}
