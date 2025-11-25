package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
        Kit<TwinClassFieldRuleEntity, UUID> needLoad = new Kit<>(TwinClassFieldRuleEntity::getId);
        for (TwinClassFieldRuleEntity ruleEntity : ruleEntities) {
            if (ruleEntity.getConditionKit() == null) {
                needLoad.add(ruleEntity);
            }
        }
        if (needLoad.isEmpty())
            return;
        KitGrouped<TwinClassFieldConditionEntity, UUID, UUID> conditions = new KitGrouped<>(twinClassFieldConditionRepository.findByTwinClassFieldRuleIdIn(needLoad.getIdSet()), TwinClassFieldConditionEntity::getId, TwinClassFieldConditionEntity::getTwinClassFieldRuleId);
        for (TwinClassFieldRuleEntity ruleEntity : needLoad) {
            if (conditions.containsGroupedKey(ruleEntity.getId()))
                ruleEntity.setConditionKit(new Kit<>(conditions.getGrouped(ruleEntity.getId()), TwinClassFieldConditionEntity::getId));
            else
                ruleEntity.setConditionKit(Kit.EMPTY);
        }
    }

    public void loadBaseTwinClassField(TwinClassFieldConditionEntity conditionEntity) throws ServiceException {
        loadBaseTwinClassFields(Collections.singleton(conditionEntity));
    }

    public void loadBaseTwinClassFields(Collection<TwinClassFieldConditionEntity> entities) throws ServiceException {
        KitGrouped<TwinClassFieldConditionEntity, UUID, UUID> needLoad = new KitGrouped<>(TwinClassFieldConditionEntity::getId, TwinClassFieldConditionEntity::getBaseTwinClassFieldId);
        for (var entity : entities) {
            if (entity.getBaseTwinClassField() == null) {
                needLoad.add(entity);
            }
        }
        if (needLoad.isEmpty())
            return;
        Kit<TwinClassFieldEntity, UUID> loaded = twinClassFieldService.findEntitiesSafe(needLoad.getGroupedKeySet());
        for (var entity : needLoad) {
            entity.setBaseTwinClassField(loaded.get(entity.getBaseTwinClassFieldId()));
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldConditionEntity> createConditions(Collection<TwinClassFieldConditionEntity> conditions) throws ServiceException {
        if (CollectionUtils.isEmpty(conditions))
            return Collections.emptyList();

       for (TwinClassFieldConditionEntity condition : conditions) {
           validateEntityAndThrow(condition, EntitySmartService.EntityValidateMode.beforeSave);
       }

        return StreamSupport.stream(entityRepository().saveAll(conditions).spliterator(), false).toList();
    }

    public void deleteConditions(UUID twinClassId) throws ServiceException {
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
        if (null == entity.getTwinClassFieldRuleId())
            return logErrorAndReturnFalse(entity.logNormal() + " empty twinClassFieldRuleId");
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
