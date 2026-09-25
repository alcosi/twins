package org.twins.core.unit.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputUncommitedFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;

class FieldLookuperFromItemOutputUncommitedFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FieldLookuperFromItemOutputUncommitedFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromItemOutputUncommitedFields();
        setField(lookuper, "twinService", twinService);
    }

    // contract: resolve the field value from factoryItem.getOutput().getField(fieldId) ONLY
    //           (uncommitted output fields). Missing -> ServiceException(FACTORY_PIPELINE_STEP_ERROR).
    //           Never consults the DB.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_fieldPresentInOutput_returnsValue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            var expected = fieldValue(fieldId, "uncommitted-val");
            var output = new TwinCreate();
            output.addField(expected);
            var factoryItem = new FactoryItem().setOutput(output);

            var result = lookuper.lookupFieldValue(factoryItem, field);

            assertSame(expected, result);
            verifyNoInteractions(twinService);
        }

        @Test
        void lookupFieldValue_fieldAbsentInOutput_returnsNull() throws ServiceException {
            var field = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var output = new TwinCreate();
            var factoryItem = new FactoryItem().setOutput(output);

            assertNull(lookuper.lookupFieldValue(factoryItem, field)); // not-found is the caller's decision (undefined value at the batch boundary)
            verifyNoInteractions(twinService);
        }
    }

    private FieldValue fieldValue(UUID fieldId, String value) {
        var twinClassField = new TwinClassFieldEntity();
        twinClassField.setId(fieldId);
        var fv = new FieldValueText(twinClassField);
        fv.setValue(value);
        return fv;
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
}
