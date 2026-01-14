package org.twins.core.service.twinclass;

import com.github.f4b6a3.uuid.UuidCreator;
import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleMapEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleRepository;
import org.twins.core.domain.twinclass.TwinClassFieldConditionTree;
import org.twins.core.domain.twinclass.TwinClassFieldRuleSave;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;
import java.util.function.Function;

import static org.twins.core.service.twinclass.TwinClassFieldConditionService.MAX_RECURSION_DEPTH;

@Slf4j
@Component
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldRuleService extends EntitySecureFindServiceImpl<TwinClassFieldRuleEntity> {
    private static int FIELD_OVERWRITER_STUB_ID = FeaturerTwins.ID_4601; // "no overwriter" stub featurer

    private final TwinClassFieldRuleRepository twinClassFieldRuleRepository;
    private final EntitySmartService entitySmartService;
    private final TwinClassFieldConditionService twinClassFieldConditionService;
    private final TwinClassFieldRuleMapService twinClassFieldRuleMapService;

    @Lazy
    private final TwinClassService twinClassService;

    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public CrudRepository<TwinClassFieldRuleEntity, UUID> entityRepository() {
        return twinClassFieldRuleRepository;
    }

    @Override
    public Function<TwinClassFieldRuleEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldRuleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldRuleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (null == entity.getFieldOverwriterFeaturerId())
            return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_RULE_FEATURER_NOT_SPECIFIED.getMessage());
        if (null == entity.getOverwrittenValue()
                && null == entity.getOverwrittenRequired()
                && entity.getFieldOverwriterFeaturerId() == FIELD_OVERWRITER_STUB_ID)
            return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_RULE_OVERWRITTEN_VALUE_NOT_SPECIFIED.getMessage());

        return true;
    }

    /**
     * Bulk creation of multiple rules.
     * Persists a TwinClass field–dependency rule together with all its conditions.
     * <p>
     * Steps:
     * 1. Save the rule row first to obtain generated UUID (if not provided by caller).
     * 2. For every condition passed inside the rule – set the generated ruleId and save them in bulk.
     * 3. Attach the persisted conditions set back to the rule entity and return it to the caller.
     * </p>
     */
    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldRuleEntity> createRules(List<TwinClassFieldRuleSave> rules) throws Exception {
        if (CollectionUtils.isEmpty(rules))
            return Collections.emptyList();
        List<TwinClassFieldRuleEntity> result = new ArrayList<>(rules.size());
        List<TwinClassFieldConditionTree> conditionsToSave = new ArrayList<>();
        List<TwinClassFieldRuleMapEntity> ruleMapsToSave = new ArrayList<>();

        for (TwinClassFieldRuleSave ruleSave : rules) {
            TwinClassFieldRuleEntity rule = ruleSave.getTwinClassFieldRule();

            if (rule.getFieldOverwriterFeaturerId() == null) {
                rule.setFieldOverwriterFeaturerId(FIELD_OVERWRITER_STUB_ID);
            }
            if (rule.getId() == null) {
                rule.setId(UuidCreator.getTimeOrdered());
            }

            if (ruleSave.getTwinClassFieldConditionTrees() != null) {
                for (var conditionTree : ruleSave.getTwinClassFieldConditionTrees()) {
                    setRuleIdForConditionTree(conditionTree, rule.getId(), 0);
                    conditionsToSave.add(conditionTree);
                }
            }

            if (ruleSave.getTwinClassFieldIds() != null && !ruleSave.getTwinClassFieldIds().isEmpty()) {
                for (UUID twinClassFieldId : ruleSave.getTwinClassFieldIds()) {
                    TwinClassFieldRuleMapEntity ruleMap = new TwinClassFieldRuleMapEntity()
                            .setTwinClassFieldRuleId(rule.getId())
                            .setTwinClassFieldId(twinClassFieldId);
                    ruleMapsToSave.add(ruleMap);
                }
            }

            result.add(rule);
        }
        entitySmartService.saveAllAndLog(result, twinClassFieldRuleRepository);
        if (!conditionsToSave.isEmpty())
            twinClassFieldConditionService.createConditionsTree(conditionsToSave);
        if (!ruleMapsToSave.isEmpty()) {
            twinClassFieldRuleMapService.createRuleMaps(ruleMapsToSave);
        }
        return result;
    }

    private void setRuleIdForConditionTree(TwinClassFieldConditionTree conditionTree, UUID ruleId, int currentDepth) throws ServiceException {
        if (currentDepth > MAX_RECURSION_DEPTH) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_CONDITION_DEPTH_EXCEEDED, " maximum depth is " + MAX_RECURSION_DEPTH);
        }

        conditionTree.setTwinClassFieldRuleId(ruleId);
        if (conditionTree.getChildConditions() != null) {
            for (TwinClassFieldConditionTree child : conditionTree.getChildConditions()) {
                setRuleIdForConditionTree(child, ruleId, currentDepth + 1);
            }
        }
    }

    /**
     * Removes every rule and its conditions associated with the given Twin-Class.
     * <p>Order is important: first conditions – because they reference the rules via FK, then rules themselves.</p>
     */
    @Transactional(rollbackFor = Throwable.class)
    public void deleteRulesByTwinClass(UUID twinClassId) throws ServiceException {
        if (twinClassId == null)
            return;

        twinClassService.findEntitySafe(twinClassId);
        twinClassFieldConditionService.deleteConditions(twinClassId);
        Set<UUID> ruleIdsToDelete = twinClassFieldRuleRepository.findRuleIdsByTwinClassId(twinClassId);
        twinClassFieldRuleMapService.deleteRuleMaps(twinClassId);
        twinClassFieldRuleRepository.deleteAllById(ruleIdsToDelete);
    }
}
