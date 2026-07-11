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
import org.twins.core.featurer.factory.lookuper.FieldLookuper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldLookuperTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private StubLookuper lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new StubLookuper();
        setField(lookuper, "twinService", twinService);
    }

    // contract for FieldLookuper.getFreshestValue(twinEntity, twinClassFieldId, factoryContext, msg):
    //   1. If factoryContext has an output FactoryItem for twinEntity.id, return its uncommitted field.
    //   2. Otherwise (or if that field is null), fall back to twinService.getTwinFieldValue(twinEntity, fieldId).
    //   3. If both are null, throw ServiceException(FACTORY_PIPELINE_STEP_ERROR) with the supplied msg.

    @Nested
    class GetFreshestValue {

        @Test
        void getFreshestValue_uncommittedPresent_returnsUncommittedAndSkipsDb() throws Exception {
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var ctx = new FactoryContext(null, null);
            var uncommitted = fieldValue(fieldId, "uncommitted");
            registerOutputFieldInContext(ctx, twin.getId(), uncommitted);

            var result = lookuper.callGetFreshestValue(twin, fieldId, ctx, "msg");

            assertSame(uncommitted, result);
            verifyNoInteractions(twinService);
        }

        @Test
        void getFreshestValue_uncommittedNull_fallsBackToDb() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var ctx = new FactoryContext(null, null);
            var dbValue = fieldValue(fieldId, "db-val");

            when(twinService.getTwinFieldValue(twin, fieldId)).thenReturn(dbValue);

            var result = lookuper.callGetFreshestValue(twin, fieldId, ctx, "msg");

            assertSame(dbValue, result);
            verify(twinService).getTwinFieldValue(twin, fieldId);
        }

        @Test
        void getFreshestValue_uncommittedNullAndDbNull_throwsWithSuppliedMessage() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity().setId(UUID.randomUUID());
            var ctx = new FactoryContext(null, null);

            when(twinService.getTwinFieldValue(twin, fieldId)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> lookuper.callGetFreshestValue(twin, fieldId, ctx, "custom-msg"));

            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }

    /** Minimal same-package stub so we can invoke the public getFreshestValue directly. */
    static class StubLookuper extends FieldLookuper {
        public FieldValue callGetFreshestValue(TwinEntity twinEntity, UUID fieldId, FactoryContext ctx, String msg) throws ServiceException {
            return getFreshestValue(twinEntity, fieldId, ctx, msg);
        }
    }

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
