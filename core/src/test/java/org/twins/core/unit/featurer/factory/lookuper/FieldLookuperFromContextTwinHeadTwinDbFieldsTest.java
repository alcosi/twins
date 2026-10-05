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
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextTwinHeadTwinDbFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class FieldLookuperFromContextTwinHeadTwinDbFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldLookuperFromContextTwinHeadTwinDbFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromContextTwinHeadTwinDbFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinClassFieldService", twinClassFieldService);
    }

    // contract: load the head twin of factoryItem.getTwin() via TwinService.loadHead,
    //           then resolve the field from the head twin's DB fields via getTwinFieldValue(headTwin, fieldId).
    //           Missing on head -> ServiceException(FACTORY_PIPELINE_STEP_ERROR).
    //           Source: ONLY the head twin's DB fields.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_fieldPresentOnHeadTwinDb_returnsHeadValue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            when(twinClassFieldService.findEntitySafe(field.getId())).thenReturn(field); // the UUID entry resolves the field entity once per call
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);

            twin.setHeadTwin(headTwin); // the head stage's bulk loadHead is a no-op mock — preset the field the stage reads
            var expected = fieldValue(fieldId, "head-db-val");
            when(twinService.getTwinFieldValue(headTwin, field)).thenReturn(expected);

            var result = lookuper.lookupFieldValue(factoryItem, field.getId());

            assertSame(expected, result);
            verify(twinService).getTwinFieldValue(headTwin, field);
            // Must NOT consult the twin itself for the field (source isolation).
            verify(twinService, never()).getTwinFieldValue(twin, field);
        }

        @Test
        void lookupFieldValue_fieldAbsentOnHeadTwinDb_returnsUndefinedValue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            when(twinClassFieldService.findEntitySafe(field.getId())).thenReturn(field); // the UUID entry resolves the field entity once per call
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var headTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = itemWithTwin(twin);

            twin.setHeadTwin(headTwin); // the head stage's bulk loadHead is a no-op mock — preset the field the stage reads
            when(twinService.getTwinFieldValue(headTwin, field)).thenReturn(null);

            var undefined = new FieldValueText(field); // no value set -> isUndefined()
            when(twinService.createFieldValue(field)).thenReturn(undefined);
            assertSame(undefined, lookuper.lookupFieldValue(factoryItem, field.getId())); // the per-item entry converts a not-found lookup into an undefined value
        }
    }

    private FactoryItem itemWithTwin(TwinEntity twin) {
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
