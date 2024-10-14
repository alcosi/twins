package org.twins.core.service.twinflow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinflow.*;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.*;
import org.twins.core.domain.factory.FactoryBranchId;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.TransitionSearch;
import org.twins.core.domain.transition.TransitionContext;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserGroupService;
import org.twins.core.service.user.UserService;

import java.util.*;

import static org.cambium.common.util.CacheUtils.evictCache;


@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionService extends EntitySecureFindServiceImpl<TwinflowTransitionEntity> {
    private final TwinflowTransitionRepository twinflowTransitionRepository;
    private final TwinflowTransitionValidatorRepository twinflowTransitionValidatorRepository;
    private final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;
    private final TwinStatusTransitionTriggerRepository twinStatusTransitionTriggerRepository;
    private final TwinflowTransitionAliasRepository twinflowTransitionAliasRepository;
    private final TwinClassService twinClassService;
    private final TwinFactoryService twinFactoryService;
    private final TwinStatusService twinStatusService;
    private final TwinflowTransitionSearchService twinflowTransitionSearchService;
    @Lazy
    private final TwinService twinService;
    private final TwinflowService twinflowService;
    @Lazy
    private final FeaturerService featurerService;
    @Lazy
    private final AuthService authService;
    private final UserGroupService userGroupService;
    private final PermissionService permissionService;
    private final UserService userService;
    private final I18nService i18nService;

    @Autowired
    private CacheManager cacheManager;

    @Override
    public CrudRepository<TwinflowTransitionEntity, UUID> entityRepository() {
        return twinflowTransitionRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowTransitionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinflowTransitionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinflowId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinFlowId");
        if (entity.getDstTwinStatusId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty dstTwinStatusId");

        switch (entityValidateMode) {
            case beforeSave:
                if ((entity.getSrcTwinStatus() == null && entity.getSrcTwinStatusId() != null) || (entity.getSrcTwinStatus() != null && !entity.getSrcTwinStatus().getId().equals(entity.getSrcTwinStatusId())))
                    entity.setSrcTwinStatus(twinStatusService.findEntitySafe(entity.getSrcTwinStatusId()));
                if (entity.getDstTwinStatus() == null || !entity.getDstTwinStatus().getId().equals(entity.getDstTwinStatusId()))
                    entity.setDstTwinStatus(twinStatusService.findEntitySafe(entity.getDstTwinStatusId()));
                if (entity.getTwinflow() == null || !entity.getTwinflow().getId().equals(entity.getTwinflowId()))
                    entity.setTwinflow(twinflowService.findEntitySafe(entity.getTwinflowId()));
                if ((entity.getPermission() == null && entity.getPermissionId() != null) || (entity.getPermission() != null && !entity.getPermission().getId().equals(entity.getPermissionId())))
                    entity.setPermission(permissionService.findEntitySafe(entity.getPermissionId()));
                if (entity.getCreatedByUser() == null || !entity.getCreatedByUser().getId().equals(entity.getCreatedByUserId()))
                    entity.setCreatedByUser(userService.findEntitySafe(entity.getCreatedByUserId()));
            default:
                if (entity.getSrcTwinStatusId() != null
                        && (!twinClassService.isInstanceOf(entity.getSrcTwinStatus().getTwinClass(), entity.getDstTwinStatus().getTwinClassId())
                        || !twinClassService.isInstanceOf(entity.getDstTwinStatus().getTwinClass(), entity.getSrcTwinStatus().getTwinClassId())))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incompatible src -> dst status classes [" + entity.getSrcTwinStatusId() + " > " + entity.getDstTwinStatusId() + "]");
                if (!twinClassService.isInstanceOf(entity.getTwinflow().getTwinClass(), entity.getDstTwinStatus().getTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incompatible twinflow -> dst status classes [" + entity.getTwinflowId() + " > " + entity.getDstTwinStatusId() + "]");
                if (entity.getPermission() != null) {
                    permissionService.validateEntity(entity.getPermission(), EntitySmartService.EntityValidateMode.afterRead); // we are not saving permission entity that is why we can use read mode
                    if (entity.getPermission().getPermissionGroup() != null
                            && entity.getPermission().getPermissionGroup().getTwinClassId() != null
                            && !twinClassService.isInstanceOf(entity.getTwinflow().getTwinClass(), entity.getPermission().getPermissionGroup().getTwinClassId()))
                        return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incompatible twinflow -> permission classes [" + entity.getTwinflowId() + " > " + entity.getPermissionId() + "]");
                    ;
                }
        }
        return true;
    }

    public void loadAllTransitions(List<TwinClassEntity> twinClasses) {
        for (TwinClassEntity twinClass : twinClasses) {
            twinflowService.loadTwinflows(twinClass);
            if (null != twinClass.getTwinflowKit() && twinClass.getTwinflowKit().isNotEmpty())
                twinClass.setTransitionsKit(new Kit<>(twinflowTransitionRepository.findByTwinflowIdIn(twinClass.getTwinflowKit().getIdSet()), TwinflowTransitionEntity::getTwinflowId));
        }
    }

    public void loadAllTransitions(TwinflowEntity twinflowEntity) {
        if (twinflowEntity.getTransitionsKit() != null)
            return;
        twinflowEntity.setTransitionsKit(new Kit<>(twinflowTransitionRepository.findByTwinflowId(twinflowEntity.getId()), TwinflowTransitionEntity::getId));
    }

    public PermissionEntity loadPermission(TwinflowTransitionEntity twinflowTransitionEntity) throws ServiceException {
        if (twinflowTransitionEntity.getPermissionId() == null)
            return null;
        if (twinflowTransitionEntity.getPermission() != null)
            return twinflowTransitionEntity.getPermission();
        twinflowTransitionEntity.setPermission(permissionService.findEntitySafe(twinflowTransitionEntity.getPermissionId()));
        return twinflowTransitionEntity.getPermission();
    }

    public UserEntity loadCreatedBy(TwinflowTransitionEntity twinflowTransitionEntity) throws ServiceException {
        if (twinflowTransitionEntity.getCreatedByUser() != null)
            return twinflowTransitionEntity.getCreatedByUser();
        twinflowTransitionEntity.setCreatedByUser(userService.findEntitySafe(twinflowTransitionEntity.getCreatedByUserId()));
        return twinflowTransitionEntity.getCreatedByUser();
    }

    public Kit<TwinflowTransitionEntity, UUID> loadValidTransitions(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getValidTransitionsKit() != null)
            return twinEntity.getValidTransitionsKit();
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroups(apiUser);
        twinflowService.loadTwinflow(twinEntity);
        List<TwinflowTransitionEntity> twinflowTransitionEntityList = twinflowTransitionRepository.findValidTransitions(
                twinEntity.getTwinflow().getId(),
                twinEntity.getTwinStatusId(),
                apiUser.getDomainId(),
                TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                TypedParameterTwins.uuidNullable(twinEntity.getPermissionSchemaSpaceId()),
                apiUser.getUser().getId(),
                TypedParameterTwins.uuidArray(apiUser.getUserGroups()),
                TypedParameterTwins.uuidNullable(twinEntity.getTwinClassId()),
                TwinService.isAssignee(twinEntity, apiUser),
                TwinService.isCreator(twinEntity, apiUser));
        filterTransitions(twinEntity, twinflowTransitionEntityList);
        return twinEntity.getValidTransitionsKit();
    }

    public PaginationResult<TwinflowTransitionEntity> search(TransitionSearch transitionSearch, SimplePagination pagination) throws ServiceException {
        return twinflowTransitionSearchService.findTransitions(transitionSearch, pagination);
    }

    private void filterTransitions(TwinEntity twinEntity, List<TwinflowTransitionEntity> twinflowTransitionEntityList) throws ServiceException {
        if (CollectionUtils.isEmpty(twinflowTransitionEntityList)) {
            twinEntity.setValidTransitionsKit(new Kit<>(twinflowTransitionEntityList, TwinflowTransitionEntity::getId)); // this will help to avoid loading one more time
            return;
        }
        /* we can have 2 concurrent transitions to same dst_status_id and same alias:
        1. with src_status_id = null - case of "from any" transition
        2. with specific src_status_id
        Second case has more priority
        This logic can be done with postgres sql "distinct on" operator, but it's not supported in hibernate
        */
        Map<String, TwinflowTransitionEntity> alreadyAdded = new HashMap<>(); // key = alias_id + dst_status
        for (TwinflowTransitionEntity transitionEntity : twinflowTransitionEntityList) {
            String transitionDistinctKey = transitionEntity.getTwinflowTransitionAlias().getAlias() + transitionEntity.getDstTwinStatusId();
            if (alreadyAdded.containsKey(transitionDistinctKey)
                    && alreadyAdded.get(transitionDistinctKey).getSrcTwinStatusId() != null)
                continue; //skipping current transition because we already have one with specific src_status
            if (runTransitionValidators(transitionEntity, twinEntity))
                alreadyAdded.put(transitionDistinctKey, transitionEntity);
        }
        twinEntity.setValidTransitionsKit(new Kit<>(alreadyAdded.values().stream().toList(), TwinflowTransitionEntity::getId));
    }

    public void loadValidTransitions(Collection<TwinEntity> twinEntityList) throws ServiceException {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList) {
            if (twinEntity.getValidTransitionsKit() != null)
                continue;
            needLoad.put(twinEntity.getId(), twinEntity);
        }
        if (MapUtils.isEmpty(needLoad))
            return;
        twinflowService.loadTwinflow(needLoad.values());
        Map<TransitionDetectKey, List<TwinEntity>> detectKeyMap = convertToDetectKeys(needLoad.values());
        ApiUser apiUser = authService.getApiUser();
        List<TwinflowTransitionEntity> twinflowTransitionEntityList;
        TransitionDetectKey detectKey;
        for (Map.Entry<TransitionDetectKey, List<TwinEntity>> entry : detectKeyMap.entrySet()) {
            detectKey = entry.getKey();
            twinflowTransitionEntityList = twinflowTransitionRepository.findValidTransitions(
                    detectKey.twinflowId,
                    detectKey.srcStatusId,
                    apiUser.getDomainId(),
                    TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                    TypedParameterTwins.uuidNullable(detectKey.permissionSpaceId),
                    apiUser.getUser().getId(),
                    TypedParameterTwins.uuidArray(apiUser.getUserGroups()),
                    TypedParameterTwins.uuidNullable(detectKey.twinClassId),
                    detectKey.isAssignee,
                    detectKey.isCreator);
            for (TwinEntity twinEntity : entry.getValue()) {
                filterTransitions(twinEntity, twinflowTransitionEntityList);
            }
        }
    }

    private Map<TransitionDetectKey, List<TwinEntity>> convertToDetectKeys(Collection<TwinEntity> twinEntities) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Map<TransitionDetectKey, List<TwinEntity>> triples = new HashMap<>();
        TransitionDetectKey triple;
        for (TwinEntity twinEntity : twinEntities) {
            triple = new TransitionDetectKey(
                    twinEntity.getTwinflow().getId(),
                    twinEntity.getTwinStatusId(),
                    twinEntity.getPermissionSchemaSpaceId(),
                    TwinService.isAssignee(twinEntity, apiUser),
                    TwinService.isCreator(twinEntity, apiUser),
                    twinEntity.getTwinClassId());
            triples.computeIfAbsent(triple, k -> new ArrayList<>());
            triples.get(triple).add(twinEntity);
        }
        return triples;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinflowTransitionEntity createTwinflowTransition(TwinflowTransitionEntity twinflowTransitionEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        TwinflowTransitionAliasEntity twinflowTransitionAlias = creatAliasIfNeeded(twinflowTransitionEntity.getTwinflowTransitionAlias());
        twinflowTransitionEntity
                .setNameI18NId(i18nService.createI18nAndTranslations(I18nType.TWINFLOW_TRANSITION_NAME, nameI18n).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.TWINFLOW_TRANSITION_DESCRIPTION, descriptionI18n).getId())
                .setCreatedByUserId(apiUser.getUserId())
                .setTwinflowTransitionAliasId(twinflowTransitionAlias.getId())
                .setTwinflowTransitionAlias(twinflowTransitionAlias);

        validateEntityAndThrow(twinflowTransitionEntity, EntitySmartService.EntityValidateMode.beforeSave);
        return entitySmartService.save(twinflowTransitionEntity, twinflowTransitionRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }

    private TwinflowTransitionAliasEntity creatAliasIfNeeded(TwinflowTransitionAliasEntity transitionAlias) throws ServiceException {
        if (transitionAlias.getDomainId() == null)
            transitionAlias.setDomainId(authService.getApiUser().getDomainId());
        if (transitionAlias.getId() != null)
            return transitionAlias;
        TwinflowTransitionAliasEntity currentTransitionAlias = twinflowTransitionAliasRepository.findByDomainIdAndAlias(transitionAlias.getDomainId(), transitionAlias.getAlias());
        if (currentTransitionAlias != null)
            transitionAlias.setId(currentTransitionAlias.getId());
        else
            transitionAlias = saveTwinflowTransitionAlias(transitionAlias);
        return transitionAlias;
    }

    private TwinflowTransitionAliasEntity saveTwinflowTransitionAlias(TwinflowTransitionAliasEntity transitionAlias) throws ServiceException {
        return entitySmartService.save(transitionAlias, twinflowTransitionAliasRepository, EntitySmartService.SaveMode.saveAndLogOnException);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinflowTransitionEntity updateTwinflowTransition(TwinflowTransitionEntity twinflowTransitionEntity, I18nEntity nameI18n, I18nEntity descriptionI18n, EntityCUD<TwinflowTransitionValidatorEntity> validatorCUD, EntityCUD<TwinflowTransitionTriggerEntity> triggerCUD) throws ServiceException {
        TwinflowTransitionEntity dbTwinflowTransitionEntity = findEntitySafe(twinflowTransitionEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        cudValidators(dbTwinflowTransitionEntity, validatorCUD);
        cudTriggers(dbTwinflowTransitionEntity, triggerCUD);
        updateTransitionAlias(dbTwinflowTransitionEntity, twinflowTransitionEntity.getTwinflowTransitionAlias(), changesHelper);
        updateTransitionName(dbTwinflowTransitionEntity, nameI18n, changesHelper);
        updateTransitionDescription(dbTwinflowTransitionEntity, descriptionI18n, changesHelper);
        updateTransitionInBuildFactory(dbTwinflowTransitionEntity, twinflowTransitionEntity.getInbuiltTwinFactoryId(), changesHelper);
        updateTransitionDraftingFactory(dbTwinflowTransitionEntity, twinflowTransitionEntity.getDraftingTwinFactoryId(), changesHelper);
        updateTransitionPermission(dbTwinflowTransitionEntity, twinflowTransitionEntity.getPermissionId(), changesHelper);
        updateTransitionSrcStatus(dbTwinflowTransitionEntity, twinflowTransitionEntity.getSrcTwinStatusId(), changesHelper);
        updateTransitionDstStatus(dbTwinflowTransitionEntity, twinflowTransitionEntity.getDstTwinStatusId(), changesHelper);
        validateEntity(dbTwinflowTransitionEntity, EntitySmartService.EntityValidateMode.beforeSave);
        dbTwinflowTransitionEntity = entitySmartService.saveAndLogChanges(dbTwinflowTransitionEntity, twinflowTransitionRepository, changesHelper);
        evictCache(cacheManager, TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, dbTwinflowTransitionEntity.getTwinflow().getTwinClassId());
        return dbTwinflowTransitionEntity;
    }

    public void cudValidators(TwinflowTransitionEntity dbTwinflowTransitionEntity, EntityCUD<TwinflowTransitionValidatorEntity> validatorCUD) throws ServiceException {
        if (validatorCUD == null)
            return;
        if (CollectionUtils.isNotEmpty(validatorCUD.getCreateList())) {
            createValidators(dbTwinflowTransitionEntity, validatorCUD.getCreateList());
        }
        if (CollectionUtils.isNotEmpty(validatorCUD.getUpdateList())) {
            updateValidators(dbTwinflowTransitionEntity, validatorCUD.getUpdateList());
        }
        if (CollectionUtils.isNotEmpty(validatorCUD.getDeleteUUIDList())) {
            deleteValidators(dbTwinflowTransitionEntity, validatorCUD.getDeleteUUIDList());
        }
        evictCache(cacheManager, TwinflowTransitionValidatorRepository.CACHE_TRANSITION_VALIDATOR_BY_TRANSITION_ID_ORDERED, dbTwinflowTransitionEntity.getId());
    }

    public void deleteValidators(TwinflowTransitionEntity dbTwinflowTransitionEntity, List<UUID> validatorDeleteUUIDList) throws ServiceException {
        Kit<TwinflowTransitionValidatorEntity, UUID> deleteEntityKit = new Kit<>(twinflowTransitionValidatorRepository.findAllByTwinflowTransitionIdAndIdIn(dbTwinflowTransitionEntity.getId(), validatorDeleteUUIDList), TwinflowTransitionValidatorEntity::getId);
        if (CollectionUtils.isEmpty(deleteEntityKit.getCollection()))
            return;
        for (UUID validatorUuid : validatorDeleteUUIDList) {
            TwinflowTransitionValidatorEntity validator = deleteEntityKit.get(validatorUuid);
            if (null == validator)
                throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "cant find transitionValidator[" + validatorUuid + "] for delete operation");
            log.info(validator.logDetailed() + " will be deleted");
        }
        twinflowTransitionValidatorRepository.deleteAllByTwinflowTransitionIdAndIdIn(dbTwinflowTransitionEntity.getId(), validatorDeleteUUIDList);
    }

    public List<TwinflowTransitionValidatorEntity> createValidators(TwinflowTransitionEntity dbTwinflowTransitionEntity, List<TwinflowTransitionValidatorEntity> validators) throws ServiceException {
        for (TwinflowTransitionValidatorEntity validator : validators)
            validator.setTwinflowTransitionId(dbTwinflowTransitionEntity.getId());
        return IterableUtils.toList(entitySmartService.saveAllAndLog(validators, twinflowTransitionValidatorRepository));
    }

    public void updateValidators(TwinflowTransitionEntity dbTwinflowTransitionEntity, List<TwinflowTransitionValidatorEntity> validators) throws ServiceException {
        ChangesHelper changesHelper = new ChangesHelper();
        TwinflowTransitionValidatorEntity dbValidatorEntity;
        List<TwinflowTransitionValidatorEntity> saveList = new ArrayList<>();
        for (TwinflowTransitionValidatorEntity validator : validators) {
            changesHelper.flush();
            dbValidatorEntity = entitySmartService.findById(validator.getId(), twinflowTransitionValidatorRepository, EntitySmartService.FindMode.ifEmptyThrows);
            if (changesHelper.isChanged(TwinflowTransitionValidatorEntity.Fields.order, dbValidatorEntity.getOrder(), validator.getOrder()))
                dbValidatorEntity.setOrder(validator.getOrder());
            if (changesHelper.isChanged(TwinflowTransitionValidatorEntity.Fields.invert, dbValidatorEntity.isInvert(), validator.isInvert()))
                dbValidatorEntity.setInvert(validator.isInvert());
            if (changesHelper.isChanged(TwinflowTransitionValidatorEntity.Fields.twinValidatorFeaturerId, dbValidatorEntity.getTwinValidatorFeaturerId(), validator.getTwinValidatorFeaturerId()))
                dbValidatorEntity.setTwinValidatorFeaturerId(validator.getTwinValidatorFeaturerId());
            if (changesHelper.isChanged(TwinflowTransitionValidatorEntity.Fields.twinValidatorParams, dbValidatorEntity.getTwinValidatorParams(), validator.getTwinValidatorParams()))
                dbValidatorEntity.setTwinValidatorParams(validator.getTwinValidatorParams());
            if (changesHelper.isChanged(TwinflowTransitionValidatorEntity.Fields.isActive, dbValidatorEntity.isActive(), validator.isActive()))
                dbValidatorEntity.setActive(validator.isActive());
            if (changesHelper.hasChanges())
                saveList.add(dbValidatorEntity);
        }
        if (CollectionUtils.isEmpty(saveList))
            entitySmartService.saveAllAndLogChanges(saveList, twinflowTransitionValidatorRepository, changesHelper);
    }

    public void cudTriggers(TwinflowTransitionEntity dbTwinflowTransitionEntity, EntityCUD<TwinflowTransitionTriggerEntity> triggerCUD) throws ServiceException {
        if (triggerCUD == null)
            return;
        if (CollectionUtils.isNotEmpty(triggerCUD.getCreateList())) {
            createTriggers(dbTwinflowTransitionEntity, triggerCUD.getCreateList());
        }
        if (CollectionUtils.isNotEmpty(triggerCUD.getUpdateList())) {
            updateTriggers(dbTwinflowTransitionEntity, triggerCUD.getUpdateList());
        }
        if (CollectionUtils.isNotEmpty(triggerCUD.getDeleteUUIDList())) {
            deleteTriggers(dbTwinflowTransitionEntity, triggerCUD.getDeleteUUIDList());
        }
        evictCache(cacheManager, TwinflowTransitionTriggerRepository.CACHE_TRANSITION_TRIGGERS_BY_TRANSITION_ID_ORDERED, dbTwinflowTransitionEntity.getId());
    }

    public void deleteTriggers(TwinflowTransitionEntity dbTwinflowTransitionEntity, List<UUID> triggerDeleteUUIDList) throws ServiceException {
        Kit<TwinflowTransitionTriggerEntity, UUID> deleteEntityKit = new Kit<>(twinflowTransitionTriggerRepository.findAllByTwinflowTransitionIdAndIdIn(dbTwinflowTransitionEntity.getId(), triggerDeleteUUIDList), TwinflowTransitionTriggerEntity::getId);
        if (CollectionUtils.isEmpty(deleteEntityKit.getCollection()))
            return;
        for (UUID triggerUuid : triggerDeleteUUIDList) {
            TwinflowTransitionTriggerEntity trigger = deleteEntityKit.get(triggerUuid);
            if (null == trigger)
                throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "cant find transitionTrigger[" + triggerUuid + "] for delete operation");
            log.info(trigger.logDetailed() + " will be deleted");
        }
        twinflowTransitionTriggerRepository.deleteAllByTwinflowTransitionIdAndIdIn(dbTwinflowTransitionEntity.getId(), triggerDeleteUUIDList);
    }

    public List<TwinflowTransitionTriggerEntity> createTriggers(TwinflowTransitionEntity dbTwinflowTransitionEntity, List<TwinflowTransitionTriggerEntity> triggers) throws ServiceException {
        for (TwinflowTransitionTriggerEntity trigger : triggers)
            trigger.setTwinflowTransitionId(dbTwinflowTransitionEntity.getId());
        return IterableUtils.toList(entitySmartService.saveAllAndLog(triggers, twinflowTransitionTriggerRepository));
    }

    public void updateTriggers(TwinflowTransitionEntity dbTwinflowTransitionEntity, List<TwinflowTransitionTriggerEntity> triggers) throws ServiceException {
        ChangesHelper changesHelper = new ChangesHelper();
        TwinflowTransitionTriggerEntity dbTriggerEntity;
        List<TwinflowTransitionTriggerEntity> saveList = new ArrayList<>();
        for (TwinflowTransitionTriggerEntity trigger : triggers) {
            changesHelper.flush();
            dbTriggerEntity = entitySmartService.findById(trigger.getId(), twinflowTransitionTriggerRepository, EntitySmartService.FindMode.ifEmptyThrows);
            if (changesHelper.isChanged(TwinflowTransitionTriggerEntity.Fields.order, dbTriggerEntity.getOrder(), trigger.getOrder()))
                dbTriggerEntity.setOrder(trigger.getOrder());
            if (changesHelper.isChanged(TwinflowTransitionTriggerEntity.Fields.transitionTriggerFeaturerId, dbTriggerEntity.getTransitionTriggerFeaturerId(), trigger.getTransitionTriggerFeaturerId()))
                dbTriggerEntity.setTransitionTriggerFeaturerId(trigger.getTransitionTriggerFeaturerId());
            if (changesHelper.isChanged(TwinflowTransitionTriggerEntity.Fields.transitionTriggerParams, dbTriggerEntity.getTransitionTriggerParams(), trigger.getTransitionTriggerParams()))
                dbTriggerEntity.setTransitionTriggerParams(trigger.getTransitionTriggerParams());
            if (changesHelper.isChanged(TwinflowTransitionTriggerEntity.Fields.isActive, dbTriggerEntity.isActive(), trigger.isActive()))
                dbTriggerEntity.setActive(trigger.isActive());
            if (changesHelper.hasChanges())
                saveList.add(dbTriggerEntity);
        }
        if (CollectionUtils.isEmpty(saveList))
            entitySmartService.saveAllAndLogChanges(saveList, twinflowTransitionTriggerRepository, changesHelper);
    }

    public void updateTransitionAlias(TwinflowTransitionEntity dbTwinflowTransitionEntity, TwinflowTransitionAliasEntity twinflowTransitionAliasEntity, ChangesHelper changesHelper) throws ServiceException {
        if (twinflowTransitionAliasEntity.getAlias() == null)
            return;
        creatAliasIfNeeded(twinflowTransitionAliasEntity);
        if (!changesHelper.isChanged(TwinflowTransitionEntity.Fields.twinflowTransitionAliasId, dbTwinflowTransitionEntity.getTwinflowTransitionAliasId(), twinflowTransitionAliasEntity.getId()))
            return;
        dbTwinflowTransitionEntity.setTwinflowTransitionAliasId(twinflowTransitionAliasEntity.getId());
        dbTwinflowTransitionEntity.setTwinflowTransitionAlias(twinflowTransitionAliasEntity);
    }

    public void updateTransitionDescription(TwinflowTransitionEntity dbTwinflowTransitionEntity, I18nEntity descriptionI18n, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbTwinflowTransitionEntity.getDescriptionI18NId() != null)
            descriptionI18n.setId(dbTwinflowTransitionEntity.getDescriptionI18NId());
        i18nService.saveTranslations(I18nType.TWINFLOW_DESCRIPTION, descriptionI18n);
        if(changesHelper.isChanged(TwinflowTransitionEntity.Fields.descriptionI18NId, dbTwinflowTransitionEntity.getDescriptionI18NId(), descriptionI18n.getId()))
            dbTwinflowTransitionEntity.setDescriptionI18NId(descriptionI18n.getId());
    }


    public void updateTransitionName(TwinflowTransitionEntity dbTwinflowTransitionEntity, I18nEntity nameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbTwinflowTransitionEntity.getNameI18NId() != null)
            nameI18n.setId(dbTwinflowTransitionEntity.getNameI18NId());
        i18nService.saveTranslations(I18nType.TWINFLOW_NAME, nameI18n);
        if(changesHelper.isChanged(TwinflowTransitionEntity.Fields.nameI18NId, dbTwinflowTransitionEntity.getNameI18NId(), nameI18n.getId()))
            dbTwinflowTransitionEntity.setNameI18NId(nameI18n.getId());
    }

    public void updateTransitionInBuildFactory(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID factoryId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(TwinflowTransitionEntity.Fields.inbuiltTwinFactoryId, dbTwinflowTransitionEntity.getInbuiltTwinFactoryId(), factoryId))
            return;
        dbTwinflowTransitionEntity.setInbuiltTwinFactoryId(factoryId);
    }

    public void updateTransitionDraftingFactory(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID factoryId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(TwinflowTransitionEntity.Fields.draftingTwinFactoryId, dbTwinflowTransitionEntity.getDraftingTwinFactoryId(), factoryId))
            return;
        dbTwinflowTransitionEntity.setDraftingTwinFactoryId(factoryId);
    }

    public void updateTransitionPermission(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID permissionId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(TwinflowTransitionEntity.Fields.permissionId, dbTwinflowTransitionEntity.getPermissionId(), permissionId))
            return;
        dbTwinflowTransitionEntity.setPermissionId(permissionId);
    }

    public void updateTransitionSrcStatus(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID statusId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(TwinflowTransitionEntity.Fields.srcTwinStatusId, dbTwinflowTransitionEntity.getSrcTwinStatusId(), statusId))
            return;

        if(null != statusId && !UuidUtils.isNullifyMarker(statusId) && !twinClassService.isStatusAllowedForTwinClass(dbTwinflowTransitionEntity.getTwinflow().getTwinClass(), statusId))
            throw new ServiceException(ErrorCodeTwins.TRANSITION_STATUS_INCORRECT, "status[" + statusId + "] is not allowed for twinClass[" + dbTwinflowTransitionEntity.getTwinflow().getTwinClassId() + "]");
        dbTwinflowTransitionEntity.setSrcTwinStatusId(UuidUtils.nullifyIfNecessary(statusId));
    }

    public void updateTransitionDstStatus(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID statusId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(TwinflowTransitionEntity.Fields.dstTwinStatusId, dbTwinflowTransitionEntity.getSrcTwinStatusId(), statusId))
            return;
        if(null == statusId)
            throw new ServiceException(ErrorCodeTwins.TRANSITION_STATUS_INCORRECT, "Dst status for transition can't be null");
        if(!twinClassService.isStatusAllowedForTwinClass(dbTwinflowTransitionEntity.getTwinflow().getTwinClass(), statusId))
            throw new ServiceException(ErrorCodeTwins.TRANSITION_STATUS_INCORRECT, "status[" + statusId + "] is not allowed for twinClass[" + dbTwinflowTransitionEntity.getTwinflow().getTwinClassId() + "]");
        dbTwinflowTransitionEntity.setDstTwinStatusId(statusId);
    }


    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class TransitionDetectKey {
        final UUID twinflowId;
        final UUID srcStatusId;
        final UUID permissionSpaceId;
        final boolean isAssignee;
        final boolean isCreator;
        final UUID twinClassId;
    }


    public TransitionContext createTransitionContext(TwinEntity twinEntity, UUID transitionId) throws ServiceException {
        twinflowService.loadTwinflow(twinEntity);
        if (twinEntity.getTwinflow() == null)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Not twinflow can be detected for " + twinEntity.logDetailed());
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroups(apiUser);
        TwinflowTransitionEntity transition = twinflowTransitionRepository.findTransition(
                transitionId,
                apiUser.getDomainId(),
                TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                TypedParameterTwins.uuidNullable(twinEntity.getPermissionSchemaSpaceId()),
                apiUser.getUserId(),
                TypedParameterTwins.uuidArray(apiUser.getUserGroups()),
                TypedParameterTwins.uuidNullable(twinEntity.getTwinClassId()),
                TwinService.isAssignee(twinEntity, apiUser),
                TwinService.isCreator(twinEntity, apiUser));
        if (transition == null)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionId + "] can not be found or denied  for " + twinEntity.logDetailed());
        if (!transition.getTwinflowId().equals(twinEntity.getTwinflow().getId()))
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionId + "] can not be performed for " + twinEntity.logDetailed()
                    + ". Twinflow[" + twinEntity.getTwinflow().getId() + "] was detected for twin, but given transition is linked to twinflow[" + transition.getTwinflowId() + "]");
        if (!transition.getSrcTwinStatusId().equals(twinEntity.getTwinStatusId()))
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionId + "] can not be performed for " + twinEntity.logDetailed()
                    + ". Given transition is valid only from status[" + transition.getSrcTwinStatusId() + "]");
        TransitionContext transitionContext = new TransitionContext();
        transitionContext
                .setTransitionEntity(transition)
                .addTargetTwin(twinEntity);
        return transitionContext;
    }

    public TransitionContext createTransitionContext(Collection<TwinEntity> twinEntities, UUID transitionId) throws ServiceException {
        twinflowService.loadTwinflow(twinEntities);
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroups(apiUser);
        TwinflowTransitionEntity transition = null;
        Map<TransitionDetectKey, List<TwinEntity>> triples = convertToDetectKeys(twinEntities);
        TransitionDetectKey detectKey;
        for (Map.Entry<TransitionDetectKey, List<TwinEntity>> entry : triples.entrySet()) {
            detectKey = entry.getKey();
            transition = twinflowTransitionRepository.findTransition(
                    transitionId,
                    apiUser.getDomainId(),
                    TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                    TypedParameterTwins.uuidNullable(detectKey.permissionSpaceId),
                    apiUser.getUserId(),
                    TypedParameterTwins.uuidArray(apiUser.getUserGroups()),
                    TypedParameterTwins.uuidNullable(detectKey.twinClassId),
                    detectKey.isAssignee,
                    detectKey.isCreator);
            if (transition == null)
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionId + "] can not be found or denied for [" + entry.getValue().size() + "] twins");
            if (!transition.getTwinflowId().equals(entry.getKey().twinflowId))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionId + "] can not be performed for [" + entry.getValue().size() + "] twins."
                        + " Twinflow[" + entry.getKey().twinflowId + "] was detected for them, but given transition is linked to twinflow[" + transition.getTwinflowId() + "]");
            if (!transition.getSrcTwinStatusId().equals(entry.getKey().srcStatusId))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionId + "] can not be performed for [" + entry.getValue().size() + "] twins."
                        + ". Given transition is valid only from status[" + transition.getSrcTwinStatusId() + "]");
        }
        TransitionContext transitionContext = new TransitionContext();
        transitionContext
                .setTransitionEntity(transition)
                .addTargetTwins(twinEntities);
        return transitionContext;
    }

    public TransitionContext createTransitionContext(TwinEntity twinEntity, String transitionAlias) throws ServiceException {
        twinflowService.loadTwinflow(twinEntity);
        if (twinEntity.getTwinflow() == null)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Not twinflow can be detected for " + twinEntity.logDetailed());
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroups(apiUser);
        TwinflowTransitionEntity transition = twinflowTransitionRepository.findTransitionByAlias(
                twinEntity.getTwinflow().getId(),
                twinEntity.getTwinStatusId(),
                transitionAlias,
                apiUser.getDomainId(),
                TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                TypedParameterTwins.uuidNullable(twinEntity.getPermissionSchemaSpaceId()),
                apiUser.getUserId(),
                TypedParameterTwins.uuidArray(apiUser.getUserGroups()),
                TypedParameterTwins.uuidNullable(twinEntity.getTwinClassId()),
                TwinService.isAssignee(twinEntity, apiUser),
                TwinService.isCreator(twinEntity, apiUser)
        );
        if (transition == null)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Not transitions for alias[" + transitionAlias + "] can not be performed for " + twinEntity.logDetailed());
        TransitionContext transitionContext = new TransitionContext();
        transitionContext
                .setTransitionEntity(transition)
                .addTargetTwin(twinEntity);
        return transitionContext;
    }

    public Collection<TransitionContext> createTransitionContext(Collection<TwinEntity> twinEntities, String transitionAlias) throws ServiceException {
        twinflowService.loadTwinflow(twinEntities);
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroups(apiUser);
        TwinflowTransitionEntity transition = null;
        Map<UUID, TransitionContext> transitionContextMap = new HashMap<>();
        Map<TransitionDetectKey, List<TwinEntity>> triples = convertToDetectKeys(twinEntities);
        TransitionDetectKey detectKey;
        for (Map.Entry<TransitionDetectKey, List<TwinEntity>> entry : triples.entrySet()) {
            detectKey = entry.getKey();
            transition = twinflowTransitionRepository.findTransitionByAlias(
                    detectKey.twinflowId,
                    detectKey.srcStatusId,
                    transitionAlias,
                    apiUser.getDomainId(),
                    TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                    TypedParameterTwins.uuidNullable(detectKey.permissionSpaceId),
                    apiUser.getUserId(),
                    TypedParameterTwins.uuidArray(apiUser.getUserGroups()),
                    TypedParameterTwins.uuidNullable(detectKey.twinClassId),
                    detectKey.isAssignee,
                    detectKey.isCreator);
            if (transition == null)
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionAlias + "] can not be found or denied for [" + entry.getValue().size() + "] twins");
            if (!transition.getTwinflowId().equals(entry.getKey().twinflowId))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionAlias + "] can not be performed for [" + entry.getValue().size() + "] twins."
                        + " Twinflow[" + entry.getKey().twinflowId + "] was detected for them, but given transition is linked to twinflow[" + transition.getTwinflowId() + "]");
            if (transition.getSrcTwinStatusId() != null && !transition.getSrcTwinStatusId().equals(entry.getKey().srcStatusId))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionAlias + "] can not be performed for [" + entry.getValue().size() + "] twins."
                        + ". Given transition is valid only from status[" + transition.getSrcTwinStatusId() + "]");
            TransitionContext transitionContext = transitionContextMap.get(transition.getId());
            if (transitionContext == null) {
                transitionContext = new TransitionContext();
                transitionContext.setTransitionEntity(transition);
                transitionContextMap.put(transition.getId(), transitionContext);
            }
            for (TwinEntity twinEntity : entry.getValue())
                transitionContext.addTargetTwin(twinEntity);
        }
        return transitionContextMap.values();
    }

    public Kit<TwinflowTransitionValidatorEntity, UUID> loadValidators(TwinflowTransitionEntity transition) {
        if (transition.getValidatorsKit() != null)
            return transition.getValidatorsKit();
        List<TwinflowTransitionValidatorEntity> validators = twinflowTransitionValidatorRepository.findByTwinflowTransitionIdOrderByOrder(transition.getId());
        transition.setValidatorsKit(new Kit<>(validators, TwinflowTransitionValidatorEntity::getId));
        return transition.getValidatorsKit();
    }

    public void loadValidators(Collection<TwinflowTransitionEntity> transitions) {
        Map<UUID, TwinflowTransitionEntity> needLoad = new HashMap<>();
        for (TwinflowTransitionEntity transition : transitions)
            if (transition.getValidatorsKit() == null)
                needLoad.put(transition.getId(), transition);
        if (needLoad.isEmpty()) return;
        KitGrouped<TwinflowTransitionValidatorEntity, UUID, UUID> validatorsKit = new KitGrouped<>(
                twinflowTransitionValidatorRepository.findAllByTwinflowTransitionIdInOrderByOrder(needLoad.keySet()), TwinflowTransitionValidatorEntity::getId, TwinflowTransitionValidatorEntity::getTwinflowTransitionId);
        for (Map.Entry<UUID, TwinflowTransitionEntity> entry : needLoad.entrySet())
            entry.getValue().setValidatorsKit(new Kit<>(validatorsKit.getGrouped(entry.getKey()), TwinflowTransitionValidatorEntity::getId));
    }

    public Kit<TwinflowTransitionTriggerEntity, UUID> loadTriggers(TwinflowTransitionEntity transition) {
        if (transition.getTriggersKit() != null)
            return transition.getTriggersKit();
        List<TwinflowTransitionTriggerEntity> triggers = twinflowTransitionTriggerRepository.findByTwinflowTransitionIdOrderByOrder(transition.getId());
        transition.setTriggersKit(new Kit<>(triggers, TwinflowTransitionTriggerEntity::getId));
        return transition.getTriggersKit();
    }

    public void loadTriggers(Collection<TwinflowTransitionEntity> transitions) {
        Map<UUID, TwinflowTransitionEntity> needLoad = new HashMap<>();
        for (TwinflowTransitionEntity transition : transitions)
            if (transition.getTriggersKit() == null)
                needLoad.put(transition.getId(), transition);
        if (needLoad.isEmpty()) return;
        KitGrouped<TwinflowTransitionTriggerEntity, UUID, UUID> triggersKit = new KitGrouped<>(
                twinflowTransitionTriggerRepository.findAllByTwinflowTransitionIdInOrderByOrder(needLoad.keySet()), TwinflowTransitionTriggerEntity::getId, TwinflowTransitionTriggerEntity::getTwinflowTransitionId);
        for (Map.Entry<UUID, TwinflowTransitionEntity> entry : needLoad.entrySet())
            entry.getValue().setTriggersKit(new Kit<>(triggersKit.getGrouped(entry.getKey()), TwinflowTransitionTriggerEntity::getId));
    }

    public void validateTransition(TransitionContext transitionContext) throws ServiceException {
        List<TwinflowTransitionValidatorEntity> transitionValidatorEntityList = twinflowTransitionValidatorRepository.findByTwinflowTransitionIdOrderByOrder(transitionContext.getTransitionEntity().getId());
        for (TwinEntity twinEntity : transitionContext.getTargetTwinList().values())
            if (!runTransitionValidators(transitionContext.getTransitionEntity(), transitionValidatorEntityList, twinEntity))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_DENIED);
    }

    public boolean runTransitionValidators(TwinflowTransitionEntity twinflowTransitionEntity, TwinEntity twinEntity) throws ServiceException {
        // findByTwinflowTransitionIdOrderByOrder method result must be cached to avoid extra query count (in case of loading for list of twins)
        // if cache will be disabled - validator must be loaded in one query
        List<TwinflowTransitionValidatorEntity> transitionValidatorEntityList = twinflowTransitionValidatorRepository.findByTwinflowTransitionIdOrderByOrder(twinflowTransitionEntity.getId());
        return runTransitionValidators(twinflowTransitionEntity, transitionValidatorEntityList, twinEntity);
    }

    public boolean runTransitionValidators(TwinflowTransitionEntity twinflowTransitionEntity, List<TwinflowTransitionValidatorEntity> transitionValidatorEntityList, TwinEntity twinEntity) throws ServiceException {
        for (TwinflowTransitionValidatorEntity transitionValidatorEntity : transitionValidatorEntityList) {
            if (!transitionValidatorEntity.isActive()) {
                log.info(twinflowTransitionEntity.easyLog(EasyLoggable.Level.NORMAL) + " will not be used, since it is inactive. ");
                return true;
            }

            TwinValidator transitionValidator = featurerService.getFeaturer(transitionValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
            TwinValidator.ValidationResult validationResult = transitionValidator.isValid(transitionValidatorEntity.getTwinValidatorParams(), twinEntity, transitionValidatorEntity.isInvert());
            if (!validationResult.isValid()) {
                log.info(twinflowTransitionEntity.easyLog(EasyLoggable.Level.NORMAL) + " is not valid. " + validationResult.getMessage());
                return false;
            }
        }
        return true;
    }

    @Transactional
    public TransitionResult performTransition(TransitionContext transitionContext) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        validateTransition(transitionContext);
        TransitionResult ret = new TransitionResult();
        if (transitionContext.getAttachmentCUD() != null && CollectionUtils.isNotEmpty(transitionContext.getAttachmentCUD().getCreateList())) {
            transitionContext.getAttachmentCUD().getCreateList().forEach(a -> a
                    .setTwinflowTransitionId(transitionContext.getTransitionEntity().getId())
                    .setTwinflowTransition(transitionContext.getTransitionEntity()));
        }
        UUID inbuiltFactoryId = transitionContext.getTransitionEntity().getInbuiltTwinFactoryId();
        if (inbuiltFactoryId != null) {
            FactoryBranchId factoryBranchId = FactoryBranchId.root(inbuiltFactoryId);
            FactoryContext factoryContext = new FactoryContext(factoryBranchId)
                    .setInputTwinList(transitionContext.getTargetTwinList().values())
                    .setFields(transitionContext.getFields())
                    .setAttachmentCUD(transitionContext.getAttachmentCUD())
                    .setBasics(transitionContext.getBasics());
            if (CollectionUtils.isNotEmpty(transitionContext.getNewTwinList())) { //new twins must be added to factory content for having possibility to run pipelines for them
                for (TwinCreate twinCreate : transitionContext.getNewTwinList()) {
                    factoryContext.add(new FactoryItem()
                            .setOutput(twinCreate)
                            .setFactoryContext(factoryContext));
//                            .setContextFactoryItemList(transitionContext.getTargetTwinList().values().stream().toList())); //fixme
                }
            }
            LoggerUtils.traceTreeStart();
            List<TwinOperation> twinFactoryOutput;
            try {
                twinFactoryOutput = twinFactoryService.runFactory(transitionContext.getTransitionEntity().getInbuiltTwinFactoryId(), factoryContext);
            } finally {
                LoggerUtils.traceTreeEnd();
            }
            for (TwinOperation twinOperation : twinFactoryOutput) {
                if (twinOperation instanceof TwinCreate twinCreate) {
                    TwinService.TwinCreateResult twinCreateResult = twinService.createTwin(apiUser, twinCreate);
                    ret.addProcessedTwin(twinCreateResult.getCreatedTwin());
                } else if (twinOperation instanceof TwinUpdate twinUpdate) {
                    boolean isProcessedTwin = true;
                    if (transitionContext.getTargetTwinList() != null && transitionContext.getTargetTwinList().containsKey(twinUpdate.getTwinEntity().getId())) {// case when twin was taken from input, we have to force update status from transition
                        if (twinUpdate.getTwinEntity().getTwinStatusId() == null || twinUpdate.getDbTwinEntity().getTwinStatusId().equals(twinUpdate.getTwinEntity().getTwinStatusId()))
                            twinUpdate.getTwinEntity()
                                    .setTwinStatusId(transitionContext.getTransitionEntity().getDstTwinStatusId())
                                    .setTwinStatus(transitionContext.getTransitionEntity().getDstTwinStatus());
                        isProcessedTwin = false;
                    }
                    twinService.updateTwin(twinUpdate);
                    if (isProcessedTwin) {
                        ret.addProcessedTwin(twinUpdate.getDbTwinEntity());
                    } else
                        ret.addTransitionedTwin(twinUpdate.getDbTwinEntity());
                }
            }
        } else {
            twinService.changeStatus(transitionContext.getTargetTwinList().values(), transitionContext.getTransitionEntity().getDstTwinStatus());
            ret.setTransitionedTwinList(transitionContext.getTargetTwinList().values().stream().toList());
        }
        runTriggers(transitionContext);
        return ret;
    }

    @Transactional
    public void runTriggers(TransitionContext transitionContext) throws ServiceException {
        TwinflowTransitionEntity transitionEntity = transitionContext.getTransitionEntity();
        List<TwinflowTransitionTriggerEntity> transitionTriggerEntityList = twinflowTransitionTriggerRepository.findByTwinflowTransitionIdOrderByOrder(transitionEntity.getId());
        //todo run status input/output triggers
        for (TwinEntity targetTwin : transitionContext.getTargetTwinList().values())
            for (TwinflowTransitionTriggerEntity triggerEntity : transitionTriggerEntityList) {
                if (!triggerEntity.isActive()) {
                    log.info(triggerEntity.easyLog(EasyLoggable.Level.DETAILED) + " will not be triggered, since it is inactive");
                    continue;
                }

                log.info(triggerEntity.easyLog(EasyLoggable.Level.DETAILED) + " will be triggered");
                TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturer(), TransitionTrigger.class);
                transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), targetTwin, transitionEntity.getSrcTwinStatus(), transitionEntity.getDstTwinStatus());
            }
    }

    @Data
    @Accessors(chain = true)
    public static class TransitionResult {
        private List<TwinEntity> transitionedTwinList;
        private List<TwinEntity> processedTwinList;

        public TransitionResult addTransitionedTwin(TwinEntity twinEntity) {
            transitionedTwinList = CollectionUtils.safeAdd(transitionedTwinList, twinEntity);
            return this;
        }

        public TransitionResult addTransitionedTwin(List<TwinEntity> twinEntityList) {
            if (CollectionUtils.isEmpty(twinEntityList))
                return this;
            if (transitionedTwinList == null)
                transitionedTwinList = new ArrayList<>();
            transitionedTwinList.addAll(twinEntityList);
            return this;
        }

        public TransitionResult addProcessedTwin(TwinEntity twinEntity) {
            processedTwinList = CollectionUtils.safeAdd(processedTwinList, twinEntity);
            return this;
        }

        public TransitionResult addProcessedTwin(List<TwinEntity> twinEntityList) {
            if (CollectionUtils.isEmpty(twinEntityList))
                return this;
            if (processedTwinList == null)
                processedTwinList = new ArrayList<>();
            processedTwinList.addAll(twinEntityList);
            return this;
        }
    }

//    @Transactional
//    public void performTransition(TwinflowTransitionEntity transitionEntity, List<TwinUpdate> twinUpdateBatch) throws ServiceException {
//        for (TwinUpdate twinUpdate : twinUpdateBatch) {
//            performTransition(transitionEntity, twinUpdate);
//        }
//    }

//    @Transactional
//    public void performTransition(TwinflowTransitionEntity transitionEntity, TwinEntity twinEntity) throws ServiceException {
//        if (!validateTransition(transitionEntity, twinEntity))
//            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT);
//        twinService.changeStatus(twinEntity, transitionEntity.getDstTwinStatus());
//
//        List<TwinflowTransitionTriggerEntity> transitionTriggerEntityList = twinflowTransitionTriggerRepository.findByTwinflowTransitionIdOrderByOrder(transitionEntity.getId());
//        for (TwinflowTransitionTriggerEntity triggerEntity : transitionTriggerEntityList) {
//            log.info(triggerEntity.easyLog(EasyLoggable.Level.DETAILED) + " will be triggered");
//            TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturer(), TransitionTrigger.class);
//            transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), twinEntity, transitionEntity.getSrcTwinStatus(), transitionEntity.getDstTwinStatus());
//        }
//    }
}

