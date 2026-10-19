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
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twin.TwinService;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FillerFieldMathSumFromContextFieldTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FillerFieldMathSumFromContextField filler;

    private static final UUID ADDEND_FIELD_ID = UUID.randomUUID(); // from context
    private static final UUID AUGEND_FIELD_ID = UUID.randomUUID(); // from output

    @BeforeEach
    void setUp() {
        // Math fillers use constructor injection (@RequiredArgsConstructor with final TwinService).
        filler = new FillerFieldMathSumFromContextField(twinService);
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

            filler.fill(props(false), factoryItem, null, addend);

            FieldValueText result = (FieldValueText) factoryItem.getOutput().getField(AUGEND_FIELD_ID);
            assertEquals(new BigDecimal("15"), new BigDecimal(result.getValue()));
        }

        @Test
        void fill_negativeSumNotAllowed_throwsStepError() throws ServiceException {
            var augend = new FieldValueText(field(AUGEND_FIELD_ID)).setValue("-10");
            var factoryItem = buildFactoryItem(augend);
            var addend = new FieldValueText(field(ADDEND_FIELD_ID)).setValue("5");

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), factoryItem, null, addend));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_negativeSumAllowed_writesNegativeResult() throws ServiceException {
            var augend = new FieldValueText(field(AUGEND_FIELD_ID)).setValue("-10");
            var factoryItem = buildFactoryItem(augend);
            var addend = new FieldValueText(field(ADDEND_FIELD_ID)).setValue("5");

            filler.fill(props(true), factoryItem, null, addend);

            FieldValueText result = (FieldValueText) factoryItem.getOutput().getField(AUGEND_FIELD_ID);
            assertEquals(new BigDecimal("-5"), new BigDecimal(result.getValue()));
        }

        @Test
        void fill_addendNotTextField_throwsStepError() throws ServiceException {
            // NAME + contract: both operands must be text-representable numbers.
            var augend = new FieldValueText(field(AUGEND_FIELD_ID)).setValue("10");
            var factoryItem = buildFactoryItem(augend);
            var nonText = new org.twins.core.featurer.fieldtyper.value.FieldValueUser(field(ADDEND_FIELD_ID));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), factoryItem, null, nonText));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_augendMissingOnCreate_throwsStepError() throws ServiceException {
            // On TwinCreate, missing augend on output is an error (cannot sum against unknown value).
            var factoryItem = buildFactoryItem(null);
            var addend = new FieldValueText(field(ADDEND_FIELD_ID)).setValue("5");

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), factoryItem, null, addend));
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

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), factoryItem, null, addend));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
