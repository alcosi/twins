package org.twins.core.unit.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputDbFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperFromItemOutputDbFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FieldLookuperFromItemOutputDbFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromItemOutputDbFields();
        setField(lookuper, "twinService", twinService);
    }

    // contract: resolve the field value from factoryItem.getOutput().getTwinEntity()'s DB fields
    //           via getTwinFieldValue(outputTwin, fieldId). Missing -> ServiceException(FACTORY_PIPELINE_STEP_ERROR).
    //           Source: ONLY the output twin's DB fields (not the context, not uncommitted).

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_fieldPresentInOutputTwinDb_returnsValue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var outputTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithOutputTwin(outputTwin);
            var expected = fieldValue(fieldId, "db-val");

            when(twinService.getTwinFieldValue(outputTwin, fieldId)).thenReturn(expected);

            var result = lookuper.lookupFieldValue(factoryItem, fieldId);

            assertSame(expected, result);
            verify(twinService).getTwinFieldValue(outputTwin, fieldId);
        }

        @Test
        void lookupFieldValue_fieldAbsentInOutputTwinDb_throwsFactoryPipelineError() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var outputTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithOutputTwin(outputTwin);

            when(twinService.getTwinFieldValue(outputTwin, fieldId)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, fieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }

    private FactoryItem itemWithOutputTwin(TwinEntity twin) {
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output);
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
