package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerFieldsFromTemplateTwinAll;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

class FillerFieldsFromTemplateTwinAllTest extends BaseUnitTest {

    @Mock
    private TwinClassService twinClassService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    @Mock
    private TwinService twinService;

    private FillerFieldsFromTemplateTwinAll filler;

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldsFromTemplateTwinAll();
        inject(filler, "twinClassService", twinClassService);
        inject(filler, "twinClassFieldService", twinClassFieldService);
        inject(filler, "twinService", twinService);
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

    private FactoryItem buildFactoryItem(TwinClassEntity outputClass) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity().setTwinClass(outputClass));
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity field(UUID id) {
        return new TwinClassFieldEntity().setId(id).setTwinClassId(UUID.randomUUID());
    }

    @Nested
    class Fill {

        @Test
        void fill_outputInstanceOfTemplate_clonesFilledTemplateFields() throws ServiceException {
            // NAME promises: every FILLED template-twin field whose class matches is cloned into the output.
            var fieldId = UUID.randomUUID();
            var srcField = field(fieldId);
            var templateClass = new TwinClassEntity();
            templateClass.setTwinClassFieldKit(new Kit<>(List.of(srcField), TwinClassFieldEntity::getId));
            var templateTwin = new TwinEntity().setTwinClass(templateClass).setTwinClassId(UUID.randomUUID());
            var outputClass = new TwinClassEntity();
            var factoryItem = buildFactoryItem(outputClass);
            when(twinClassService.isInstanceOf(eq(outputClass), eq(templateTwin.getTwinClassId()))).thenReturn(true);
            var value = new FieldValueText(srcField).setValue("v");
            when(twinService.getTwinFieldValue(templateTwin, srcField)).thenReturn(value);

            filler.fill(new Properties(), List.of(factoryItem), templateTwin, false);

            var cloned = factoryItem.getOutput().getField(fieldId);
            assertNotNull(cloned);
            assertEquals("v", ((FieldValueText) cloned).getValue());
            assertNotSame(value, cloned);
        }

        @Test
        void fill_outputNotInstanceOfTemplate_throwsFactoryIncorrect() throws ServiceException {
            var templateClass = new TwinClassEntity();
            templateClass.setTwinClassFieldKit(new Kit<>(List.of(), TwinClassFieldEntity::getId));
            var templateTwin = new TwinEntity().setTwinClass(templateClass).setTwinClassId(UUID.randomUUID());
            var outputClass = new TwinClassEntity();
            var factoryItem = buildFactoryItem(outputClass);
            when(twinClassService.isInstanceOf(eq(outputClass), eq(templateTwin.getTwinClassId()))).thenReturn(false);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), List.of(factoryItem), templateTwin, false));
            assertEquals(ErrorCodeTwins.FACTORY_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_emptyTemplateClassFields_throwsStepError() throws ServiceException {
            var templateClass = new TwinClassEntity();
            templateClass.setTwinClassFieldKit(new Kit<>(List.of(), TwinClassFieldEntity::getId));
            var templateTwin = new TwinEntity().setTwinClass(templateClass).setTwinClassId(UUID.randomUUID());
            var outputClass = new TwinClassEntity();
            var factoryItem = buildFactoryItem(outputClass);
            when(twinClassService.isInstanceOf(eq(outputClass), eq(templateTwin.getTwinClassId()))).thenReturn(true);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), List.of(factoryItem), templateTwin, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_unfilledTemplateField_isSkipped() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var srcField = field(fieldId);
            var templateClass = new TwinClassEntity();
            templateClass.setTwinClassFieldKit(new Kit<>(List.of(srcField), TwinClassFieldEntity::getId));
            var templateTwin = new TwinEntity().setTwinClass(templateClass).setTwinClassId(UUID.randomUUID());
            var outputClass = new TwinClassEntity();
            var factoryItem = buildFactoryItem(outputClass);
            when(twinClassService.isInstanceOf(eq(outputClass), eq(templateTwin.getTwinClassId()))).thenReturn(true);
            // empty FieldValueText -> isFilled == false -> skipped
            var value = new FieldValueText(srcField);
            when(twinService.getTwinFieldValue(templateTwin, srcField)).thenReturn(value);

            filler.fill(new Properties(), List.of(factoryItem), templateTwin, false);

            assertNull(factoryItem.getOutput().getField(fieldId));
        }
    }
}
