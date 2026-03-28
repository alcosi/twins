package org.twins.core.service.twinclass;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.enums.twinclass.LogicOperator;

import java.util.*;

class TwinClassFieldRuleExecutionServiceTest {

        private final TwinClassFieldRuleExecutionService service = new TwinClassFieldRuleExecutionService();

        @Test
        void testSimpleEqCondition() {
                UUID fieldId = UUID.randomUUID();
                TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);

                // Rule: if field == "A", make required = true
                TwinClassFieldRuleEntity rule = new TwinClassFieldRuleEntity()
                        .setId(UUID.randomUUID())
                        .setOverwrittenRequired(true)
                        .setRulePriority(1);

                TwinClassFieldConditionEntity condition = new TwinClassFieldConditionEntity()
                        .setId(UUID.randomUUID())
                        .setLogicOperatorId(LogicOperator.LEAF)
                        .setBaseTwinClassFieldId(fieldId)
                        .setConditionEvaluatorParams(
                                new HashMap<>(Map.of("conditionOperator", "eq", "valueToCompareWith",
                                        "A")));

                rule.setConditionKit(
                        new Kit<>(Collections.singletonList(condition), TwinClassFieldConditionEntity::getId));

                TwinClassFieldRuleExecutionService.FieldRuleInput input = TwinClassFieldRuleExecutionService.FieldRuleInput
                        .builder()
                        .field(field)
                        .currentValue("A")
                        .rules(Collections.singletonList(rule))
                        .build();

                List<TwinClassFieldRuleExecutionService.FieldRuleOutput> outputs = service
                        .applyRules(Collections.singletonList(input));

                Assertions.assertEquals(1, outputs.size());
                Assertions.assertTrue(outputs.get(0).getRequired());

                // Test non-match
                input.setCurrentValue("B");
                outputs = service.applyRules(Collections.singletonList(input));
                Assertions.assertFalse(outputs.get(0).getRequired());
        }

        @Test
        void testLogicAnd() {
                UUID fieldId = UUID.randomUUID();
                TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);

                // Rule: if field contains "A" AND field != "AB", make required=true
                // Structure:
                // Root (AND)
                // - Leaf (contains A)
                // - Leaf (neq AB)

                TwinClassFieldRuleEntity rule = new TwinClassFieldRuleEntity()
                        .setId(UUID.randomUUID())
                        .setOverwrittenRequired(true);

                UUID rootId = UUID.randomUUID();
                TwinClassFieldConditionEntity root = new TwinClassFieldConditionEntity()
                        .setId(rootId)
                        .setLogicOperatorId(LogicOperator.AND);

                TwinClassFieldConditionEntity c1 = new TwinClassFieldConditionEntity()
                        .setId(UUID.randomUUID())
                        .setParentTwinClassFieldConditionId(rootId)
                        .setLogicOperatorId(LogicOperator.LEAF)
                        .setBaseTwinClassFieldId(fieldId)
                        .setConditionEvaluatorParams(
                                new HashMap<>(Map.of("conditionOperator", "contains",
                                        "valueToCompareWith", "A")));

                TwinClassFieldConditionEntity c2 = new TwinClassFieldConditionEntity()
                        .setId(UUID.randomUUID())
                        .setParentTwinClassFieldConditionId(rootId)
                        .setLogicOperatorId(LogicOperator.LEAF)
                        .setBaseTwinClassFieldId(fieldId)
                        .setConditionEvaluatorParams(
                                new HashMap<>(Map.of("conditionOperator", "neq", "valueToCompareWith",
                                        "AB")));

                rule.setConditionKit(new Kit<>(Arrays.asList(root, c1, c2), TwinClassFieldConditionEntity::getId));

                TwinClassFieldRuleExecutionService.FieldRuleInput input = TwinClassFieldRuleExecutionService.FieldRuleInput
                        .builder()
                        .field(field)
                        .currentValue("AC")
                        .rules(Collections.singletonList(rule))
                        .build();

                // AC contains A and != AB -> True
                List<TwinClassFieldRuleExecutionService.FieldRuleOutput> outputs = service
                        .applyRules(Collections.singletonList(input));
                Assertions.assertTrue(outputs.get(0).getRequired());

                // AB contains A but == AB -> False (because neq AB is false)
                input.setCurrentValue("AB");
                outputs = service.applyRules(Collections.singletonList(input));
                Assertions.assertFalse(outputs.get(0).getRequired());
        }

        @Test
        void testPriority() {
                UUID fieldId = UUID.randomUUID();
                TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);

                // Rule 1: priority 1, value="One"
                TwinClassFieldRuleEntity rule1 = new TwinClassFieldRuleEntity()
                        .setId(UUID.randomUUID())
                        .setRulePriority(1)
                        .setOverwrittenValue("One")
                        .setConditionKit(createAlwaysTrueCondition(fieldId));

                // Rule 2: priority 2, value="Two"
                TwinClassFieldRuleEntity rule2 = new TwinClassFieldRuleEntity()
                        .setId(UUID.randomUUID())
                        .setRulePriority(2)
                        .setOverwrittenValue("Two")
                        .setConditionKit(createAlwaysTrueCondition(fieldId));

                TwinClassFieldRuleExecutionService.FieldRuleInput input = TwinClassFieldRuleExecutionService.FieldRuleInput
                        .builder()
                        .field(field)
                        .currentValue("any")
                        .rules(Arrays.asList(rule2, rule1)) // Unsorted input
                        .build();

                List<TwinClassFieldRuleExecutionService.FieldRuleOutput> outputs = service
                        .applyRules(Collections.singletonList(input));

                // Expected: rule1 applied first, then rule2. Result "Two".
                Assertions.assertEquals("Two", outputs.get(0).getValue());
        }

        @Test
        void testNumericComparison() {
                UUID fieldId = UUID.randomUUID();
                TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);

                // Rule: if field > 10, make required=true
                TwinClassFieldRuleEntity rule = new TwinClassFieldRuleEntity()
                        .setId(UUID.randomUUID())
                        .setOverwrittenRequired(true);

                TwinClassFieldConditionEntity condition = new TwinClassFieldConditionEntity()
                        .setId(UUID.randomUUID())
                        .setLogicOperatorId(LogicOperator.LEAF)
                        .setBaseTwinClassFieldId(fieldId)
                        .setConditionEvaluatorParams(new HashMap<>(
                                Map.of("conditionOperator", "gt", "valueToCompareWith", " 10 ")));

                rule.setConditionKit(
                        new Kit<>(Collections.singletonList(condition), TwinClassFieldConditionEntity::getId));

                TwinClassFieldRuleExecutionService.FieldRuleInput input = TwinClassFieldRuleExecutionService.FieldRuleInput
                        .builder()
                        .field(field)
                        .currentValue("15")
                        .rules(Collections.singletonList(rule))
                        .build();

                // 15 > 10 -> True
                List<TwinClassFieldRuleExecutionService.FieldRuleOutput> outputs = service
                        .applyRules(Collections.singletonList(input));
                Assertions.assertTrue(outputs.get(0).getRequired());

                // 5 > 10 -> False
                input.setCurrentValue("5");
                outputs = service.applyRules(Collections.singletonList(input));
                Assertions.assertFalse(outputs.get(0).getRequired());

                // Edge case: precision
                // 10.0001 > 10 -> True
                input.setCurrentValue("10.0001");
                outputs = service.applyRules(Collections.singletonList(input));
                Assertions.assertTrue(outputs.get(0).getRequired());
        }

    @Test
    void testApplyRulesMapReturnsValuesWhenNoRules() {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(UUID.randomUUID());
        Map<TwinClassFieldEntity, Object> values = new LinkedHashMap<>();
        values.put(field, "value");

        Map<TwinClassFieldEntity, Object> result = service.applyRules(Collections.emptyMap(), values);

        Assertions.assertSame(values, result);
    }

    @Test
    void testInOperatorWithMultiValueActual() {
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);

        TwinClassFieldRuleEntity rule = new TwinClassFieldRuleEntity()
                .setId(UUID.randomUUID())
                .setOverwrittenRequired(true);

        TwinClassFieldConditionEntity condition = new TwinClassFieldConditionEntity()
                .setId(UUID.randomUUID())
                .setLogicOperatorId(LogicOperator.LEAF)
                .setBaseTwinClassFieldId(fieldId)
                .setConditionEvaluatorParams(new HashMap<>(Map.of("conditionOperator", "in",
                        "valueToCompareWith", "A;C")));

        rule.setConditionKit(
                new Kit<>(Collections.singletonList(condition), TwinClassFieldConditionEntity::getId));

        TwinClassFieldRuleExecutionService.FieldRuleInput input = TwinClassFieldRuleExecutionService.FieldRuleInput
                .builder()
                .field(field)
                .currentValue("B,A")
                .rules(Collections.singletonList(rule))
                .build();

        List<TwinClassFieldRuleExecutionService.FieldRuleOutput> outputs = service
                .applyRules(Collections.singletonList(input));
        Assertions.assertTrue(outputs.get(0).getRequired());

        input.setCurrentValue("D");
        outputs = service.applyRules(Collections.singletonList(input));
        Assertions.assertFalse(outputs.get(0).getRequired());
    }

    @Test
    void testRuleWithoutConditionsAllowsNullOverwrittenValue() {
        UUID fieldId = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);

        TwinClassFieldRuleEntity rule = new TwinClassFieldRuleEntity()
                .setId(UUID.randomUUID())
                .setOverwrittenValue(null);

        TwinClassFieldRuleExecutionService.FieldRuleInput input = TwinClassFieldRuleExecutionService.FieldRuleInput
                .builder()
                .field(field)
                .currentValue("X")
                .rules(Collections.singletonList(rule))
                .build();

        List<TwinClassFieldRuleExecutionService.FieldRuleOutput> outputs = service
                .applyRules(Collections.singletonList(input));
        Assertions.assertNull(outputs.get(0).getValue());
    }

    @Test
    void testContextUpdatesWithinAndAcrossFields() {
        UUID fieldAId = UUID.randomUUID();
        UUID fieldBId = UUID.randomUUID();
        TwinClassFieldEntity fieldA = new TwinClassFieldEntity().setId(fieldAId);
        TwinClassFieldEntity fieldB = new TwinClassFieldEntity().setId(fieldBId);

        TwinClassFieldRuleEntity ruleA1 = new TwinClassFieldRuleEntity()
                .setId(UUID.randomUUID())
                .setRulePriority(1)
                .setOverwrittenValue("B");

        TwinClassFieldRuleEntity ruleA2 = new TwinClassFieldRuleEntity()
                .setId(UUID.randomUUID())
                .setRulePriority(2)
                .setOverwrittenRequired(true)
                .setConditionKit(createEqCondition(fieldAId, "B"));

        TwinClassFieldRuleEntity ruleB1 = new TwinClassFieldRuleEntity()
                .setId(UUID.randomUUID())
                .setOverwrittenRequired(true)
                .setConditionKit(createEqCondition(fieldAId, "B"));

        TwinClassFieldRuleExecutionService.FieldRuleInput inputA = TwinClassFieldRuleExecutionService.FieldRuleInput
                .builder()
                .field(fieldA)
                .currentValue("A")
                .rules(Arrays.asList(ruleA2, ruleA1))
                .build();

        TwinClassFieldRuleExecutionService.FieldRuleInput inputB = TwinClassFieldRuleExecutionService.FieldRuleInput
                .builder()
                .field(fieldB)
                .currentValue("X")
                .rules(Collections.singletonList(ruleB1))
                .build();

        List<TwinClassFieldRuleExecutionService.FieldRuleOutput> outputs = service
                .applyRules(Arrays.asList(inputA, inputB));

        Assertions.assertEquals("B", outputs.get(0).getValue());
        Assertions.assertTrue(outputs.get(0).getRequired());
        Assertions.assertTrue(outputs.get(1).getRequired());
    }

        @Test
        void testApplyRulesWithFieldUpdates() {
                UUID fieldId = UUID.randomUUID();
                TwinClassFieldEntity field = new TwinClassFieldEntity()
                        .setId(fieldId)
                        .setRequired(false)
                        .setFieldTyperParams(new HashMap<>(Map.of("existingParam", "existingValue")));

                // Rule: Always true, overwrite required=true, overwrite param
                // "newParam"="newValue"
                TwinClassFieldRuleEntity rule = new TwinClassFieldRuleEntity()
                        .setId(UUID.randomUUID())
                        .setOverwrittenRequired(true)
                        .setFieldOverwriterParams(new HashMap<>(
                                Map.of("newParam", "newValue", "existingParam", "overwrittenValue")))
                        .setConditionKit(createAlwaysTrueCondition(fieldId));

                Map<TwinClassFieldEntity, List<TwinClassFieldRuleEntity>> rulesByField = new HashMap<>();
                rulesByField.put(field, Collections.singletonList(rule));

                Map<TwinClassFieldEntity, Object> values = new HashMap<>();
                values.put(field, "someValue");

                Map<TwinClassFieldEntity, Object> result = service.applyRules(rulesByField, values);

                Assertions.assertEquals(1, result.size());
                TwinClassFieldEntity resultField = result.keySet().iterator().next();

                // Verify required is updated
                Assertions.assertTrue(resultField.getRequired());

                // Verify fieldTyperParams are updated/merged
                Assertions.assertEquals("newValue", resultField.getFieldTyperParams().get("newParam"));
                Assertions.assertEquals("overwrittenValue", resultField.getFieldTyperParams().get("existingParam"));
            Assertions.assertNull(result.get(resultField));
        }

        private Kit<TwinClassFieldConditionEntity, UUID> createAlwaysTrueCondition(UUID fieldId) {
                TwinClassFieldConditionEntity condition = new TwinClassFieldConditionEntity()
                        .setId(UUID.randomUUID())
                        .setLogicOperatorId(LogicOperator.LEAF)
                        .setBaseTwinClassFieldId(fieldId)
                        .setConditionEvaluatorParams(
                                new HashMap<>(Map.of("conditionOperator", "neq", "valueToCompareWith",
                                        "IMPOSSIBLE_VALUE")));
                return new Kit<>(Collections.singletonList(condition), TwinClassFieldConditionEntity::getId);
        }

    private Kit<TwinClassFieldConditionEntity, UUID> createEqCondition(UUID fieldId, String expected) {
        TwinClassFieldConditionEntity condition = new TwinClassFieldConditionEntity()
                .setId(UUID.randomUUID())
                .setLogicOperatorId(LogicOperator.LEAF)
                .setBaseTwinClassFieldId(fieldId)
                .setConditionEvaluatorParams(new HashMap<>(Map.of("conditionOperator", "eq",
                        "valueToCompareWith", expected)));
        return new Kit<>(Collections.singletonList(condition), TwinClassFieldConditionEntity::getId);
    }
}
