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
            }
        }

        return output;
    }

    private void applyRuleEffect(TwinClassFieldRuleEntity rule, FieldRuleOutput output) {
        if (rule.getOverwrittenRequired() != null) {
            output.setRequired(rule.getOverwrittenRequired());
        }
        if (StringUtils.isNotEmpty(rule.getOverwrittenValue())) {
            output.setValue(rule.getOverwrittenValue());
        }
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
            return false;

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
        String expected = params.getOrDefault("valueToCompareWith", "");

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
                return compareNumbers(actualValue, expected) < 0;
            case gt:
                return compareNumbers(actualValue, expected) > 0;
            case contains:
                if (actualValue == null)
                    return false;
                return StringUtils.containsIgnoreCase(actualValue, expected);
            case in:
                if (StringUtils.isEmpty(expected))
                    return false;
                String[] options = expected.split("[;,]");
                if (actualValue == null)
                    return false;
                for (String opt : options) {
                    if (StringUtils.equalsIgnoreCase(opt.trim(), actualValue))
                        return true;
                }
                return false;
            default:
                return false;
        }
    }

    private int compareNumbers(String actual, String expected) {
        try {
            BigDecimal ac = new BigDecimal(actual);
            BigDecimal ex = new BigDecimal(expected);
            return ac.compareTo(ex);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<ConditionNode> buildConditionTree(List<TwinClassFieldConditionEntity> conditions) {
        boolean hasLogic = conditions.stream().anyMatch(c -> c.getLogicOperatorId() != null);

        if (hasLogic) {
            return buildNewConditionTree(conditions);
        } else {
            return buildGroupNoTree(conditions);
        }
    }

    private List<ConditionNode> buildNewConditionTree(List<TwinClassFieldConditionEntity> conditions) {
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

    private List<ConditionNode> buildGroupNoTree(List<TwinClassFieldConditionEntity> conditions) {
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

    private LogicOperator inferLogic(TwinClassFieldConditionEntity c) {
        if (c.getLogicOperatorId() != null)
            return c.getLogicOperatorId();
        if (c.getBaseTwinClassFieldId() != null)
            return LogicOperator.LEAF;
        return LogicOperator.AND;
    }

    private String normalizeValue(Object value) {
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
    private static class ConditionNode {
        final TwinClassFieldConditionEntity condition;
        final LogicOperator logic;
        final List<ConditionNode> children = new ArrayList<>();
    }
}
