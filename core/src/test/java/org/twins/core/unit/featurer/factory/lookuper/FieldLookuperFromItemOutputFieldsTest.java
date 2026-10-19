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
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class FieldLookuperFromItemOutputFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FieldLookuperFromItemOutputFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromItemOutputFields();
        setField(lookuper, "twinService", twinService);
        setField(lookuper, "twinClassFieldService", twinClassFieldService);
    }

    // contract: getFreshestValue — first try the uncommitted output field for twinEntity.id
    //           (looked up via factoryContext.getFactoryItem(twinId).getOutput().getField(fieldId));
    //           if null, fall back to twinService.getTwinFieldValue(twinEntity, fieldId).
    //           Both null -> lookupFieldValueOrNull returns null; the per-item template converts
    //           that null into an undefined value.
    //           Source priority: uncommitted output field > twin DB field.

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_presentAsUncommittedOutput_returnsUncommittedValueAndSkipsDb() throws Exception {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            when(twinClassFieldService.findEntitySafe(field.getId())).thenReturn(field); // the UUID entry resolves the field entity once per call
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var factoryContext = new FactoryContext(null, null);
            var factoryItem = itemWithTwinAndContext(twin, factoryContext);
            var uncommitted = fieldValue(fieldId, "uncommitted-val");
            registerOutputFieldInContext(factoryContext, twin.getId(), uncommitted);

            var result = lookuper.lookupFieldValue(factoryItem, field.getId());

            assertSame(uncommitted, result);
            // DB must not be consulted when uncommitted output has the value.
            verify(twinService, never()).getTwinFieldValue(any(TwinEntity.class), any(TwinClassFieldEntity.class)); // no db read happened
        }

        @Test
        void lookupFieldValue_notInUncommitted_fallsBackToDb() throws Exception {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            when(twinClassFieldService.findEntitySafe(field.getId())).thenReturn(field); // the UUID entry resolves the field entity once per call
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var factoryContext = new FactoryContext(null, null);
            var factoryItem = itemWithTwinAndContext(twin, factoryContext);
            var dbValue = fieldValue(fieldId, "db-val");

            // factoryContext.getFactoryItem returns null -> straight to DB fallback.
            when(twinService.getTwinFieldValue(twin, field)).thenReturn(dbValue);

            var result = lookuper.lookupFieldValue(factoryItem, field.getId());

            assertSame(dbValue, result);
            verify(twinService).getTwinFieldValue(twin, field);
        }

        @Test
        void lookupFieldValue_nowhere_returnsUndefinedValue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            when(twinClassFieldService.findEntitySafe(field.getId())).thenReturn(field); // the UUID entry resolves the field entity once per call
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var factoryContext = new FactoryContext(null, null);
            var factoryItem = itemWithTwinAndContext(twin, factoryContext);

            when(twinService.getTwinFieldValue(twin, field)).thenReturn(null);

            var undefined = new FieldValueText(field); // no value set -> isUndefined()
            when(twinService.createFieldValue(field)).thenReturn(undefined);
            assertSame(undefined, lookuper.lookupFieldValue(factoryItem, field.getId())); // the per-item entry converts a not-found lookup into an undefined value
        }
    }

    private FactoryItem itemWithTwinAndContext(TwinEntity twin, FactoryContext factoryContext) {
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        return new FactoryItem().setOutput(output).setFactoryContext(factoryContext);
    }

    /**
     * Registers an uncommitted FieldValue under twinId in the FactoryContext's factoryItemsUniq
     * map, mimicking what FactoryContext.add() does for an output item — so getFreshestValue's
     * factoryContext.getFactoryItem(twinId) path finds it.
     */
    @SuppressWarnings("unchecked")
    private void registerOutputFieldInContext(FactoryContext ctx, UUID twinId, FieldValue fv) throws Exception {
        var output = new TwinCreate();
        output.addField(fv);
        var item = new FactoryItem().setOutput(output).setFactoryContext(ctx);
        var uniqField = findField(FactoryContext.class, "factoryItemsUniq");
        uniqField.setAccessible(true);
        var uniq = (Hashtable<UUID, FactoryItem>) uniqField.get(ctx);
        uniq.put(twinId, item);
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
