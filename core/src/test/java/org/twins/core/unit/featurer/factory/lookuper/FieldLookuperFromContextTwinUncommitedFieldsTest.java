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
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinUncommitedFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FieldLookuperFromContextTwinUncommitedFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldLookuperFromContextTwinUncommitedFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromContextTwinUncommitedFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinClassFieldService", twinClassFieldService);
    }

    // contract: resolve the field value from the SINGLE context item's uncommitted output fields
    //           (output.getField(fieldId)). Missing -> ServiceException(FACTORY_PIPELINE_STEP_ERROR).
    //           Source: ONLY the context item's output fields map. Never the DB.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_fieldPresentInContextItemOutput_returnsValue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            when(twinClassFieldService.findEntitySafe(field.getId())).thenReturn(field); // the UUID entry resolves the field entity once per call
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var expected = fieldValue(fieldId, "uncommitted-val");
            var output = new TwinCreate();
            output.addField(expected);
            var contextItem = new FactoryItem().setOutput(output);
            var factoryItem = new FactoryItem().setContextFactoryItemList(List.of(contextItem));

            var result = lookuper.lookupFieldValue(factoryItem, field.getId());

            assertSame(expected, result);
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class)); // no db read happened
        }

        @Test
        void lookupFieldValue_fieldAbsentInContextItemOutput_returnsUndefinedValue() throws ServiceException {
            var field = new TwinClassFieldEntity().setId(UUID.randomUUID());
            when(twinClassFieldService.findEntitySafe(field.getId())).thenReturn(field); // the UUID entry resolves the field entity once per call
            var output = new TwinCreate();
            var contextItem = new FactoryItem().setOutput(output);
            var factoryItem = new FactoryItem().setContextFactoryItemList(List.of(contextItem));

            var undefined = new FieldValueText(field); // no value set -> isUndefined()
            when(twinService.createFieldValue(field)).thenReturn(undefined);
            assertSame(undefined, lookuper.lookupFieldValue(factoryItem, field.getId())); // the per-item entry converts a not-found lookup into an undefined value
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class)); // no db read happened
        }

        @Test
        void lookupFieldValue_noContextItem_throwsFactoryPipelineError() throws ServiceException {
            var field = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var factoryItem = new FactoryItem(); // empty context list

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, field.getId()));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class)); // no db read happened
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
