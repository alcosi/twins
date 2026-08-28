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
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerFieldMathSumFromContextField;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFieldsAndContextTwinDbFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerFieldMathSumFromContextFieldTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextFieldsAndContextTwinDbFields lookuper;

    @Mock
    private TwinService twinService;

    private FillerFieldMathSumFromContextField filler;

    private static final UUID ADDEND_FIELD_ID = UUID.randomUUID(); // from context
    private static final UUID AUGEND_FIELD_ID = UUID.randomUUID(); // from output

    @BeforeEach
    void setUp() throws Exception {
        // Math fillers use constructor injection (@RequiredArgsConstructor with final TwinService).
        filler = new FillerFieldMathSumFromContextField(twinService);
        inject(filler, "fieldLookupers", fieldLookupers);
        when(fieldLookupers.getFromContextFieldsAndContextTwinDbFields()).thenReturn(lookuper);
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

    private Properties props(boolean allowNegative) {
        var p = new Properties();
        p.setProperty("addendTwinClassFieldId", ADDEND_FIELD_ID.toString());
        p.setProperty("augendTwinClassFieldId", AUGEND_FIELD_ID.toString());
        p.setProperty("allowNegativeResult", Boolean.toString(allowNegative));
        return p;
    }

    private FactoryItem buildFactoryItem(FieldValueText augendOnOutput) {
        var twinClass = new TwinClassEntity().setId(UUID.randomUUID());
        var twin = new TwinEntity().setTwinClass(twinClass);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        if (augendOnOutput != null) {
            output.addField(augendOnOutput);
        }
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity field(UUID id) {
        return new TwinClassFieldEntity().setId(id).setTwinClassId(UUID.randomUUID());
    }

    @Nested
    class Fill {

        @Test
        void fill_sumsContextAddendWithOutputAugend_writesToOutput() throws ServiceException {
            // NAME promises: SUM = OUTPUT's augend + CONTEXT's addend, written into the AUGEND field on output.
            var augend = new FieldValueText(field(AUGEND_FIELD_ID)).setValue("10");
            var factoryItem = buildFactoryItem(augend);
            var addend = new FieldValueText(field(ADDEND_FIELD_ID)).setValue("5");
            when(lookuper.lookupFieldValue(factoryItem, ADDEND_FIELD_ID)).thenReturn(addend);

            filler.fill(props(false), factoryItem, null);

            FieldValueText result = (FieldValueText) factoryItem.getOutput().getField(AUGEND_FIELD_ID);
            assertEquals(new BigDecimal("15"), new BigDecimal(result.getValue()));
        }

        @Test
        void fill_negativeSumNotAllowed_throwsStepError() throws ServiceException {
            var augend = new FieldValueText(field(AUGEND_FIELD_ID)).setValue("-10");
            var factoryItem = buildFactoryItem(augend);
            var addend = new FieldValueText(field(ADDEND_FIELD_ID)).setValue("5");
            when(lookuper.lookupFieldValue(factoryItem, ADDEND_FIELD_ID)).thenReturn(addend);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_negativeSumAllowed_writesNegativeResult() throws ServiceException {
            var augend = new FieldValueText(field(AUGEND_FIELD_ID)).setValue("-10");
            var factoryItem = buildFactoryItem(augend);
            var addend = new FieldValueText(field(ADDEND_FIELD_ID)).setValue("5");
            when(lookuper.lookupFieldValue(factoryItem, ADDEND_FIELD_ID)).thenReturn(addend);

            filler.fill(props(true), factoryItem, null);

            FieldValueText result = (FieldValueText) factoryItem.getOutput().getField(AUGEND_FIELD_ID);
            assertEquals(new BigDecimal("-5"), new BigDecimal(result.getValue()));
        }

        @Test
        void fill_addendNotTextField_throwsStepError() throws ServiceException {
            // NAME + contract: both operands must be text-representable numbers.
            var augend = new FieldValueText(field(AUGEND_FIELD_ID)).setValue("10");
            var factoryItem = buildFactoryItem(augend);
            // Return a non-FieldValueText from the lookuper by stubbing with a spy that returns a marker subclass.
            // Simpler: stub lookuper to return a FieldValueText that we cast — to exercise the not-text branch we
            // pass a FieldValueUser-like value via a FieldValueText subclass is not possible; instead rely on the
            // instanceof check using a real non-text FieldValue.
            var nonText = new org.twins.core.featurer.fieldtyper.value.FieldValueUser(field(ADDEND_FIELD_ID));
            when(lookuper.lookupFieldValue(factoryItem, ADDEND_FIELD_ID)).thenReturn(nonText);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_augendMissingOnCreate_throwsStepError() throws ServiceException {
            // On TwinCreate, missing augend on output is an error (cannot sum against unknown value).
            var factoryItem = buildFactoryItem(null);
            var addend = new FieldValueText(field(ADDEND_FIELD_ID)).setValue("5");
            when(lookuper.lookupFieldValue(factoryItem, ADDEND_FIELD_ID)).thenReturn(addend);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_augendNotTextField_throwsStepError() throws ServiceException {
            // Augend present on output but is not a FieldValueText -> FACTORY_PIPELINE_STEP_ERROR.
            // FieldValueText is the only currently-supported concrete type; place a non-text FieldValue in output.
            var nonTextAugend = new org.twins.core.featurer.fieldtyper.value.FieldValueUser(field(AUGEND_FIELD_ID));
            var factoryItem = buildFactoryItem(null);
            factoryItem.getOutput().addField(nonTextAugend);
            var addend = new FieldValueText(field(ADDEND_FIELD_ID)).setValue("5");
            when(lookuper.lookupFieldValue(factoryItem, ADDEND_FIELD_ID)).thenReturn(addend);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
