package org.twins.core.unit.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFields;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;

class FieldLookuperFromContextFieldsTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FieldLookuperFromContextFields lookuper;

    @BeforeEach
    void setUp() throws Exception {
        lookuper = new FieldLookuperFromContextFields();
        setField(lookuper, "twinService", twinService);
    }

    // contract: resolve the field value from the factory context's own fields map,
    //           keyed by lookupTwinClassFieldId. Must NOT touch the DB / TwinService.
    //           Missing key -> ServiceException(FACTORY_PIPELINE_STEP_ERROR, code 11002).

    @Nested
    class LookupFieldValue {

        @Test
        void lookupFieldValue_fieldPresentInContext_returnsThatValue() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            var factoryItem = new FactoryItem().setFactoryContext(new FactoryContext(null, null));
            var expected = fieldValue(fieldId, "ctx-val");
            factoryItem.getFactoryContext().getFields().put(fieldId, expected);

            var result = lookuper.lookupFieldValue(factoryItem, field);

            assertSame(expected, result);
            // source-resolved from the context map only: TwinService must never be consulted.
            verifyNoInteractions(twinService);
        }

        @Test
        void lookupFieldValue_fieldAbsentInContext_returnsNull() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(fieldId);
            var factoryItem = new FactoryItem().setFactoryContext(new FactoryContext(null, null));

            assertNull(lookuper.lookupFieldValue(factoryItem, field)); // not-found is the caller's decision (undefined value at the batch boundary)
            verifyNoInteractions(twinService);
        }

        @Test
        void lookupFieldValue_wrongFieldIdPresent_returnsNullForRequestedId() throws ServiceException {
            var requestedId = UUID.randomUUID();
            var field = new TwinClassFieldEntity().setId(requestedId);
            var otherId = UUID.randomUUID();
            var factoryItem = new FactoryItem().setFactoryContext(new FactoryContext(null, null));
            factoryItem.getFactoryContext().getFields().put(otherId, fieldValue(otherId, "other"));

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
