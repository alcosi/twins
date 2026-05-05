package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorParam;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorParamTest extends BaseUnitTest {

    private final ConditionEvaluatorParam evaluator = new ConditionEvaluatorParam();
    private TwinClassFieldEntity field;
    private TwinClassFieldConditionEntity condition;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
        condition = new TwinClassFieldConditionEntity();
    }

    private Properties props(TwinClassFieldConditionOperator operator, String compareWith, String paramKey) {
        var props = new Properties();
        props.put(ConditionEvaluator.CONDITION_OPERATOR, operator.name());
        props.put(ConditionEvaluator.VALUE_TO_COMPARE_WITH, compareWith != null ? compareWith : "");
        if (paramKey != null)
            props.put("evaluatedParamKey", paramKey);

        return props;
    }

    @Nested
    class Evaluate {

        @Test
        void evaluate_alwaysThrowsNotImplemented() {
            var props = props(TwinClassFieldConditionOperator.eq, "hello", "myKey");
            var value = new FieldValueText(field).setValue("hello");

            assertThrows(ServiceException.class, () -> evaluator.evaluate(condition, props, value));
        }
    }

    @Nested
    class GetDescriptorType {

        @Test
        void getDescriptorType_returnsConditionDescriptorParam() {
            assertEquals(ConditionDescriptorParam.class, evaluator.getDescriptorType());
        }
    }

    @Nested
    class GetConditionDescriptor {

        @Test
        void getConditionDescriptor_buildsAllFields() throws ServiceException {
            var descriptor = evaluator.getConditionDescriptor(
                    condition,
                    props(TwinClassFieldConditionOperator.eq, "expected", "myParamKey")
            );

            assertEquals("myParamKey", descriptor.evaluatedParamKey());
            assertEquals("expected", descriptor.valueToCompareWith());
            assertEquals(TwinClassFieldConditionOperator.eq, descriptor.conditionOperator());
        }

        @Test
        void getConditionDescriptor_nullParamKey_setsNullKey() throws ServiceException {
            var descriptor = evaluator.getConditionDescriptor(
                    condition,
                    props(TwinClassFieldConditionOperator.gt, "10", null)
            );

            assertNull(descriptor.evaluatedParamKey());
            assertEquals("10", descriptor.valueToCompareWith());
        }
    }
}
