package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldConditionService extends EntitySecureFindServiceImpl<TwinClassFieldConditionEntity> {
    private final TwinClassFieldConditionRepository twinClassFieldConditionRepository;
    private final EntitySmartService entitySmartService;

    @Lazy
    private final TwinClassService twinClassService;

    @Lazy
    private final TwinClassFieldService twinClassFieldService;


    public void loadConditions(TwinClassFieldRuleEntity ruleEntity) {
        loadConditions(Collections.singleton(ruleEntity));
    }

    public void loadConditions(Collection<TwinClassFieldRuleEntity> ruleEntities) {
        Map<UUID, TwinClassFieldRuleEntity> needLoad = new HashMap<>();
        Set<UUID> forRules = new HashSet<>();
        for (TwinClassFieldRuleEntity ruleEntity : ruleEntities)
            if (ruleEntity.getConditionKit() == null) {
                needLoad.put(ruleEntity.getId(), ruleEntity);
                forRules.add(ruleEntity.getId());
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<TwinClassFieldConditionEntity, UUID, UUID> conditions = new KitGrouped<>(twinClassFieldConditionRepository.findByTwinClassFieldRuleIdIn(forRules), TwinClassFieldConditionEntity::getId, TwinClassFieldConditionEntity::getTwinClassFieldRuleId);
        for (TwinClassFieldRuleEntity ruleEntity : needLoad.values()) {
            List<TwinClassFieldConditionEntity> ruleConditions = new ArrayList<>();
            if (conditions.containsGroupedKey(ruleEntity.getId()))
                ruleConditions.addAll(conditions.getGrouped(ruleEntity.getId()));
            ruleEntity.setConditionKit(new Kit<>(conditions, TwinClassFieldConditionEntity::getId));
        }
    }

    public List<TwinClassFieldConditionEntity> saveConditions(Collection<TwinClassFieldConditionEntity> conditions) {
        Iterable<TwinClassFieldConditionEntity> savedEntities = entitySmartService.saveAllAndLog(
                conditions,
                twinClassFieldConditionRepository
        );
        List<TwinClassFieldConditionEntity> result = StreamSupport.stream(savedEntities.spliterator(), false)
                .collect(Collectors.toList());
        return result;
    }

    public void deleteConditions(UUID twinClassId) throws ServiceException{
        if (twinClassId == null)
            return;
        twinClassService.findEntitySafe(twinClassId);
        twinClassFieldConditionRepository.deleteByTwinClassId(twinClassId);
    }

    @Override
    public CrudRepository<TwinClassFieldConditionEntity, UUID> entityRepository() {
        return twinClassFieldConditionRepository;
    }

    @Override
    public Function<TwinClassFieldConditionEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldConditionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldConditionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findEntitySafe(entity.getBaseTwinClassFieldId());
        return twinClassFieldService.isEntityReadDenied(twinClassFieldEntity, readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinClassFieldConditionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
         if (null == entity.getBaseTwinClassFieldId())
            return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_CONDITION_BASE_FIELD_NOT_SPECIFIED.getMessage());
        if (null == entity.getConditionEvaluatorFeaturerId())
            return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_CONDITION_FEATURER_NOT_SPECIFIED.getMessage());
         switch (entityValidateMode) {
            case beforeSave:
                if (entity.getBaseTwinClassField() == null || !entity.getBaseTwinClassField().getId().equals(entity.getBaseTwinClassFieldId()))
                    entity.setBaseTwinClassField(twinClassFieldService.findEntitySafe(entity.getBaseTwinClassFieldId()));
            default:
        }
        return true;
    }
}
