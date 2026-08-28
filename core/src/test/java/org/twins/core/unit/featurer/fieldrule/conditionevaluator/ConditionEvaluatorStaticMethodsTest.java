package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;
import org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluator;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorStaticMethodsTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    @Nested
    class EvaluateOperator {

        @Test
        void evaluateOperator_eq_sameValue_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("hello", TwinClassFieldConditionOperator.eq, "hello"));
        }

        @Test
        void evaluateOperator_eq_caseInsensitive_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("Hello", TwinClassFieldConditionOperator.eq, "HELLO"));
        }

        @Test
        void evaluateOperator_eq_differentValue_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("hello", TwinClassFieldConditionOperator.eq, "world"));
        }

        @Test
        void evaluateOperator_eq_nullActual_nullExpected_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator(null, TwinClassFieldConditionOperator.eq, "null"));
        }

        @Test
        void evaluateOperator_eq_emptyActual_nullExpected_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("", TwinClassFieldConditionOperator.eq, "null"));
        }

        @Test
        void evaluateOperator_eq_nullActual_nonNullExpected_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator(null, TwinClassFieldConditionOperator.eq, "hello"));
        }

        @Test
        void evaluateOperator_neq_differentValues_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("hello", TwinClassFieldConditionOperator.neq, "world"));
        }

        @Test
        void evaluateOperator_neq_sameValue_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("hello", TwinClassFieldConditionOperator.neq, "hello"));
        }

        @Test
        void evaluateOperator_neq_nullActual_nullExpected_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator(null, TwinClassFieldConditionOperator.neq, "null"));
        }

        @Test
        void evaluateOperator_neq_nullActual_nonNullExpected_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator(null, TwinClassFieldConditionOperator.neq, "hello"));
        }

        @Test
        void evaluateOperator_lt_smallerActual_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("5", TwinClassFieldConditionOperator.lt, "10"));
        }

        @Test
        void evaluateOperator_lt_largerActual_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("15", TwinClassFieldConditionOperator.lt, "10"));
        }

        @Test
        void evaluateOperator_lt_equalValues_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("10", TwinClassFieldConditionOperator.lt, "10"));
        }

        @Test
        void evaluateOperator_lt_nonNumericActual_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("abc", TwinClassFieldConditionOperator.lt, "10"));
        }

        @Test
        void evaluateOperator_lt_blankActual_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("", TwinClassFieldConditionOperator.lt, "10"));
        }

        @Test
        void evaluateOperator_gt_largerActual_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("15", TwinClassFieldConditionOperator.gt, "10"));
        }

        @Test
        void evaluateOperator_gt_smallerActual_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("5", TwinClassFieldConditionOperator.gt, "10"));
        }

        @Test
        void evaluateOperator_gt_equalValues_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("10", TwinClassFieldConditionOperator.gt, "10"));
        }

        @Test
        void evaluateOperator_contains_substringPresent_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("hello world", TwinClassFieldConditionOperator.contains, "world"));
        }

        @Test
        void evaluateOperator_contains_caseInsensitive_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("Hello World", TwinClassFieldConditionOperator.contains, "world"));
        }

        @Test
        void evaluateOperator_contains_substringAbsent_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("hello world", TwinClassFieldConditionOperator.contains, "xyz"));
        }

        @Test
        void evaluateOperator_contains_nullActual_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator(null, TwinClassFieldConditionOperator.contains, "hello"));
        }

        @Test
        void evaluateOperator_in_valueInCommaSet_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("b", TwinClassFieldConditionOperator.in, "a,b,c"));
        }

        @Test
        void evaluateOperator_in_valueInSemicolonSet_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("b", TwinClassFieldConditionOperator.in, "a;b;c"));
        }

        @Test
        void evaluateOperator_in_valueNotInSet_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("d", TwinClassFieldConditionOperator.in, "a,b,c"));
        }

        @Test
        void evaluateOperator_in_nullActual_nullInSet_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator(null, TwinClassFieldConditionOperator.in, "null,a,b"));
        }

        @Test
        void evaluateOperator_in_emptyExpected_returnsFalse() {
            assertFalse(ConditionEvaluator.evaluateOperator("a", TwinClassFieldConditionOperator.in, ""));
        }

        @Test
        void evaluateOperator_in_multiValueActual_anyMatchesSet_returnsTrue() {
            assertTrue(ConditionEvaluator.evaluateOperator("a,c", TwinClassFieldConditionOperator.in, "c,d,e"));
        }
    }

    @Nested
    class CompareNumbers {

        @Test
        void compareNumbers_equalValues_returnsZero() {
            assertEquals(0, ConditionEvaluator.compareNumbers("5", "5"));
        }

        @Test
        void compareNumbers_actualSmaller_returnsNegative() {
            assertTrue(ConditionEvaluator.compareNumbers("3", "5") < 0);
        }

        @Test
        void compareNumbers_actualLarger_returnsPositive() {
            assertTrue(ConditionEvaluator.compareNumbers("10", "5") > 0);
        }

        @Test
        void compareNumbers_decimalValues_returnsCorrectSign() {
            assertTrue(ConditionEvaluator.compareNumbers("1.5", "1.6") < 0);
        }

        @Test
        void compareNumbers_blankActual_returnsNull() {
            assertNull(ConditionEvaluator.compareNumbers("", "5"));
        }

        @Test
        void compareNumbers_blankExpected_returnsNull() {
            assertNull(ConditionEvaluator.compareNumbers("5", "  "));
        }

        @Test
        void compareNumbers_nonNumericActual_returnsNull() {
            assertNull(ConditionEvaluator.compareNumbers("abc", "5"));
        }

        @Test
        void compareNumbers_whitespaceAroundNumbers_handledCorrectly() {
            assertEquals(0, ConditionEvaluator.compareNumbers(" 5 ", " 5 "));
        }
    }

    @Nested
    class SplitValues {

        @Test
        void splitValues_commaSeparated_returnsAllParts() {
            var result = ConditionEvaluator.splitValues("a,b,c");

            assertEquals(3, result.size());
            assertTrue(result.contains("a") && result.contains("b") && result.contains("c"));
        }

        @Test
        void splitValues_semicolonSeparated_returnsAllParts() {
            var result = ConditionEvaluator.splitValues("a;b;c");

            assertEquals(3, result.size());
        }

        @Test
        void splitValues_mixedSeparators_returnsAllParts() {
            var result = ConditionEvaluator.splitValues("a,b;c");

            assertEquals(3, result.size());
        }

        @Test
        void splitValues_blank_returnsEmpty() {
            assertTrue(ConditionEvaluator.splitValues("   ").isEmpty());
        }

        @Test
        void splitValues_null_returnsEmpty() {
            assertTrue(ConditionEvaluator.splitValues(null).isEmpty());
        }

        @Test
        void splitValues_uppercaseValues_lowercasedInResult() {
            var result = ConditionEvaluator.splitValues("ABC,DEF");

            assertTrue(result.contains("abc") && result.contains("def"));
        }

        @Test
        void splitValues_whitespaceAroundParts_trimmed() {
            var result = ConditionEvaluator.splitValues(" a , b ");

            assertTrue(result.contains("a") && result.contains("b"));
        }

        @Test
        void splitValues_emptyParts_skipped() {
            var result = ConditionEvaluator.splitValues("a,,b");

            assertEquals(2, result.size());
        }
    }

    @Nested
    class NormalizeValue {

        @Test
        void normalizeValue_null_returnsNull() {
            assertNull(ConditionEvaluator.normalizeValue(null));
        }

        @Test
        void normalizeValue_plainString_returnsSameString() {
            assertEquals("hello", ConditionEvaluator.normalizeValue("hello"));
        }

        @Test
        void normalizeValue_booleanTrue_returnsTrue() {
            assertEquals("true", ConditionEvaluator.normalizeValue(true));
        }

        @Test
        void normalizeValue_booleanFalse_returnsFalse() {
            assertEquals("false", ConditionEvaluator.normalizeValue(false));
        }

        @Test
        void normalizeValue_fieldValueText_returnsInnerValue() {
            var value = new FieldValueText(field).setValue("inner");

            assertEquals("inner", ConditionEvaluator.normalizeValue(value));
        }

        @Test
        void normalizeValue_clearedFieldValueText_returnsNull() {
            var value = new FieldValueText(field).setValue(null);

            assertNull(ConditionEvaluator.normalizeValue(value));
        }

        @Test
        void normalizeValue_fieldValueBoolean_returnsString() {
            var value = new FieldValueBoolean(field).setValue(true);

            assertEquals("true", ConditionEvaluator.normalizeValue(value));
        }

        @Test
        void normalizeValue_dataListOptionWithId_returnsIdString() {
            var id = UUID.randomUUID();
            var option = new DataListOptionEntity().setId(id);

            assertEquals(id.toString(), ConditionEvaluator.normalizeValue(option));
        }

        @Test
        void normalizeValue_dataListOptionNullId_returnsNull() {
            assertNull(ConditionEvaluator.normalizeValue(new DataListOptionEntity()));
        }

        @Test
        void normalizeValue_userEntityWithId_returnsIdString() {
            var id = UUID.randomUUID();
            var user = new UserEntity();
            user.setId(id);

            assertEquals(id.toString(), ConditionEvaluator.normalizeValue(user));
        }

        @Test
        void normalizeValue_collectionOfStrings_returnsJoined() {
            assertEquals("a,b,c", ConditionEvaluator.normalizeValue(List.of("a", "b", "c")));
        }

        @Test
        void normalizeValue_collectionWithNulls_nullsSkipped() {
            var list = new ArrayList<>();
            list.add("a");
            list.add(null);
            list.add("b");

            assertEquals("a,b", ConditionEvaluator.normalizeValue(list));
        }
    }
}
