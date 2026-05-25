package org.twins.core.service.twin;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.domain.field.rule.FieldRuleOutput;
import org.twins.core.domain.field.rule.FieldRulesApplyResult;
import org.twins.core.enums.twinclass.LogicOperator;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValueColorHEX;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twinclass.TwinClassFieldRuleMapService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TwinFieldRuleExecutionServiceComprehensiveTest {

    @Mock
    private TwinClassFieldRuleMapService twinClassFieldRuleMapService;

    @InjectMocks
    private TwinFieldRuleExecutionService service;

    // ===================== Helpers =====================

    private TwinClassFieldEntity field(UUID id) {
        return field(id, false);
    }

    private TwinClassFieldEntity field(UUID id, boolean required) {
        TwinClassFieldEntity f = new TwinClassFieldEntity();
        f.setId(id);
        f.setRequired(required);
        return f;
    }

    private TwinClassFieldRuleEntity rule(UUID id, int priority, boolean overwrittenRequired) {
        return new TwinClassFieldRuleEntity()
                .setId(id)
                .setRulePriority(priority)
                .setOverwrittenRequired(overwrittenRequired);
    }

    private TwinClassFieldConditionEntity condition(UUID id, UUID baseFieldId, UUID parentId, LogicOperator logic, String operator, String valueToCompare) {
        TwinClassFieldConditionEntity c = new TwinClassFieldConditionEntity();
        c.setId(id);
        c.setBaseTwinClassFieldId(baseFieldId);
        c.setParentTwinClassFieldConditionId(parentId);
        c.setLogicOperatorId(logic);
        HashMap<String, String> params = new HashMap<>();
        if (operator != null) params.put("conditionOperator", operator);
        if (valueToCompare != null) params.put("valueToCompareWith", valueToCompare);
        c.setConditionEvaluatorParams(params);
        return c;
    }

    private void mockLoadRules() {
        try {
            lenient().doNothing().when(twinClassFieldRuleMapService).loadRules(anyList(), eq(true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FieldRulesApplyResult applyRules(List<FieldValue> values, TwinEntity twin) throws Exception {
        mockLoadRules();
        return service.applyRules(values, twin);
    }

    private void attachRules(TwinClassFieldEntity field, TwinClassFieldRuleEntity... rules) {
        field.setRuleKit(new Kit<>(new ArrayList<>(Arrays.asList(rules)), TwinClassFieldRuleEntity::getId));
    }

    private void attachConditions(TwinClassFieldRuleEntity rule, TwinClassFieldConditionEntity... conditions) {
        rule.setConditionKit(new Kit<>(new ArrayList<>(Arrays.asList(conditions)), TwinClassFieldConditionEntity::getId));
    }

    @Test
    @DisplayName("DIAG: проверка инициализации сервиса")
    void diagnostic_serviceNotNull() throws Exception {
        assertNotNull(service, "service должен быть инициализирован через @InjectMocks");
        assertNotNull(twinClassFieldRuleMapService, "mock должен быть инициализирован");

        var f = field(UUID.randomUUID());
        mockLoadRules();
        var result = service.applyRules(Collections.emptyList(), new TwinEntity());
        assertNotNull(result, "applyRules не должен возвращать null для пустой коллекции");
    }

    @Test
    @DisplayName("DIAG: простое правило без условий")
    void diagnostic_simpleRuleNoConditions() throws Exception {
        var f = field(UUID.randomUUID(), false);
        var r = rule(UUID.randomUUID(), 1, true);
        attachConditions(r); // нет условий → checkRule=true
        attachRules(f, r);

        mockLoadRules();
        var twin = new TwinEntity();
        var result = service.applyRules(List.of(new FieldValueText(f).setValue("x")), twin);

        assertNotNull(result, "result не должен быть null");
        assertNotNull(result.get(f.getId()), "result должен содержать поле " + f.getId());
        assertTrue(result.get(f.getId()).getRequired());
    }

    @Test
    @DisplayName("DIAG: правило с eq-условием — детальный разбор")
    void diagnostic_eqWithConditionDetailed() throws Exception {
        var f1 = field(UUID.randomUUID());
        var f2 = field(UUID.randomUUID(), false);
        var condId = UUID.randomUUID();
        var r = rule(UUID.randomUUID(), 1, true);
        attachConditions(r, condition(condId, f1.getId(), null, LogicOperator.LEAF, "eq", "hello"));
        attachRules(f2, r);

        var v1 = new FieldValueText(f1).setValue("hello");
        var v2 = new FieldValueText(f2).setValue("x");
        var twin = new TwinEntity();

        mockLoadRules();

        try {
            var result = service.applyRules(List.of(v1, v2), twin);
            assertNotNull(result);
            var out2 = result.get(f2.getId());
            if (out2 == null) {
                // Выведем все ключи в result
                var keys = result.getIdSet();
                fail("result.get(f2) is null! keys in result: " + keys + ", f2.id=" + f2.getId());
            }
            assertTrue(out2.getRequired(), "rule should set required=true for f2");
        } catch (NullPointerException e) {
            e.printStackTrace();
            fail("NPE: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 1. evaluateCondition: eq
    // ============================================================

    @Nested
    @DisplayName("evaluateCondition — eq")
    class EqTest {

        @Test
        @DisplayName("eq: точное совпадение — правило применяется")
        void match() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "eq", "hello"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("hello"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("eq: несовпадение — правило НЕ применяется, hasError")
        void noMatch() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "eq", "hello"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("world"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertFalse(result.get(f2.getId()).getRequired());
            assertTrue(result.get(f2.getId()).getHasError());
        }

        @Test
        @DisplayName("eq: case-insensitive — правило применяется")
        void caseInsensitive() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "eq", "Hello"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("HELLO"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("eq: actual=null, expected='null' — правило применяется")
        void nullCheck() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "eq", "null"));
            attachRules(f2, r);

            // f1 без значения = UNDEFINED, normalizeValue → null
            var result = applyRules(List.of(
                    new FieldValueText(f1),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("eq: actual=value, expected='null' — правило НЕ применяется")
        void nullCheckWithValue() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "eq", "null"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("something"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertFalse(result.get(f2.getId()).getRequired());
        }
    }

    // ============================================================
    // 2. evaluateCondition: neq
    // ============================================================

    @Nested
    @DisplayName("evaluateCondition — neq")
    class NeqTest {

        @Test
        @DisplayName("neq: разные значения — правило применяется")
        void different() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "neq", "hello"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("world"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("neq: одинаковые значения — правило НЕ применяется")
        void same() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "neq", "hello"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("hello"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertFalse(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("neq: actual=value, expected='null' — правило применяется")
        void nullCheckWithValue() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "neq", "null"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("something"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }
    }

    // ============================================================
    // 3. evaluateCondition: lt, gt
    // ============================================================

    @Nested
    @DisplayName("evaluateCondition — lt/gt")
    class LtGtTest {

        @Test
        @DisplayName("lt: 5 < 10 — правило применяется")
        void lt_true() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "lt", "10"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("5"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("lt: 15 < 10 — правило НЕ применяется")
        void lt_false() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "lt", "10"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("15"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertFalse(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("lt: 10 < 10 — правило НЕ применяется (равно)")
        void lt_equal() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "lt", "10"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("10"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertFalse(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("gt: 15 > 10 — правило применяется")
        void gt_true() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "gt", "10"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("15"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("gt: decimal 10.5 > 10.0 — правило применяется")
        void gt_decimal() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "gt", "10.0"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("10.5"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("lt: нечисловое значение — правило НЕ применяется")
        void lt_nonNumeric() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "lt", "10"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("abc"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertFalse(result.get(f2.getId()).getRequired());
        }
    }

    // ============================================================
    // 4. evaluateCondition: contains, in
    // ============================================================

    @Nested
    @DisplayName("evaluateCondition — contains/in")
    class ContainsInTest {

        @Test
        @DisplayName("contains: подстрока найдена — правило применяется")
        void contains_match() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "contains", "llo"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("hello world"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("contains: подстрока не найдена — правило НЕ применяется")
        void contains_noMatch() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "contains", "xyz"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("hello world"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertFalse(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("contains: case-insensitive — правило применяется")
        void contains_caseInsensitive() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "contains", "WORLD"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("hello world"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("in: значение в списке через запятую — правило применяется")
        void in_match() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "in", "option1,option2,option3"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("option2"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("in: значение НЕ в списке — правило НЕ применяется")
        void in_noMatch() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "in", "option1,option2"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("option99"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertFalse(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("in: значения через точку с запятой — правило применяется")
        void in_semicolon() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "in", "a;b;c"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("b"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("in: case-insensitive — правило применяется")
        void in_caseInsensitive() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "in", "Option1,Option2"));
            attachRules(f2, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("option1"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }
    }

    // ============================================================
    // 5. Condition tree: AND/OR/LEAF
    // ============================================================

    @Nested
    @DisplayName("Condition tree — AND/OR/LEAF")
    class ConditionTreeTest {

        @Test
        @DisplayName("AND: оба условия выполнены — правило применяется")
        void and_bothTrue() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID());
            var f3 = field(UUID.randomUUID(), false);
            UUID andId = UUID.randomUUID();
            var andCond = condition(andId, null, null, LogicOperator.AND, null, null);
            var leaf1 = condition(UUID.randomUUID(), f1.getId(), andId, LogicOperator.LEAF, "eq", "hello");
            var leaf2 = condition(UUID.randomUUID(), f2.getId(), andId, LogicOperator.LEAF, "eq", "world");

            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, andCond, leaf1, leaf2);
            attachRules(f3, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("hello"),
                    new FieldValueText(f2).setValue("world"),
                    new FieldValueText(f3).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f3.getId()).getRequired());
        }

        @Test
        @DisplayName("AND: одно условие НЕ выполнено — правило НЕ применяется")
        void and_oneFalse() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID());
            var f3 = field(UUID.randomUUID(), false);
            UUID andId = UUID.randomUUID();
            var andCond = condition(andId, null, null, LogicOperator.AND, null, null);
            var leaf1 = condition(UUID.randomUUID(), f1.getId(), andId, LogicOperator.LEAF, "eq", "hello");
            var leaf2 = condition(UUID.randomUUID(), f2.getId(), andId, LogicOperator.LEAF, "eq", "world");

            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, andCond, leaf1, leaf2);
            attachRules(f3, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("hello"),
                    new FieldValueText(f2).setValue("WRONG"),
                    new FieldValueText(f3).setValue("x")
            ), new TwinEntity());

            assertFalse(result.get(f3.getId()).getRequired());
        }

        @Test
        @DisplayName("OR: хотя бы одно условие выполнено — правило применяется")
        void or_oneTrue() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID());
            var f3 = field(UUID.randomUUID(), false);
            UUID orId = UUID.randomUUID();
            var orCond = condition(orId, null, null, LogicOperator.OR, null, null);
            var leaf1 = condition(UUID.randomUUID(), f1.getId(), orId, LogicOperator.LEAF, "eq", "hello");
            var leaf2 = condition(UUID.randomUUID(), f2.getId(), orId, LogicOperator.LEAF, "eq", "world");

            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, orCond, leaf1, leaf2);
            attachRules(f3, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("WRONG"),
                    new FieldValueText(f2).setValue("world"),
                    new FieldValueText(f3).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f3.getId()).getRequired());
        }

        @Test
        @DisplayName("OR: ни одно условие НЕ выполнено — правило НЕ применяется")
        void or_allFalse() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID());
            var f3 = field(UUID.randomUUID(), false);
            UUID orId = UUID.randomUUID();
            var orCond = condition(orId, null, null, LogicOperator.OR, null, null);
            var leaf1 = condition(UUID.randomUUID(), f1.getId(), orId, LogicOperator.LEAF, "eq", "hello");
            var leaf2 = condition(UUID.randomUUID(), f2.getId(), orId, LogicOperator.LEAF, "eq", "world");

            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, orCond, leaf1, leaf2);
            attachRules(f3, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("WRONG"),
                    new FieldValueText(f2).setValue("WRONG"),
                    new FieldValueText(f3).setValue("x")
            ), new TwinEntity());

            assertFalse(result.get(f3.getId()).getRequired());
        }

        @Test
        @DisplayName("Вложенное дерево: OR → [AND → [LEAF, LEAF], LEAF] — сложное условие")
        void nestedTree() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID());
            var f3 = field(UUID.randomUUID());
            var f4 = field(UUID.randomUUID(), false);
            UUID orId = UUID.randomUUID();
            UUID andId = UUID.randomUUID();

            var orCond = condition(orId, null, null, LogicOperator.OR, null, null);
            var andCond = condition(andId, null, orId, LogicOperator.AND, null, null);
            var leaf1 = condition(UUID.randomUUID(), f1.getId(), andId, LogicOperator.LEAF, "eq", "a");
            var leaf2 = condition(UUID.randomUUID(), f2.getId(), andId, LogicOperator.LEAF, "eq", "b");
            var leaf3 = condition(UUID.randomUUID(), f3.getId(), orId, LogicOperator.LEAF, "eq", "c");

            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, orCond, andCond, leaf1, leaf2, leaf3);
            attachRules(f4, r);

            // AND branch: f1="a" AND f2="b" → true; OR → true → правило применяется
            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("a"),
                    new FieldValueText(f2).setValue("b"),
                    new FieldValueText(f3).setValue("WRONG"),
                    new FieldValueText(f4).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f4.getId()).getRequired());
        }

        @Test
        @DisplayName("Без логики — все условия в одном AND (legacy режим)")
        void legacyAndGroup() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID());
            var f3 = field(UUID.randomUUID(), false);

            var cond1 = condition(UUID.randomUUID(), f1.getId(), null, null, "eq", "hello");
            var cond2 = condition(UUID.randomUUID(), f2.getId(), null, null, "eq", "world");

            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, cond1, cond2);
            attachRules(f3, r);

            var result = applyRules(List.of(
                    new FieldValueText(f1).setValue("hello"),
                    new FieldValueText(f2).setValue("world"),
                    new FieldValueText(f3).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f3.getId()).getRequired());
        }
    }

    // ============================================================
    // 6. isRequired — public
    // ============================================================

    @Nested
    @DisplayName("isRequired")
    class IsRequiredTest {

        @Test
        @DisplayName("rulesApplyResult == null → fallback на field.required=true")
        void nullResult_requiredTrue() {
            assertTrue(service.isRequired(new TwinEntity(), field(UUID.randomUUID(), true)));
        }

        @Test
        @DisplayName("rulesApplyResult == null → fallback на field.required=false")
        void nullResult_requiredFalse() {
            assertFalse(service.isRequired(new TwinEntity(), field(UUID.randomUUID(), false)));
        }

        @Test
        @DisplayName("rulesApplyResult содержит поле с required=true")
        void resultPresent_requiredTrue() {
            var twin = new TwinEntity();
            var f = field(UUID.randomUUID(), false);
            var applyResult = new FieldRulesApplyResult();
            applyResult.add(FieldRuleOutput.builder().field(f).value(null).required(true).descriptor(new HashMap<>()).build());
            twin.setFieldRulesApplyResult(applyResult);
            assertTrue(service.isRequired(twin, f));
        }

        @Test
        @DisplayName("rulesApplyResult не содержит поле → fallback на field.required")
        void resultMissing_fallback() {
            var twin = new TwinEntity();
            twin.setFieldRulesApplyResult(new FieldRulesApplyResult());
            assertTrue(service.isRequired(twin, field(UUID.randomUUID(), true)));
        }
    }

    // ============================================================
    // 7. checkAllRequired — public
    // ============================================================

    @Nested
    @DisplayName("checkAllRequired")
    class CheckAllRequiredTest {

        private TwinEntity twin;
        private TwinClassEntity twinClass;

        @BeforeEach
        void setUp() {
            twin = new TwinEntity();
            twin.setId(UUID.randomUUID());
            twinClass = new TwinClassEntity();
            twin.setTwinClass(twinClass);
        }

        @Test
        @DisplayName("все required поля заполнены — true")
        void allFilled() throws Exception {
            var req = field(UUID.randomUUID(), true);
            var opt = field(UUID.randomUUID(), false);
            twinClass.setTwinClassFieldKit(new Kit<>(List.of(req, opt), TwinClassFieldEntity::getId));
            mockLoadRules();

            var values = new HashMap<UUID, FieldValue>();
            values.put(req.getId(), new FieldValueText(req).setValue("data"));

            assertTrue(service.checkAllRequired(values, twin));
        }

        @Test
        @DisplayName("required поле отсутствует — false")
        void missing() throws Exception {
            var req = field(UUID.randomUUID(), true);
            twinClass.setTwinClassFieldKit(new Kit<>(List.of(req), TwinClassFieldEntity::getId));
            mockLoadRules();

            assertFalse(service.checkAllRequired(new HashMap<>(), twin));
        }

        @Test
        @DisplayName("required поле присутствует, но пустое (UNDEFINED) — false")
        void presentButEmpty() throws Exception {
            var req = field(UUID.randomUUID(), true);
            twinClass.setTwinClassFieldKit(new Kit<>(List.of(req), TwinClassFieldEntity::getId));
            mockLoadRules();

            var values = new HashMap<UUID, FieldValue>();
            values.put(req.getId(), new FieldValueText(req)); // UNDEFINED

            assertFalse(service.checkAllRequired(values, twin));
        }

        @Test
        @DisplayName("только optional поля — true")
        void onlyOptional() throws Exception {
            var opt = field(UUID.randomUUID(), false);
            twinClass.setTwinClassFieldKit(new Kit<>(List.of(opt), TwinClassFieldEntity::getId));
            mockLoadRules();

            assertTrue(service.checkAllRequired(new HashMap<>(), twin));
        }
    }

    // ============================================================
    // 8. applyRules — публичный API
    // ============================================================

    @Nested
    @DisplayName("applyRules — публичный API")
    class ApplyRulesPublicTest {

        @Test
        @DisplayName("пустая коллекция → EMPTY результат")
        void empty() throws Exception {
            mockLoadRules();
            var result = service.applyRules(Collections.emptyList(), new TwinEntity());
            assertEquals(FieldRulesApplyResult.EMPTY, result);
        }

        @Test
        @DisplayName("повторный вызов — кеширование результата")
        void cached() throws Exception {
            var f = field(UUID.randomUUID());
            mockLoadRules();
            var twin = new TwinEntity();

            var first = service.applyRules(List.of(new FieldValueText(f).setValue("x")), twin);
            var second = service.applyRules(List.of(new FieldValueText(f).setValue("x")), twin);
            assertSame(first, second);
        }

        @Test
        @DisplayName("правила сортируются по приоритету (позже = перезаписывает)")
        void prioritySort() throws Exception {
            var f = field(UUID.randomUUID());
            var r1 = rule(UUID.randomUUID(), 10, true);  // позже перезапишет
            var r2 = rule(UUID.randomUUID(), 1, false);   // первый
            attachConditions(r1); // нет условий → true
            attachConditions(r2);
            attachRules(f, r1, r2);

            mockLoadRules();
            var result = service.applyRules(List.of(new FieldValueText(f).setValue("x")), new TwinEntity());

            assertTrue(result.get(f.getId()).getRequired());
        }

        @Test
        @DisplayName("batch applyRules для списка twinEntity")
        void batch() throws Exception {
            var f = field(UUID.randomUUID());
            attachRules(f);
            mockLoadRules();

            var t1 = new TwinEntity();
            t1.setTwinClass(new TwinClassEntity());
            t1.setFieldValuesKit(new Kit<>(List.of(new FieldValueText(f).setValue("v1")), FieldValue::getTwinClassFieldId));

            var t2 = new TwinEntity();
            t2.setTwinClass(new TwinClassEntity());
            t2.setFieldValuesKit(new Kit<>(List.of(new FieldValueText(f).setValue("v2")), FieldValue::getTwinClassFieldId));

            service.applyRules(List.of(t1, t2));
            assertNotNull(t1.getFieldRulesApplyResult());
            assertNotNull(t2.getFieldRulesApplyResult());
        }

        @Test
        @DisplayName("правило с overwrittenValue — значение перезаписывается")
        void overwrittenValue() throws Exception {
            var f = field(UUID.randomUUID());
            var r = new TwinClassFieldRuleEntity()
                    .setId(UUID.randomUUID())
                    .setRulePriority(1)
                    .setOverwrittenRequired(true)
                    .setOverwrittenValue("new_value");
            attachConditions(r); // нет условий → true
            attachRules(f, r);
            mockLoadRules();

            var result = service.applyRules(List.of(new FieldValueText(f).setValue("old")), new TwinEntity());
            assertEquals("new_value", result.get(f.getId()).getValue());
        }

        @Test
        @DisplayName("поле без правил — возвращает дефолтный required")
        void noRules() throws Exception {
            var f = field(UUID.randomUUID(), true);
            attachRules(f); // пустые правила
            mockLoadRules();

            var result = service.applyRules(List.of(new FieldValueText(f).setValue("x")), new TwinEntity());
            assertTrue(result.get(f.getId()).getRequired());
        }

        @Test
        @DisplayName("normalizeValue: FieldValueBoolean → 'true'/'false' в контексте")
        void booleanNormalization() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            // Проверяем что boolean нормализуется в "true" и eq сравнение работает
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "eq", "true"));
            attachRules(f2, r);
            mockLoadRules();

            var result = service.applyRules(List.of(
                    new FieldValueBoolean(f1).setValue(true),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("normalizeValue: FieldValueColorHEX → строка цвета в контексте")
        void colorHexNormalization() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "eq", "#FF0000"));
            attachRules(f2, r);
            mockLoadRules();

            var result = service.applyRules(List.of(
                    new FieldValueColorHEX(f1).setValue("#FF0000"),
                    new FieldValueText(f2).setValue("x")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }

        @Test
        @DisplayName("правило зависит от другого поля — порядок через sortValuesByDependency")
        void dependencySorting() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);

            // f2 зависит от f1: условие проверяет f1
            var r = rule(UUID.randomUUID(), 1, true);
            attachConditions(r, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "eq", "trigger"));
            attachRules(f2, r);

            mockLoadRules();

            // Передаём f2 ПЕРВЫМ — сортировка должна поставить f1 первым
            var result = service.applyRules(List.of(
                    new FieldValueText(f2).setValue("x"),
                    new FieldValueText(f1).setValue("trigger")
            ), new TwinEntity());

            assertTrue(result.get(f2.getId()).getRequired());
        }
    }

    // ============================================================
    // 9. Циклические зависимости
    // ============================================================

    @Nested
    @DisplayName("Циклические зависимости в правилах")
    class CircularDependencyTest {

        @Test
        @DisplayName("два поля зависят друг от друга — не зависает, правила выполняются")
        void circularDeps_noHang() throws Exception {
            var f1 = field(UUID.randomUUID());
            var f2 = field(UUID.randomUUID(), false);

            // f2 зависит от f1
            var r1 = rule(UUID.randomUUID(), 1, true);
            attachConditions(r1, condition(UUID.randomUUID(), f1.getId(), null, LogicOperator.LEAF, "eq", "x"));
            attachRules(f2, r1);

            // f1 зависит от f2 — цикл!
            var r2 = rule(UUID.randomUUID(), 1, false);
            attachConditions(r2, condition(UUID.randomUUID(), f2.getId(), null, LogicOperator.LEAF, "eq", "y"));
            attachRules(f1, r2);

            mockLoadRules();

            // Не должно зависать
            var result = assertDoesNotThrow(() -> service.applyRules(List.of(
                    new FieldValueText(f1).setValue("x"),
                    new FieldValueText(f2).setValue("y")
            ), new TwinEntity()));

            assertNotNull(result);
            assertNotNull(result.get(f1.getId()));
            assertNotNull(result.get(f2.getId()));
        }
    }
}
