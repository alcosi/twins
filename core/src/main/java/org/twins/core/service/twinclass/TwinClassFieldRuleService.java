package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionRepository;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleRepository;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;

import java.util.*;

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
        Set<TwinClassFieldConditionEntity> conditions = rule.getConditions();
        rule.setConditions(null); // avoid FK constraint violation on initial save
        // Save rule entity first (to get id)
        rule = entitySmartService.save(rule, twinClassFieldRuleRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        // Save conditions if present
        if (CollectionUtils.isNotEmpty(conditions)) {
            for (TwinClassFieldConditionEntity condition : conditions) {
                condition.setTwinClassFieldRuleId(rule.getId());
            }
            var saved = twinClassFieldConditionRepository.saveAll(conditions);
            // ensure we have a consistent set instance
            rule.setConditions(new java.util.HashSet<>(IteratorUtils.toList(saved.iterator())));
        }
        return rule;
    }

    /**
     * Bulk creation of multiple rules.
     */
  //  @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldRuleEntity> createRules(List<TwinClassFieldRuleEntity> rules) throws Exception {
        if (CollectionUtils.isEmpty(rules))
            return Collections.emptyList();
        List<TwinClassFieldRuleEntity> result = new ArrayList<>(rules.size());
        for (TwinClassFieldRuleEntity rule : rules) {
            result.add(createRule(rule));
        }
        return result;
    }


    /**
     * Returns all rules (with eager-loaded conditions) for the specified Twin-Class.
     */
    public List<TwinClassFieldRuleEntity> getRulesByTwinClass(UUID twinClassId) {
        if (twinClassId == null)
            return Collections.emptyList();
        return twinClassFieldRuleRepository.findByTwinClassId(twinClassId);
    }

    /**
     * Returns all rules (with eager-loaded conditions) that affect the specified Twin-Class field.
     */
    public List<TwinClassFieldRuleEntity> loadRulesByTwinClassField(UUID twinClassFieldId) {
        if (twinClassFieldId == null)
            return Collections.emptyList();
        return twinClassFieldRuleRepository.findByDependentTwinClassFieldId(twinClassFieldId);
    }



    /**
     * Removes every rule and its conditions associated with the given Twin-Class.
     * <p>Order is important: first conditions – because they reference the rules via FK, then rules themselves.</p>
     */
    @Transactional(rollbackFor = Throwable.class)
    public void deleteRulesByTwinClass(UUID twinClassId) {
        if (twinClassId == null)
            return;
        twinClassFieldConditionRepository.deleteByTwinClassId(twinClassId);
        twinClassFieldRuleRepository.deleteByTwinClassId(twinClassId);
    }


}
