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
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperFromContextTwinFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FieldLookuperFromContextTwinFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromContextTwinFields();
        setField(lookuper, "twinService", twinService);
    }

    // contract: resolve from SINGLE context item — uncommitted output.getField(fieldId) first;
    //           if null, fall back to twinService.getTwinFieldValue(contextTwin, fieldId).
    //           Both null -> ServiceException(FACTORY_PIPELINE_STEP_ERROR).

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_fieldPresentInContextItemOutput_returnsUncommittedAndSkipsDb() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var expected = fieldValue(fieldId, "uncommitted-val");
            var output = new TwinCreate();
            output.setTwinEntity(contextTwin);
            output.addField(expected);
            var contextItem = new FactoryItem().setOutput(output);
            var factoryItem = new FactoryItem().setContextFactoryItemList(List.of(contextItem));

            var result = lookuper.lookupFieldValue(factoryItem, fieldId);

            assertSame(expected, result);
            verifyNoInteractions(twinService);
        }

        @Test
        void lookupFieldValue_notInUncommitted_fallsBackToDb() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var dbValue = fieldValue(fieldId, "db-val");
            var output = new TwinCreate();
            output.setTwinEntity(contextTwin);
            var contextItem = new FactoryItem().setOutput(output);
            var factoryItem = new FactoryItem().setContextFactoryItemList(List.of(contextItem));

            when(twinService.getTwinFieldValue(contextTwin, fieldId)).thenReturn(dbValue);

            var result = lookuper.lookupFieldValue(factoryItem, fieldId);

            assertSame(dbValue, result);
            verify(twinService).getTwinFieldValue(contextTwin, fieldId);
        }

        @Test
        void lookupFieldValue_nowhere_throwsFactoryPipelineError() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var output = new TwinCreate();
            output.setTwinEntity(contextTwin);
            var contextItem = new FactoryItem().setOutput(output);
            var factoryItem = new FactoryItem().setContextFactoryItemList(List.of(contextItem));

            when(twinService.getTwinFieldValue(contextTwin, fieldId)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, fieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void lookupFieldValue_noContextItem_throwsFactoryPipelineError() {
            var fieldId = UUID.randomUUID();
            var factoryItem = new FactoryItem();

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, fieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
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
