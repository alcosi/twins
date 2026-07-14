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
import org.twins.core.featurer.factory.filler.FillerFieldMathDivisionFromContextField;
import org.twins.core.featurer.factory.lookuper.FieldLookuperFromItemOutputDbFields;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class FillerFieldMathDivisionFromContextFieldTest extends BaseUnitTest {

    @Mock
    private FieldLookupers fieldLookupers;

    @Mock
    private FieldLookuperFromItemOutputDbFields dbLookuper;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private FillerFieldMathDivisionFromContextField filler;

    private static final UUID DIVIDEND_FIELD_ID = UUID.randomUUID();
    private static final UUID DIVISOR_FIELD_ID = UUID.randomUUID();
    private static final UUID TARGET_FIELD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerFieldMathDivisionFromContextField(twinClassFieldService);
        inject(filler, "fieldLookupers", fieldLookupers);
        // NOTE: getFromItemOutputDbFields() is stubbed per-test (only the divisor-missing path consults it);
        // stubbing it in @BeforeEach would trip strict-stubbing on the happy-path tests.
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

    private Properties props() {
        var p = new Properties();
        p.setProperty("dividendTwinClassFieldId", DIVIDEND_FIELD_ID.toString());
        p.setProperty("divisorTwinClassFieldId", DIVISOR_FIELD_ID.toString());
        p.setProperty("targetTwinClassFieldId", TARGET_FIELD_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem(FieldValue... outputFields) {
        var twinClass = new TwinClassEntity().setId(UUID.randomUUID());
        var twin = new TwinEntity().setTwinClass(twinClass);
        var output = new TwinCreate();
        output.setTwinEntity(twin);
        for (FieldValue fv : outputFields) {
            output.addField(fv);
        }
        return new FactoryItem().setOutput(output);
    }

    private TwinClassFieldEntity field(UUID id) {
        return new TwinClassFieldEntity().setId(id).setTwinClassId(UUID.randomUUID());
    }

    @Nested
    class Fill {

        @Test
        void fill_dividesOutputDividendByOutputDivisor_writesToTarget() throws ServiceException {
            // NAME promises: DIVISION = OUTPUT's dividend / OUTPUT's divisor (HALF_UP, scale 2), written to TARGET.
            var dividend = new FieldValueText(field(DIVIDEND_FIELD_ID)).setValue("10");
            var divisor = new FieldValueText(field(DIVISOR_FIELD_ID)).setValue("4");
            var target = new FieldValueText(field(TARGET_FIELD_ID));
            var factoryItem = buildFactoryItem(dividend, divisor, target);

            filler.fill(props(), List.of(factoryItem), null, false);

            FieldValueText result = (FieldValueText) factoryItem.getOutput().getField(TARGET_FIELD_ID);
            // 10 / 4 = 2.50 (HALF_UP, scale 2)
            assertEquals(new BigDecimal("2.50"), new BigDecimal(result.getValue()));
        }

        @Test
        void fill_divisorMissing_throwsStepError() throws ServiceException {
            var dividend = new FieldValueText(field(DIVIDEND_FIELD_ID)).setValue("10");
            var target = new FieldValueText(field(TARGET_FIELD_ID));
            var factoryItem = buildFactoryItem(dividend, target);
            // divisor not on output -> code calls dbLookuper; stub null.
            when(fieldLookupers.getFromItemOutputDbFields()).thenReturn(dbLookuper);
            when(dbLookuper.lookupFieldValue(factoryItem, DIVISOR_FIELD_ID)).thenReturn(null);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_divisionByZero_throwsStepError() throws ServiceException {
            var dividend = new FieldValueText(field(DIVIDEND_FIELD_ID)).setValue("10");
            var divisor = new FieldValueText(field(DIVISOR_FIELD_ID)).setValue("0");
            var target = new FieldValueText(field(TARGET_FIELD_ID));
            var factoryItem = buildFactoryItem(dividend, divisor, target);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_divisorNotTextField_throwsStepError() throws ServiceException {
            var dividend = new FieldValueText(field(DIVIDEND_FIELD_ID)).setValue("10");
            // divisor on output but not a FieldValueText
            var nonTextDivisor = new org.twins.core.featurer.fieldtyper.value.FieldValueUser(field(DIVISOR_FIELD_ID));
            var target = new FieldValueText(field(TARGET_FIELD_ID));
            var factoryItem = buildFactoryItem(dividend, nonTextDivisor, target);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_targetNotTextField_throwsStepError() throws ServiceException {
            // Target present on output but not a FieldValueText -> FACTORY_PIPELINE_STEP_ERROR.
            var dividend = new FieldValueText(field(DIVIDEND_FIELD_ID)).setValue("10");
            var divisor = new FieldValueText(field(DIVISOR_FIELD_ID)).setValue("4");
            var nonTextTarget = new org.twins.core.featurer.fieldtyper.value.FieldValueUser(field(TARGET_FIELD_ID));
            var factoryItem = buildFactoryItem(dividend, divisor, nonTextTarget);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
