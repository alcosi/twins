package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;
import org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluator;
import org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluatorValue;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorValueTest extends BaseUnitTest {

    private final ConditionEvaluatorValue evaluator = new ConditionEvaluatorValue();
    private TwinClassFieldEntity field;
    private TwinClassFieldConditionEntity condition;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
        condition = new TwinClassFieldConditionEntity();
    }

    private Properties props(TwinClassFieldConditionOperator operator, String compareWith) {
        var props = new Properties();
        props.put(ConditionEvaluator.CONDITION_OPERATOR, operator.name());
        props.put(ConditionEvaluator.VALUE_TO_COMPARE_WITH, compareWith != null ? compareWith : "");

        return props;
    }

    @Nested
    class Evaluate {

        @Test
        void evaluate_textValue_eq_match_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello");

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.eq, "hello"), value));
        }

        @Test
        void evaluate_textValue_eq_mismatch_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello");

            assertFalse(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.eq, "world"), value));
        }

        @Test
        void evaluate_textValue_eq_caseInsensitive_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("Hello");

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.eq, "HELLO"), value));
        }

        @Test
        void evaluate_clearedTextValue_eq_null_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue(null);

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.eq, "null"), value));
        }

        @Test
        void evaluate_textValue_neq_different_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello");

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.neq, "world"), value));
        }

        @Test
        void evaluate_textValue_neq_same_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello");

            assertFalse(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.neq, "hello"), value));
        }

        @Test
        void evaluate_textValue_lt_smallerActual_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("5");

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.lt, "10"), value));
        }

        @Test
        void evaluate_textValue_lt_largerActual_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("15");

            assertFalse(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.lt, "10"), value));
        }

        @Test
        void evaluate_textValue_gt_largerActual_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("15");

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.gt, "10"), value));
        }

        @Test
        void evaluate_textValue_contains_substring_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello world");

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.contains, "world"), value));
        }

        @Test
        void evaluate_textValue_contains_absent_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello world");

            assertFalse(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.contains, "xyz"), value));
        }

        @Test
        void evaluate_textValue_in_valueInSet_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("b");

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.in, "a,b,c"), value));
        }

        @Test
        void evaluate_textValue_in_valueNotInSet_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("d");

            assertFalse(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.in, "a,b,c"), value));
        }
    }

    @Nested
    class GetDescriptorType {

        @Test
        void getDescriptorType_returnsConditionDescriptorValue() {
            assertEquals(ConditionDescriptorValue.class, evaluator.getDescriptorType());
        }
    }

    @Nested
    class GetConditionDescriptor {

        @Test
        void getConditionDescriptor_buildsDescriptorWithOperatorAndValue() throws ServiceException {
            var descriptor = evaluator.getConditionDescriptor(condition, props(TwinClassFieldConditionOperator.neq, "expected"));

            assertEquals(TwinClassFieldConditionOperator.neq, descriptor.conditionOperator());
            assertEquals("expected", descriptor.valueToCompareWith());
        }
    }
}
