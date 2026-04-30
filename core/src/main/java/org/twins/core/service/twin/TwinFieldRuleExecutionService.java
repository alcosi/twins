package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.domain.field.rule.FieldRuleOutput;
import org.twins.core.domain.field.rule.FieldRulesApplyResult;
import org.twins.core.enums.twinclass.LogicOperator;
import org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluator;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twinclass.TwinClassFieldRuleMapService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinFieldRuleExecutionService {
    public static final String DESCRIPTOR_RULE_FAILED = "rule_failed";
    @Lazy
    private final TwinClassFieldRuleMapService twinClassFieldRuleMapService;
    @Lazy
    private final FeaturerService featurerService;

    @LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2000, level = JavaLoggingLevel.WARNING)
    private void collectDependencies(TwinClassFieldRuleEntity rule, Set<UUID> distinctDependencies) {
        if (rule.getConditionKit() == null || CollectionUtils.isEmpty(rule.getConditionKit().getList()))
            return;
        for (TwinClassFieldConditionEntity condition : rule.getConditionKit().getList()) {
            if (condition.getBaseTwinClassFieldId() != null) {
                distinctDependencies.add(condition.getBaseTwinClassFieldId());
            }
        }
    }

    private FieldRulesApplyResult applyRulesOnValues(Collection<FieldValue> values) throws ServiceException {
        if (CollectionUtils.isEmpty(values))
            return FieldRulesApplyResult.EMPTY;
        twinClassFieldRuleMapService.loadRules(values.stream().map(FieldValue::getTwinClassField).toList(), true);
        Map<UUID, FieldValue> contextValues = new HashMap<>();
        for (FieldValue value : values) {
            TwinClassFieldEntity field = value.getTwinClassField();
            if (field == null) continue;
            contextValues.put(field.getId(), value);
        }

        var results = new FieldRulesApplyResult();
        List<FieldValue> sortedValues = sortValuesByDependency(values);
        for (FieldValue value : sortedValues) {
            results.add(evaluateFieldRules(value, contextValues));
        }
        return results;
    }

    public FieldRulesApplyResult applyRules(Collection<FieldValue> values, TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getFieldRulesApplyResult() == null) {
            twinEntity.setFieldRulesApplyResult(applyRulesOnValues(values));
        }
        return twinEntity.getFieldRulesApplyResult();
    }

    public void applyRules(Collection<TwinEntity> twinEntities) throws ServiceException {
        for (var twinEntity : twinEntities) {
            if (twinEntity.getFieldRulesApplyResult() == null) {
                twinEntity.setFieldRulesApplyResult(applyRulesOnValues(twinEntity.getFieldValuesKit()));
            }
        }
    }

    public void applyRules(TwinEntity twinEntity) throws ServiceException {
        applyRules(Collections.singletonList(twinEntity));
    }

    @SneakyThrows
    public boolean checkAllRequired(Map<UUID, FieldValue> values, TwinEntity twinEntity) {
        applyRules(values.values(), twinEntity);
        if (twinEntity.getFieldRulesApplyResult() != null && twinEntity.getFieldRulesApplyResult().getAllRequiredFieldsFilled() != null) {
            return twinEntity.getFieldRulesApplyResult().getAllRequiredFieldsFilled();
        }
        for (var classField : twinEntity.getTwinClass().getTwinClassFieldKit()) {
            if (isRequired(twinEntity, classField)
                    && (!values.containsKey(classField.getId()) || values.get(classField.getId()).isEmpty())) {
                twinEntity.getFieldRulesApplyResult().setAllRequiredFieldsFilled(false);
                return false;
            }
        }
        return true;
    }

    public boolean checkAllRequired(Kit<FieldValue, UUID> values, TwinEntity twinEntity) {
        return checkAllRequired(values.getMap(), twinEntity);
    }

    public boolean isRequired(TwinEntity twin, TwinClassFieldEntity twinClassField) {
        if (twin.getFieldRulesApplyResult() == null) {
            log.warn("RulesApplyResult is not set for {} ", twin.logNormal());
            return twinClassField.getRequired();
        }
        var requiredDetectedByRule = twin.getFieldRulesApplyResult().get(twinClassField.getId());
        if (requiredDetectedByRule != null)
            return requiredDetectedByRule.getRequired();
        else
            return twinClassField.getRequired();
    }

    private List<FieldValue> sortValuesByDependency(Collection<FieldValue> values) {
        if (CollectionUtils.isEmpty(values) || values.size() < 2)
            return values instanceof List ? (List<FieldValue>) values : new ArrayList<>(values);

        Map<UUID, FieldValue> valueMap = values.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getTwinClassField() != null)
                .collect(Collectors.toMap(v -> v.getTwinClassField().getId(), v -> v, (a, b) -> a, LinkedHashMap::new));
        Set<UUID> ids = valueMap.keySet();

        Map<UUID, Set<UUID>> dependencies = new HashMap<>();
        Map<UUID, Integer> inDegree = new HashMap<>();
        for (UUID id : ids) {
            dependencies.putIfAbsent(id, new HashSet<>());
            inDegree.putIfAbsent(id, 0);
        }

        for (FieldValue value : values) {
            if (value == null || value.getTwinClassField() == null) continue;
            TwinClassFieldEntity field = value.getTwinClassField();
            Set<UUID> deps = new HashSet<>();
            Kit<TwinClassFieldRuleEntity, UUID> ruleKit = field.getRuleKit();
            if (ruleKit != null && CollectionUtils.isNotEmpty(ruleKit.getList())) {
                for (TwinClassFieldRuleEntity rule : ruleKit.getList()) {
                    collectDependencies(rule, deps);
                }
            }
            for (UUID depId : deps) {
                if (ids.contains(depId) && !depId.equals(field.getId())) {
                    dependencies.get(depId).add(field.getId());
                    inDegree.put(field.getId(), inDegree.getOrDefault(field.getId(), 0) + 1);
                }
            }
        }

        Queue<UUID> queue = new LinkedList<>();
        for (UUID id : ids) {
            if (inDegree.get(id) == 0) queue.add(id);
        }

        List<FieldValue> sorted = new ArrayList<>(values.size());
        Set<UUID> processed = new HashSet<>();

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            FieldValue v = valueMap.get(current);
            if (v != null) {
                sorted.add(v);
                processed.add(current);
            }
            if (dependencies.containsKey(current)) {
                for (UUID neighbor : dependencies.get(current)) {
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                    if (inDegree.get(neighbor) == 0) queue.add(neighbor);
                }
            }
        }
        if (sorted.size() < valueMap.size()) {
            log.warn("Circular dependency detected in field rules. Processing remaining fields in original order.");
            for (FieldValue v : values) {
                if (v != null && v.getTwinClassField() != null) {
                    UUID id = v.getTwinClassField().getId();
                    if (!processed.contains(id)) {
                        sorted.add(v);
                        processed.add(id);
                    }
                }
            }
        }
        return sorted;
    }

    private FieldRuleOutput evaluateFieldRules(FieldValue value, Map<UUID, FieldValue> contextValues) throws ServiceException {
        TwinClassFieldEntity field = value.getTwinClassField();
        List<TwinClassFieldRuleEntity> rules = field != null && field.getRuleKit() != null
                ? field.getRuleKit().getList()
                : Collections.emptyList();
        FieldRuleOutput output = FieldRuleOutput.builder()
                .field(field)
                .value(value)
                .required(field != null && field.getRequired() != null ? field.getRequired() : false)
                .descriptor(new HashMap<>())
                .build();
        List<TwinClassFieldRuleEntity> copy = new ArrayList<>(rules);
        if (CollectionUtils.isEmpty(copy))
            return output;
        copy.sort(Comparator.comparingInt(r -> r.getRulePriority() != null ? r.getRulePriority() : 0));
        for (TwinClassFieldRuleEntity rule : copy) {
            if (checkRule(rule, contextValues)) {
                applyRuleEffect(rule, output);
                Object outputValue = output.getValue();
                if (outputValue instanceof FieldValue fv) {
                    contextValues.put(output.getField().getId(), fv);
                }
            } else {
                output.setHasError(true);
                output.getDescriptor().put(DESCRIPTOR_RULE_FAILED, String.valueOf(true));
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

    private boolean checkRule(TwinClassFieldRuleEntity rule, Map<UUID, FieldValue> contextValues) throws ServiceException {
        List<TwinClassFieldConditionEntity> conditions = rule.getConditionKit() != null
                ? rule.getConditionKit().getList()
                : Collections.emptyList();
        if (CollectionUtils.isEmpty(conditions))
            return true;

        List<ConditionNode> roots = buildConditionTree(conditions);
        return evaluateRuleTree(roots, contextValues);
    }

    private boolean evaluateRuleTree(List<ConditionNode> roots, Map<UUID, FieldValue> contextValues) throws ServiceException {
        if (CollectionUtils.isEmpty(roots))
            return false;

        for (ConditionNode root : roots) {
            if (evaluateConditionNode(root, contextValues)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateConditionNode(ConditionNode node, Map<UUID, FieldValue> contextValues) throws ServiceException {
        if (node.logic == LogicOperator.LEAF) {
            if (node.condition == null || node.condition.getBaseTwinClassFieldId() == null)
                return false;
            FieldValue fieldValue = contextValues.get(node.condition.getBaseTwinClassFieldId());
            ConditionEvaluator evaluator = featurerService.getFeaturer(node.condition.getConditionEvaluatorFeaturerId(), ConditionEvaluator.class);
            return evaluator.evaluate(node.condition, fieldValue);
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

    @RequiredArgsConstructor
    public static class ConditionNode {
        final TwinClassFieldConditionEntity condition;
        final LogicOperator logic;
        final List<ConditionNode> children = new ArrayList<>();
    }
}
