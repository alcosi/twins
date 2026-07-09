package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;
import org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluator;
import org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluatorDataListOptionExternalId;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorDataListOptionExternalId;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorDataListOptionExternalIdTest extends BaseUnitTest {

    private final ConditionEvaluatorDataListOptionExternalId evaluator = new ConditionEvaluatorDataListOptionExternalId();
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

    private DataListOptionEntity option(String externalId) {
        return new DataListOptionEntity()
                .setId(UUID.randomUUID())
                .setExternalId(externalId);
    }

    @Nested
    class Evaluate {

        @Test
        void evaluate_nonSelectValue_throwsServiceException() {
            var value = new FieldValueText(field).setValue("hello");

            assertThrows(ServiceException.class,
                    () -> evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.eq, "ext1"), value));
        }

        @Test
        void evaluate_selectWithMatchingExternalId_eq_returnsTrue() throws ServiceException {
            var select = new FieldValueSelect(field);
            select.add(option("ext1"));

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.eq, "ext1"), select));
        }

        @Test
        void evaluate_selectWithDifferentExternalId_eq_returnsFalse() throws ServiceException {
            var select = new FieldValueSelect(field);
            select.add(option("ext1"));

            assertFalse(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.eq, "ext2"), select));
        }

        @Test
        void evaluate_emptySelect_eq_null_returnsTrue() throws ServiceException {
            var select = new FieldValueSelect(field);

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.eq, "null"), select));
        }

        @Test
        void evaluate_selectWithMultipleOptions_in_anyMatches_returnsTrue() throws ServiceException {
            var select = new FieldValueSelect(field);
            select.add(option("ext2"));

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.in, "ext1,ext2,ext3"), select));
        }

        @Test
        void evaluate_selectWithMultipleOptions_in_noneMatch_returnsFalse() throws ServiceException {
            var select = new FieldValueSelect(field);
            select.add(option("ext4"));

            assertFalse(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.in, "ext1,ext2,ext3"), select));
        }

        @Test
        void evaluate_selectOptionWithBlankExternalId_treatedAsNull() throws ServiceException {
            var select = new FieldValueSelect(field);
            select.add(option("   "));

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.eq, "null"), select));
        }

        @Test
        void evaluate_selectOptionWithNullExternalId_treatedAsNull() throws ServiceException {
            var select = new FieldValueSelect(field);
            select.add(option(null));

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.eq, "null"), select));
        }

        @Test
        void evaluate_selectWithTwoOptions_contains_matchesJoinedExternalIds_returnsTrue() throws ServiceException {
            var select = new FieldValueSelect(field);
            select.add(option("ext1"));
            select.add(option("ext2"));

            assertTrue(evaluator.evaluate(condition, props(TwinClassFieldConditionOperator.contains, "ext2"), select));
        }
    }

    @Nested
    class GetDescriptorType {

        @Test
        void getDescriptorType_returnsConditionDescriptorDataListOptionExternalId() {
            assertEquals(ConditionDescriptorDataListOptionExternalId.class, evaluator.getDescriptorType());
        }
    }

    @Nested
    class GetConditionDescriptor {

        @Test
        void getConditionDescriptor_buildsDescriptorWithOperatorAndValue() throws ServiceException {
            var descriptor = evaluator.getConditionDescriptor(
                    condition,
                    props(TwinClassFieldConditionOperator.in, "ext1,ext2")
            );

            assertEquals(TwinClassFieldConditionOperator.in, descriptor.conditionOperator());
            assertEquals("ext1,ext2", descriptor.valueToCompareWith());
        }
    }
}
