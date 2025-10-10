package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwinClassFieldRuleService extends EntitySecureFindServiceImpl<TwinClassFieldRuleEntity> {
    private static int FIELD_OVERWRITER_STUB_ID = FeaturerTwins.ID_4601; // "no overwriter" stub featurer

    private final TwinClassFieldRuleRepository twinClassFieldRuleRepository;
    private final EntitySmartService entitySmartService;
    private final TwinClassFieldConditionService twinClassFieldConditionService;

    @Lazy
    private final AuthService authService;
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
        return twinClassFieldService.isEntityReadDenied(entity.getTwinClassField(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinClassFieldRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (null == entity.getTwinClassFieldId())
            return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_RULE_TWIN_CLASS_FIELD_NOT_SPECIFIED.getMessage());
        if (null == entity.getFieldOverwriterFeaturerId())
            return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_RULE_FEATURER_NOT_SPECIFIED.getMessage());
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinClassField() == null || !entity.getTwinClassField().getId().equals(entity.getTwinClassFieldId()))
                    entity.setTwinClassField(twinClassFieldService.findEntitySafe(entity.getTwinClassFieldId()));
            default:
        }
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
    public List<TwinClassFieldRuleEntity> createRules(List<TwinClassFieldRuleEntity> rules) throws Exception {
        if (CollectionUtils.isEmpty(rules))
            return Collections.emptyList();
        List<TwinClassFieldRuleEntity> result = new ArrayList<>(rules.size());
        List<TwinClassFieldConditionEntity> conditionsToSave = new ArrayList<>();
        for (TwinClassFieldRuleEntity rule : rules) {
            if (rule.getFieldOverwriterFeaturerId() == null) {
                rule.setFieldOverwriterFeaturerId(FIELD_OVERWRITER_STUB_ID);
            }
            if (rule.getId() == null) {
                rule.setId(UUID.randomUUID()); //change to uuidV7
            }
            if (KitUtils.isNotEmpty(rule.getConditionKit())) {
                for (var condition : rule.getConditionKit()) {
                    condition.setTwinClassFieldRuleId(rule.getId());
                    condition.setTwinClassFieldRule(rule);
                    conditionsToSave.add(condition);
                }
            }
        }
        entitySmartService.saveAllAndLog(rules, twinClassFieldRuleRepository);
        if (!conditionsToSave.isEmpty())
            twinClassFieldConditionService.saveConditions(conditionsToSave);
        return result;
    }

    public void loadRules(TwinClassFieldEntity ruleEntity) {
        loadRules(Collections.singleton(ruleEntity));
    }

    public void loadRules(Collection<TwinClassFieldEntity> fieldEntities) {
        Kit<TwinClassFieldEntity, UUID> needLoad = new Kit<>(TwinClassFieldEntity::getId);
        for (TwinClassFieldEntity fieldEntity : fieldEntities) {
            if (fieldEntity.getRuleKit() == null) {
                needLoad.add(fieldEntity);
            }
        }
        if (needLoad.isEmpty())
            return;
        KitGrouped<TwinClassFieldRuleEntity, UUID, UUID> rules = new KitGrouped<>(twinClassFieldRuleRepository.findByTwinClassFieldIdIn(needLoad.getIdSet()), TwinClassFieldRuleEntity::getId, TwinClassFieldRuleEntity::getTwinClassFieldId);
        for (TwinClassFieldEntity fieldEntity : needLoad) {
            if (rules.containsGroupedKey(fieldEntity.getId()))
                fieldEntity.setRuleKit(new Kit<>(rules.getGrouped(fieldEntity.getId()), TwinClassFieldRuleEntity::getId));
            else
                fieldEntity.setRuleKit(Kit.EMPTY);
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
        twinClassFieldRuleRepository.deleteByTwinClassId(twinClassId);
    }
}
