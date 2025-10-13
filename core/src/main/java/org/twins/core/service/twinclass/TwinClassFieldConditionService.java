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
        Kit<TwinClassFieldConditionEntity, UUID> needLoad = new Kit<>(TwinClassFieldConditionEntity::getBaseTwinClassFieldId);
        Set<UUID> forFields = new HashSet<>();
        for (var entity : entities) {
            if (entity.getBaseTwinClassField() == null) {
                needLoad.add(entity);
                forFields.add(entity.getBaseTwinClassFieldId());
            }
        }
        if (needLoad.isEmpty())
            return;
        Kit<TwinClassFieldEntity, UUID> loaded = twinClassFieldService.findEntitiesSafe(forFields);
        for (var entity : needLoad) {
            entity.setBaseTwinClassField(loaded.get(entity.getBaseTwinClassFieldId()));
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
