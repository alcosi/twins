package org.twins.core.unit.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFieldsAndContextTwinDbFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperFromContextFieldsAndContextTwinDbFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FieldLookuperFromContextFieldsAndContextTwinDbFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromContextFieldsAndContextTwinDbFields();
        setField(lookuper, "twinService", twinService);
    }

    // contract: prefer the factory context's own fields map; if not filled there,
    //           fall back to the context twin's DB fields; if still not filled, descend one
    //           level (checkSingleContextItem().checkSingleContextTwin()) and try its DB fields.
    //           All-none -> ServiceException(FACTORY_PIPELINE_STEP_ERROR).
    //           Source priority: contextFields > contextTwinDb > deeperContextTwinDb.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_presentInContextFields_returnsContextValueAndSkipsDb() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithContextAndContext(contextTwin, null);
            var ctxValue = fieldValue(fieldId, "ctx-val");
            factoryItem.getFactoryContext().getFields().put(fieldId, ctxValue);

            var result = lookuper.lookupFieldValue(factoryItem, fieldId);

            assertSame(ctxValue, result);
            // DB is never consulted when the context field is filled.
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(UUID.class));
        }

        @Test
        void lookupFieldValue_absentInContextButInContextTwinDb_returnsContextTwinValue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var deeperTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithContextAndContext(contextTwin, deeperTwin);
            var dbValue = fieldValue(fieldId, "db-val");

            // contextFields miss; context twin DB hit.
            when(twinService.getTwinFieldValue(contextTwin, fieldId)).thenReturn(dbValue);

            var result = lookuper.lookupFieldValue(factoryItem, fieldId);

            assertSame(dbValue, result);
            verify(twinService).getTwinFieldValue(contextTwin, fieldId);
            // deeper twin must NOT be consulted when the first context twin hit.
            verify(twinService, never()).getTwinFieldValue(deeperTwin, fieldId);
        }

        @Test
        void lookupFieldValue_absentEverywhere_throwsFactoryPipelineError() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var deeperTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithContextAndContext(contextTwin, deeperTwin);

            when(twinService.getTwinFieldValue(contextTwin, fieldId)).thenReturn(null);
            when(twinService.getTwinFieldValue(deeperTwin, fieldId)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, fieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }

    /**
     * Builds factoryItem whose single context item is `contextTwin`, and that context item
     * itself has a single context item wrapping `deeperTwin` (used by the "look deeper" branch).
     * deeperTwin may be null when the test does not exercise the deeper path.
     */
    private FactoryItem itemWithContextAndContext(TwinEntity contextTwin, TwinEntity deeperTwin) {
        var factoryItem = new FactoryItem().setFactoryContext(new FactoryContext(null, null));
        var contextOutput = new TwinCreate();
        contextOutput.setTwinEntity(contextTwin);
        var contextItem = new FactoryItem().setOutput(contextOutput).setFactoryContext(factoryItem.getFactoryContext());
        if (deeperTwin != null) {
            var deeperOutput = new TwinCreate();
            deeperOutput.setTwinEntity(deeperTwin);
            var deeperItem = new FactoryItem().setOutput(deeperOutput).setFactoryContext(factoryItem.getFactoryContext());
            contextItem.setContextFactoryItemList(List.of(deeperItem));
        }
        factoryItem.setContextFactoryItemList(List.of(contextItem));
        return factoryItem;
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
