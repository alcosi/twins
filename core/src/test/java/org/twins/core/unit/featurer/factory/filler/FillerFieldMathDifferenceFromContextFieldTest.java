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
import org.twins.core.featurer.factory.filler.FillerFieldMathDifferenceFromContextField;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromContextFieldsAndContextTwinDbFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class FillerFieldMathDifferenceFromContextFieldTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromContextFieldsAndContextTwinDbFields lookuper;

    @Mock
    private TwinService twinService;

    private FillerFieldMathDifferenceFromContextField filler;

    private static final UUID MINUEND_FIELD_ID = UUID.randomUUID(); // on output
    private static final UUID SUBTRAHEND_FIELD_ID = UUID.randomUUID(); // from context

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldMathDifferenceFromContextField(twinService);
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
        p.setProperty("minuendTwinClassFieldId", MINUEND_FIELD_ID.toString());
        p.setProperty("subtrahendTwinClassFieldId", SUBTRAHEND_FIELD_ID.toString());
        p.setProperty("allowNegativeResult", Boolean.toString(allowNegative));
        return p;
    }

    private FactoryItem buildFactoryItem(FieldValueText minuendOnOutput) {
        var twinClass = new TwinClassEntity().setId(UUID.randomUUID());
        var twin = new TwinEntity().setTwinClass(twinClass);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        if (minuendOnOutput != null) {
            output.addField(minuendOnOutput);
        }
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity field(UUID id) {
        return new TwinClassFieldEntity().setId(id).setTwinClassId(UUID.randomUUID());
    }

    @Nested
    class Fill {

        @Test
        void fill_subtractsContextSubtrahendFromOutputMinuend_writesToOutput() throws ServiceException {
            // NAME promises: DIFFERENCE = OUTPUT's minuend - CONTEXT's subtrahend, written into the MINUEND field.
            var minuend = new FieldValueText(field(MINUEND_FIELD_ID)).setValue("10");
            var factoryItem = buildFactoryItem(minuend);
            var subtrahend = new FieldValueText(field(SUBTRAHEND_FIELD_ID)).setValue("3");
            when(lookuper.lookupFieldValue(factoryItem, SUBTRAHEND_FIELD_ID)).thenReturn(subtrahend);

            filler.fill(props(false), List.of(factoryItem), null, false);

            FieldValueText result = (FieldValueText) factoryItem.getOutput().getField(MINUEND_FIELD_ID);
            assertEquals(new BigDecimal("7"), new BigDecimal(result.getValue()));
        }

        @Test
        void fill_negativeDifferenceNotAllowed_throwsStepError() throws ServiceException {
            var minuend = new FieldValueText(field(MINUEND_FIELD_ID)).setValue("3");
            var factoryItem = buildFactoryItem(minuend);
            var subtrahend = new FieldValueText(field(SUBTRAHEND_FIELD_ID)).setValue("10");
            when(lookuper.lookupFieldValue(factoryItem, SUBTRAHEND_FIELD_ID)).thenReturn(subtrahend);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_negativeDifferenceAllowed_writesNegativeResult() throws ServiceException {
            var minuend = new FieldValueText(field(MINUEND_FIELD_ID)).setValue("3");
            var factoryItem = buildFactoryItem(minuend);
            var subtrahend = new FieldValueText(field(SUBTRAHEND_FIELD_ID)).setValue("10");
            when(lookuper.lookupFieldValue(factoryItem, SUBTRAHEND_FIELD_ID)).thenReturn(subtrahend);

            filler.fill(props(true), List.of(factoryItem), null, false);

            FieldValueText result = (FieldValueText) factoryItem.getOutput().getField(MINUEND_FIELD_ID);
            assertEquals(new BigDecimal("-7"), new BigDecimal(result.getValue()));
        }

        @Test
        void fill_subtrahendNotTextField_throwsStepError() throws ServiceException {
            var minuend = new FieldValueText(field(MINUEND_FIELD_ID)).setValue("10");
            var factoryItem = buildFactoryItem(minuend);
            var nonText = new org.twins.core.featurer.fieldtyper.value.FieldValueUser(field(SUBTRAHEND_FIELD_ID));
            when(lookuper.lookupFieldValue(factoryItem, SUBTRAHEND_FIELD_ID)).thenReturn(nonText);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_minuendMissingOnCreate_throwsStepError() throws ServiceException {
            var factoryItem = buildFactoryItem(null);
            var subtrahend = new FieldValueText(field(SUBTRAHEND_FIELD_ID)).setValue("3");
            when(lookuper.lookupFieldValue(factoryItem, SUBTRAHEND_FIELD_ID)).thenReturn(subtrahend);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_minuendNotTextField_throwsStepError() throws ServiceException {
            var nonTextMinuend = new org.twins.core.featurer.fieldtyper.value.FieldValueUser(field(MINUEND_FIELD_ID));
            var factoryItem = buildFactoryItem(null);
            factoryItem.getOutput().addField(nonTextMinuend);
            var subtrahend = new FieldValueText(field(SUBTRAHEND_FIELD_ID)).setValue("3");
            when(lookuper.lookupFieldValue(factoryItem, SUBTRAHEND_FIELD_ID)).thenReturn(subtrahend);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
