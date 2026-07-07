package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerFieldsFromContextAll;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerFieldsFromContextAllTest extends BaseUnitTest {

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FillerFieldsFromContextAll filler;

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldsFromContextAll();
        inject(filler, "twinClassFieldService", twinClassFieldService);
    }

    private void inject(Object target, String name, Object value) throws Exception {
        Field f = findField(target.getClass(), name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("field not found: " + name);
    }

    private FactoryItem buildFactoryItem(TwinClassEntity outputClass, Map<UUID, FieldValue> contextFields) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity().setTwinClass(outputClass));
        return new FactoryItem()
                .setOutput(output)
                .setFactoryContext(buildContextWithFields(contextFields));
    }

    private FactoryContext buildContextWithFields(Map<UUID, FieldValue> fields) {
        var ctx = new FactoryContext(null, null);
        // FactoryContext.getFields() returns a fresh map if null, so inject directly via reflection.
        try {
            Field f = FactoryContext.class.getDeclaredField("fields");
            f.setAccessible(true);
            f.set(ctx, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ctx;
    }

    private TwinClassFieldEntity field(UUID id) {
        return new TwinClassFieldEntity().setId(id).setTwinClassId(UUID.randomUUID());
    }

    @Nested
    class Fill {

        @Test
        void fill_matchingFields_clonedIntoOutput() throws ServiceException {
            // NAME promises: every context field whose class is-instance-of output class is cloned into output.
            var fieldId = UUID.randomUUID();
            var srcValue = new FieldValueText(field(fieldId)).setValue("v");
            Map<UUID, FieldValue> contextFields = new HashMap<>();
            contextFields.put(fieldId, srcValue);
            var outputClass = new TwinClassEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(outputClass, contextFields);
            when(twinClassFieldService.isValidForClass(eq(outputClass), any(org.twins.core.dao.twinclass.TwinClassFieldEntity.class))).thenReturn(true);

            filler.fill(new Properties(), factoryItem, null);

            var cloned = factoryItem.getOutput().getField(fieldId);
            assertNotNull(cloned);
            assertEquals("v", ((FieldValueText) cloned).getValue());
            // clone must be a different instance (clone, not the original reference)
            assertNotSame(srcValue, cloned);
        }

        @Test
        void fill_emptyContextFields_throwsStepError() {
            // NAME + error message ("No context fields present. Please check pipeline config") imply fill must
            // throw FACTORY_PIPELINE_STEP_ERROR when the context carries no fields. FactoryContext.getFields()
            // lazily creates a fresh empty map (never returns null), so the guard checks isEmpty, not == null.
            var outputClass = new TwinClassEntity().setId(UUID.randomUUID());
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity().setTwinClass(outputClass));
            var factoryItem = new FactoryItem().setOutput(output).setFactoryContext(new FactoryContext(null, null));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_fieldNotInstanceOfOutput_isSkipped() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var srcValue = new FieldValueText(field(fieldId)).setValue("v");
            Map<UUID, FieldValue> contextFields = new HashMap<>();
            contextFields.put(fieldId, srcValue);
            var outputClass = new TwinClassEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(outputClass, contextFields);
            when(twinClassFieldService.isValidForClass(eq(outputClass), any(org.twins.core.dao.twinclass.TwinClassFieldEntity.class))).thenReturn(false);

            filler.fill(new Properties(), factoryItem, null);

            assertNull(factoryItem.getOutput().getField(fieldId));
        }
    }
}
