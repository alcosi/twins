package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorValueTest extends BaseUnitTest {

    private final ConditionEvaluatorValue evaluator = new ConditionEvaluatorValue();
    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
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

            assertTrue(evaluator.evaluate(props(TwinClassFieldConditionOperator.eq, "hello"), value));
        }

        @Test
        void evaluate_textValue_eq_mismatch_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello");

            assertFalse(evaluator.evaluate(props(TwinClassFieldConditionOperator.eq, "world"), value));
        }

        @Test
        void evaluate_textValue_eq_caseInsensitive_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("Hello");

            assertTrue(evaluator.evaluate(props(TwinClassFieldConditionOperator.eq, "HELLO"), value));
        }

        @Test
        void evaluate_clearedTextValue_eq_null_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue(null);

            assertTrue(evaluator.evaluate(props(TwinClassFieldConditionOperator.eq, "null"), value));
        }

        @Test
        void evaluate_numberValue_eq_equalDecimalString_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("10");

            assertTrue(evaluator.evaluate(props(TwinClassFieldConditionOperator.eq, "10.0"), value));
        }

        @Test
        void evaluate_numberValue_eq_different_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("10");

            assertFalse(evaluator.evaluate(props(TwinClassFieldConditionOperator.eq, "20"), value));
        }

        @Test
        void evaluate_numberValue_eq_numberVsText_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("10");

            assertFalse(evaluator.evaluate(props(TwinClassFieldConditionOperator.eq, "abc"), value));
        }

        @Test
        void evaluate_numberValue_neq_equalDecimalString_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("10");

            assertFalse(evaluator.evaluate(props(TwinClassFieldConditionOperator.neq, "10.0"), value));
        }

        @Test
        void evaluate_numberValue_neq_different_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("10");

            assertTrue(evaluator.evaluate(props(TwinClassFieldConditionOperator.neq, "20"), value));
        }

        @Test
        void evaluate_textValue_neq_different_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello");

            assertTrue(evaluator.evaluate(props(TwinClassFieldConditionOperator.neq, "world"), value));
        }

        @Test
        void evaluate_textValue_neq_same_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello");

            assertFalse(evaluator.evaluate(props(TwinClassFieldConditionOperator.neq, "hello"), value));
        }

        @Test
        void evaluate_textValue_lt_smallerActual_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("5");

            assertTrue(evaluator.evaluate(props(TwinClassFieldConditionOperator.lt, "10"), value));
        }

        @Test
        void evaluate_textValue_lt_largerActual_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("15");

            assertFalse(evaluator.evaluate(props(TwinClassFieldConditionOperator.lt, "10"), value));
        }

        @Test
        void evaluate_textValue_gt_largerActual_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("15");

            assertTrue(evaluator.evaluate(props(TwinClassFieldConditionOperator.gt, "10"), value));
        }

        @Test
        void evaluate_textValue_contains_substring_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello world");

            assertTrue(evaluator.evaluate(props(TwinClassFieldConditionOperator.contains, "world"), value));
        }

        @Test
        void evaluate_textValue_contains_absent_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("hello world");

            assertFalse(evaluator.evaluate(props(TwinClassFieldConditionOperator.contains, "xyz"), value));
        }

        @Test
        void evaluate_textValue_in_valueInSet_returnsTrue() throws ServiceException {
            var value = new FieldValueText(field).setValue("b");

            assertTrue(evaluator.evaluate(props(TwinClassFieldConditionOperator.in, "a,b,c"), value));
        }

        @Test
        void evaluate_textValue_in_valueNotInSet_returnsFalse() throws ServiceException {
            var value = new FieldValueText(field).setValue("d");

            assertFalse(evaluator.evaluate(props(TwinClassFieldConditionOperator.in, "a,b,c"), value));
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
            var descriptor = evaluator.getConditionDescriptor(props(TwinClassFieldConditionOperator.neq, "expected"));

            assertEquals(TwinClassFieldConditionOperator.neq, descriptor.conditionOperator());
            assertEquals("expected", descriptor.valueToCompareWith());
        }
    }
}
