package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.enums.twinclass.LogicOperator;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;
import org.twins.core.featurer.fieldtyper.value.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldRuleExecutionService {

    public static final String DESCRIPTOR_RULE_FAILED = "rule_failed";

    @LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2
            * 1000, level = JavaLoggingLevel.WARNING)
    public List<FieldRuleOutput> applyRules(List<FieldRuleInput> inputs) {
        if (CollectionUtils.isEmpty(inputs))
            return Collections.emptyList();

        Map<UUID, String> contextValues = new HashMap<>(); // better pre-sizing?
        for (FieldRuleInput input : inputs) {
            contextValues.put(input.getField().getId(), normalizeValue(input.getCurrentValue()));
        }

        List<FieldRuleOutput> results = new ArrayList<>(inputs.size());

        for (FieldRuleInput input : inputs) {
            results.add(evaluateFieldRules(input, contextValues));
        }

        return results;
    }

    public Map<TwinClassFieldEntity, Object> applyRules(Collection<TwinClassFieldEntity> fields,
                                                        Map<TwinClassFieldEntity, Object> values) {
        Map<TwinClassFieldEntity, List<TwinClassFieldRuleEntity>> rulesByField = new HashMap<>();
        fields.forEach(it ->
                rulesByField.put(it, it.getRuleKit().getList()));
        return applyRules(rulesByField, values);
    }

    /**
     * Values are aligned to fields by key, and output contains only fields present
     * in {@code rulesByField}.
     * When {@code rulesByField} is empty, values are returned as-is.
     */
    public Map<TwinClassFieldEntity, Object> applyRules(
            Map<TwinClassFieldEntity, List<TwinClassFieldRuleEntity>> rulesByField,
            Map<TwinClassFieldEntity, Object> values) {
        if (rulesByField == null || rulesByField.isEmpty())
            return values != null ? values : Collections.emptyMap();

        List<FieldRuleInput> inputs = new ArrayList<>(rulesByField.size());
        for (Map.Entry<TwinClassFieldEntity, List<TwinClassFieldRuleEntity>> entry : rulesByField.entrySet()) {
            Object currentValue = values != null ? values.get(entry.getKey()) : null;
            inputs.add(FieldRuleInput.builder()
                    .field(entry.getKey())
                    .currentValue(currentValue)
                    .rules(entry.getValue())
                    .build());
        }

        List<FieldRuleOutput> outputs = applyRules(inputs);
        Map<TwinClassFieldEntity, Object> result = new LinkedHashMap<>(outputs.size());
        for (FieldRuleOutput output : outputs) {
            TwinClassFieldEntity field = output.getField();
            if (output.getRequired() != null)
                field.setRequired(output.getRequired());
            if (output.getDescriptor() != null && !output.getDescriptor().isEmpty()) {
                if (field.getFieldTyperParams() == null)
                    field.setFieldTyperParams(new HashMap<>());
                field.getFieldTyperParams().putAll(output.getDescriptor());
            }
            result.put(field, output.getValue());
        }
        return result;
    }

    private FieldRuleOutput evaluateFieldRules(FieldRuleInput input, Map<UUID, String> contextValues) {
        FieldRuleOutput output = FieldRuleOutput.builder()
                .field(input.getField())
                .value(input.getCurrentValue())
                .required(input.getField().getRequired() != null ? input.getField().getRequired() : false)
                .descriptor(new HashMap<>())
                .build();

        List<TwinClassFieldRuleEntity> rules = input.getRules();
        if (CollectionUtils.isEmpty(rules))
            return output;

        rules.sort(Comparator.comparingInt(r -> r.getRulePriority() != null ? r.getRulePriority() : 0));

        for (TwinClassFieldRuleEntity rule : rules) {
            if (checkRule(rule, contextValues)) {
                applyRuleEffect(rule, output);
                contextValues.put(output.getField().getId(), normalizeValue(output.getValue()));
            } else {
                output.descriptor.put(DESCRIPTOR_RULE_FAILED, String.valueOf(true));
            }
        }

        return output;
    }

    private void applyRuleEffect(TwinClassFieldRuleEntity rule, FieldRuleOutput output) {
        if (rule.getOverwrittenRequired() != null) {
            output.setRequired(rule.getOverwrittenRequired());
        }
        output.setValue(rule.getOverwrittenValue());
        if (rule.getFieldOverwriterParams() != null) {
            output.getDescriptor().putAll(rule.getFieldOverwriterParams());
        }
    }

    private boolean checkRule(TwinClassFieldRuleEntity rule, Map<UUID, String> contextValues) {
        // Use Kit if available, or fallback to list
        List<TwinClassFieldConditionEntity> conditions = rule.getConditionKit() != null
                ? rule.getConditionKit().getList()
                : Collections.emptyList();
        if (CollectionUtils.isEmpty(conditions))
            return true;

        List<ConditionNode> roots = buildConditionTree(conditions);
        return evaluateRuleTree(roots, contextValues);
    }

    private boolean evaluateRuleTree(List<ConditionNode> roots, Map<UUID, String> contextValues) {
        if (CollectionUtils.isEmpty(roots))
            return false;

        for (ConditionNode root : roots) {
            if (evaluateConditionNode(root, contextValues)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateConditionNode(ConditionNode node, Map<UUID, String> contextValues) {
        if (node.logic == LogicOperator.LEAF) {
            if (node.condition == null || node.condition.getBaseTwinClassFieldId() == null)
                return false;
            String actualValue = contextValues.get(node.condition.getBaseTwinClassFieldId());
            return evaluateCondition(actualValue, node.condition);
        }

        if (CollectionUtils.isEmpty(node.children))
            return false;

        if (node.logic == LogicOperator.AND) {
            for (ConditionNode child : node.children) {
                if (!evaluateConditionNode(child, contextValues))
                    return false;
            }
            return true;
        }

        if (node.logic == LogicOperator.OR) {
            for (ConditionNode child : node.children) {
                if (evaluateConditionNode(child, contextValues))
                    return true;
            }
            return false;
        }

        return false;
    }

    private boolean evaluateCondition(String actualValue, TwinClassFieldConditionEntity condition) {
        Map<String, String> params = condition.getConditionEvaluatorParams();
        if (params == null)
            params = Collections.emptyMap();

        String operatorStr = params.getOrDefault("conditionOperator", TwinClassFieldConditionOperator.eq.name());
        String expected = params.get("valueToCompareWith");
        if (expected == null)
            expected = "";
        else
            expected = expected.trim();

        TwinClassFieldConditionOperator operator;
        try {
            operator = TwinClassFieldConditionOperator.valueOf(operatorStr.trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown condition operator: {}", operatorStr);
            return false;
        }

        boolean isNullish = StringUtils.isEmpty(actualValue);

        switch (operator) {
            case eq:
                if ("null".equalsIgnoreCase(expected))
                    return isNullish;
                return StringUtils.equalsIgnoreCase(actualValue, expected);
            case neq:
                if ("null".equalsIgnoreCase(expected))
                    return !isNullish;
                if (actualValue == null)
                    return false;
                return !StringUtils.equalsIgnoreCase(actualValue, expected);
            case lt:
                Integer ltCompare = compareNumbers(actualValue, expected);
                return ltCompare != null && ltCompare < 0;
            case gt:
                Integer gtCompare = compareNumbers(actualValue, expected);
                return gtCompare != null && gtCompare > 0;
            case contains:
                if (actualValue == null)
                    return false;
                return StringUtils.containsIgnoreCase(actualValue, expected);
            case in:
                Set<String> expectedOptions = splitValues(expected);
                if (expectedOptions.isEmpty())
                    return false;
                if (isNullish)
                    return expectedOptions.contains("null");
                Set<String> actualOptions = splitValues(actualValue);
                for (String actualOption : actualOptions) {
                    if (expectedOptions.contains(actualOption))
                        return true;
                }
                return false;
            default:
                return false;
        }
    }

    protected Integer compareNumbers(String actual, String expected) {
        if (StringUtils.isBlank(actual) || StringUtils.isBlank(expected))
            return null;
        try {
            BigDecimal ac = new BigDecimal(actual.trim());
            BigDecimal ex = new BigDecimal(expected.trim());
            return ac.compareTo(ex);
        } catch (NumberFormatException e) {
            log.warn("Cannot compare numbers: {} vs {}", actual, expected, e);
            return null;
        }
    }

    protected Set<String> splitValues(String value) {
        if (StringUtils.isBlank(value))
            return Collections.emptySet();

        Set<String> result = new LinkedHashSet<>();
        String[] parts = value.split("[;,]");
        for (String part : parts) {
            String normalized = part.trim();
            if (StringUtils.isNotEmpty(normalized))
                result.add(normalized.toLowerCase(Locale.ROOT));
        }
        return result;
    }

    protected List<ConditionNode> buildConditionTree(List<TwinClassFieldConditionEntity> conditions) {
        boolean hasLogic = conditions.stream().anyMatch(c -> c.getLogicOperatorId() != null);

        if (hasLogic) {
            return buildNewConditionTree(conditions);
        } else {
            return buildGroupNoTree(conditions);
        }
    }

    protected List<ConditionNode> buildNewConditionTree(List<TwinClassFieldConditionEntity> conditions) {
        Map<UUID, ConditionNode> nodesById = new HashMap<>();
        for (TwinClassFieldConditionEntity c : conditions) {
            nodesById.put(c.getId(), new ConditionNode(c, inferLogic(c)));
        }

        List<ConditionNode> roots = new ArrayList<>();
        for (TwinClassFieldConditionEntity c : conditions) {
            ConditionNode node = nodesById.get(c.getId());
            if (c.getParentTwinClassFieldConditionId() != null
                    && nodesById.containsKey(c.getParentTwinClassFieldConditionId())) {
                nodesById.get(c.getParentTwinClassFieldConditionId()).children.add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    protected List<ConditionNode> buildGroupNoTree(List<TwinClassFieldConditionEntity> conditions) {
        // Since groupNo is missing in TwinClassFieldConditionEntity, we assume all
        // conditions are in one group (AND).
        List<ConditionNode> leaves = new ArrayList<>();
        for (TwinClassFieldConditionEntity c : conditions) {
            leaves.add(new ConditionNode(c, LogicOperator.LEAF));
        }

        ConditionNode root = new ConditionNode(null, LogicOperator.AND);
        root.children.addAll(leaves);
        return Collections.singletonList(root);
    }

    protected LogicOperator inferLogic(TwinClassFieldConditionEntity c) {
        if (c.getLogicOperatorId() != null)
            return c.getLogicOperatorId();
        if (c.getBaseTwinClassFieldId() != null)
            return LogicOperator.LEAF;
        return LogicOperator.AND;
    }

    protected String normalizeValue(Object value) {
        if (value == null)
            return null;
        if (value instanceof FieldValueText t)
            return normalizeValue(t.getValue());
        if (value instanceof FieldValueColorHEX t)
            return normalizeValue(t.getHex());
        if (value instanceof FieldValueBoolean b)
            return normalizeValue(b.getValue());
        if (value instanceof FieldValueDate d)
            return normalizeValue(d.getDateStr());
        if (value instanceof FieldValueSelect s)
            return normalizeValue(s.getOptions());
        if (value instanceof FieldValueUser u)
            return normalizeValue(u.getUsers());
        if (value instanceof FieldValueLink l)
            return normalizeValue(l.getTwinLinks());

        if (value instanceof DataListOptionEntity o)
            return o.getId() != null ? o.getId().toString() : null;
        if (value instanceof UserEntity u)
            return u.getId() != null ? u.getId().toString() : null;
        if (value instanceof TwinLinkEntity l)
            return l.getDstTwinId() != null ? l.getDstTwinId().toString() : null;

        if (value instanceof Collection<?> c) {
            return c.stream()
                    .map(this::normalizeValue)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.joining(","));
        }

        if (value instanceof Boolean b)
            return b ? "true" : "false";

        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            Object id = map.get("id");
            if (id != null)
                return normalizeValue(id);
        }

        return value.toString().trim();
    }

    @Data
    @Builder
    public static class FieldRuleInput {
        private TwinClassFieldEntity field;
        private Object currentValue;
        private List<TwinClassFieldRuleEntity> rules;
    }

    @Data
    @Builder
    public static class FieldRuleOutput {
        private TwinClassFieldEntity field;
        private Object value;
        private Boolean required;
        private Map<String, String> descriptor;
    }

    @RequiredArgsConstructor
    public static class ConditionNode {
        final TwinClassFieldConditionEntity condition;
        final LogicOperator logic;
        final List<ConditionNode> children = new ArrayList<>();
    }
}
