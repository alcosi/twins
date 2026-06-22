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
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputHeadTwinFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperFromItemOutputHeadTwinFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FieldLookuperFromItemOutputHeadTwinFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromItemOutputHeadTwinFields();
        setField(lookuper, "twinService", twinService);
    }

    // contract: load head twin of factoryItem.getTwin() (null head -> ServiceException),
    //           then resolve the field from head twin's freshest value (uncommitted output of head,
    //           else head DB). Missing -> ServiceException(FACTORY_PIPELINE_STEP_ERROR).
    //           Source: ONLY the head twin (uncommitted-or-db).

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_headTwinNull_throwsFactoryPipelineError() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin, new FactoryContext(null, null));

            when(twinService.loadHeadForTwin(twin)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, fieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(UUID.class));
        }

        @Test
        void lookupFieldValue_fieldInHeadDb_returnsHeadValue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryContext = new FactoryContext(null, null);
            var factoryItem = itemWithTwin(twin, factoryContext);

            when(twinService.loadHeadForTwin(twin)).thenReturn(headTwin);
            var expected = fieldValue(fieldId, "head-db-val");
            when(twinService.getTwinFieldValue(headTwin, fieldId)).thenReturn(expected);

            var result = lookuper.lookupFieldValue(factoryItem, fieldId);

            assertSame(expected, result);
            verify(twinService).getTwinFieldValue(headTwin, fieldId);
            // Must NOT consult the twin itself.
            verify(twinService, never()).getTwinFieldValue(twin, fieldId);
        }

        @Test
        void lookupFieldValue_fieldNowhereOnHead_throwsFactoryPipelineError() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin, new FactoryContext(null, null));

            when(twinService.loadHeadForTwin(twin)).thenReturn(headTwin);
            when(twinService.getTwinFieldValue(headTwin, fieldId)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.lookupFieldValue(factoryItem, fieldId));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }

    private FactoryItem itemWithTwin(TwinEntity twin, FactoryContext factoryContext) {
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output).setFactoryContext(factoryContext);
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
