package org.twins.core.service.twinclass;

import com.github.f4b6a3.uuid.UuidCreator;
import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
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
import org.twins.core.domain.twinclass.TwinClassFieldConditionTree;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldConditionService extends EntitySecureFindServiceImpl<TwinClassFieldConditionEntity> {
    public static final int MAX_RECURSION_DEPTH = 5;

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

        return StreamSupport.stream(saveSafe(conditions).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldConditionEntity> createConditionsTree(Collection<TwinClassFieldConditionTree> conditionTrees) throws ServiceException {
        if (CollectionUtils.isEmpty(conditionTrees)) {
            return Collections.emptyList();
        }

        List<TwinClassFieldConditionEntity> allEntities = new ArrayList<>();

        for (TwinClassFieldConditionTree tree : conditionTrees) {
            List<TwinClassFieldConditionEntity> treeEntities = flattenConditionTree(tree);
            allEntities.addAll(treeEntities);
        }

        return StreamSupport.stream(saveSafe(allEntities).spliterator(), false).toList();
    }

    /**
     * Converts a condition tree into a flat list of entities
     */
    private List<TwinClassFieldConditionEntity> flattenConditionTree(TwinClassFieldConditionTree tree) throws ServiceException {
        List<TwinClassFieldConditionEntity> result = new ArrayList<>();
        flattenTreeRecursive(tree, null, result, 0);
        return result;
    }

    /**
     * Recursively traverses the tree and init entities
     */
    private void flattenTreeRecursive(TwinClassFieldConditionTree node,
                                      UUID parentId,
                                      List<TwinClassFieldConditionEntity> result,
                                      int currentDepth) throws ServiceException {
        if (currentDepth > MAX_RECURSION_DEPTH) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_CONDITION_DEPTH_EXCEEDED, " maximum depth is " + MAX_RECURSION_DEPTH);
        }

        TwinClassFieldConditionEntity entity = new TwinClassFieldConditionEntity()
                .setId(UuidCreator.getTimeOrdered())
                .setTwinClassFieldRuleId(node.getTwinClassFieldRuleId())
                .setBaseTwinClassFieldId(node.getBaseTwinClassFieldId())
                .setConditionOrder(node.getConditionOrder())
                .setConditionEvaluatorFeaturerId(node.getConditionEvaluatorFeaturerId())
                .setConditionEvaluatorParams(node.getConditionEvaluatorParams())
                .setLogicOperatorId(node.getLogicOperator())
                .setParentTwinClassFieldConditionId(parentId);

        result.add(entity);

        if (node.getChildConditions() != null && !node.getChildConditions().isEmpty()) {
            for (TwinClassFieldConditionTree childTree : node.getChildConditions()) {
                flattenTreeRecursive(childTree, entity.getId(), result, currentDepth + 1);
            }
        }
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
