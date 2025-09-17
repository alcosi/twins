package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionRepository;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleRepository;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;

@Slf4j
@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassFieldMode.class)
public class TwinClassFieldRuleService {

    private final TwinClassFieldRuleRepository twinClassFieldRuleRepository;
    private final TwinClassFieldConditionRepository twinClassFieldConditionRepository;
    private final EntitySmartService entitySmartService;

    /**
     * Persists a TwinClass field–dependency rule together with all its conditions.
     * <p>
     * Steps:
     * 1. Save the rule row first to obtain generated UUID (if not provided by caller).
     * 2. For every condition passed inside the rule – set the generated ruleId and save them in bulk.
     * 3. Attach the persisted conditions set back to the rule entity and return it to the caller.
     * </p>
     */
    @Transactional(rollbackFor = Throwable.class)
    public TwinClassFieldRuleEntity createRule(TwinClassFieldRuleEntity rule) throws Exception {
        if (rule == null)
            return null;

        // Save rule entity first (to get id)
        rule = entitySmartService.save(rule, twinClassFieldRuleRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        // Save conditions if present
        if (CollectionUtils.isNotEmpty(rule.getConditions())) {
            for (TwinClassFieldConditionEntity condition : rule.getConditions()) {
                condition.setRuleId(rule.getId());
            }
            var saved = twinClassFieldConditionRepository.saveAll(rule.getConditions());
            // ensure we have a consistent set instance
            rule.setConditions(new java.util.HashSet<>(org.apache.commons.collections4.IteratorUtils.toList(saved.iterator())));
        }
        return rule;
    }

    /**
     * Bulk creation of multiple rules.
     */
    @Transactional(rollbackFor = Throwable.class)
    public java.util.List<TwinClassFieldRuleEntity> createRules(java.util.List<TwinClassFieldRuleEntity> rules) throws Exception {
        if (CollectionUtils.isEmpty(rules))
            return java.util.Collections.emptyList();
        java.util.List<TwinClassFieldRuleEntity> result = new java.util.ArrayList<>(rules.size());
        for (TwinClassFieldRuleEntity rule : rules) {
            result.add(createRule(rule));
        }
        return result;
    }
}
