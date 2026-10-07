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
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinDbFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperFromContextTwinDbFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldLookuperFromContextTwinDbFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromContextTwinDbFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinClassFieldService", twinClassFieldService);
    }

    // contract: resolve the field value from the SINGLE context twin's DB fields,
    //           going through TwinService.wrapField(contextTwin, twinClassField) then
    //           TwinService.getTwinFieldValue(TwinField). Missing -> ServiceException(FACTORY_PIPELINE_STEP_ERROR).
    //           Zero or multiple context twins -> checkSingleContextItem throws FACTORY_PIPELINE_STEP_ERROR
    //           before TwinService is consulted.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_fieldPresentInContextTwinDb_returnsValueAndQueriesContextTwin() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            when(twinClassFieldService.findEntitySafe(field.getId())).thenReturn(field); // the UUID entry resolves the field entity once per call
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = factoryItemWithSingleContext(contextTwin);
            var expected = filledValue(fieldId, "db-val");

            when(twinService.getTwinFieldValue(contextTwin, field)).thenReturn(expected);

            var result = lookuper.lookupFieldValue(factoryItem, field.getId());

            assertSame(expected, result);
            // read hits the context twin and the requested field (no other twin).
            verify(twinService).getTwinFieldValue(contextTwin, field);
        }

        @Test
        void lookupFieldValue_fieldAbsentInContextTwinDb_returnsUndefinedValue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            when(twinClassFieldService.findEntitySafe(field.getId())).thenReturn(field); // the UUID entry resolves the field entity once per call
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = factoryItemWithSingleContext(contextTwin);

            when(twinService.getTwinFieldValue(contextTwin, field)).thenReturn(null);

            var undefined = new FieldValueText(field); // no value set -> isUndefined()
            when(twinService.createFieldValue(field)).thenReturn(undefined);
            assertSame(undefined, lookuper.lookupFieldValue(factoryItem, field.getId())); // the per-item entry converts a not-found lookup into an undefined value
        }

        @Test
        void lookupFieldValue_multipleContextTwins_throwsFactoryPipelineError() throws ServiceException {
            var field = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var factoryItem = new FactoryItem();
            // two context items -> checkSingleContextTwin fails before TwinService is touched.
            factoryItem.setContextFactoryItemList(List.of(
                    new FactoryItem(),
                    new FactoryItem()));

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, field.getId()));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void lookupFieldValue_zeroContextTwins_throwsFactoryPipelineError() throws ServiceException {
            var field = new TwinClassFieldEntity().setId(UUID.randomUUID());
            var factoryItem = new FactoryItem(); // empty context list

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, field.getId()));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }
    }

    private FactoryItem factoryItemWithSingleContext(TwinEntity contextTwin) {
        // Use TwinCreate so getTwin() returns contextTwin directly (no TwinUpdate.isSketch() / status NPE).
        var output = new TwinCreate();
        output.setTwinEntity(contextTwin);
        var root = new FactoryItem().setOutput(output);
        return new FactoryItem().setContextFactoryItemList(List.of(root));
    }

    private FieldValue filledValue(UUID fieldId, String value) {
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
