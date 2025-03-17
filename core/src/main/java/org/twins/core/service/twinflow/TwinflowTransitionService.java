package org.twins.core.service.twinflow;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nType;
import org.twins.core.service.i18n.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.draft.DraftStatus;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinflow.*;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.domain.factory.*;
import org.twins.core.domain.search.TransitionAliasSearch;
import org.twins.core.domain.search.TransitionSearch;
import org.twins.core.domain.transition.*;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.draft.DraftCommitService;
import org.twins.core.service.draft.DraftService;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserGroupService;
import org.twins.core.service.user.UserService;

import java.util.*;
import java.util.function.Function;

import static org.cambium.common.util.CacheUtils.evictCache;
import static org.cambium.common.util.RowUtils.mapUuidInt;
import static org.twins.core.dao.specifications.twinflow.TransitionAliasSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionService extends EntitySecureFindServiceImpl<TwinflowTransitionEntity> {
    private final TwinflowTransitionRepository twinflowTransitionRepository;
    private final TwinflowTransitionValidatorRuleRepository twinflowTransitionValidatorRuleRepository;
    private final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;
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
    @Lazy
    private final DraftService draftService;
    @Lazy
    private final DraftCommitService draftCommitService;
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
    public Function<TwinflowTransitionEntity, UUID> entityGetIdFunction() {
        return TwinflowTransitionEntity::getId;
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

    public void loadValidTransitions(TwinEntity twinEntity) throws ServiceException {
        loadValidTransitions(Collections.singleton(twinEntity));
    }

    public void loadValidTransitions(Collection<TwinEntity> twinEntityList) throws ServiceException {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList) {
            if (twinEntity.getTwinClass().getOwnerType().equals(TwinClassEntity.OwnerType.SYSTEM)) //no transitions available for such twins, because they are cross-domain
                continue;
            if (twinEntity.getValidTransitionsKit() != null)
                continue;
            needLoad.put(twinEntity.getId(), twinEntity);
        }
        if (MapUtils.isEmpty(needLoad))
            return;
        userGroupService.loadGroupsForCurrentUser();
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
                    TypedParameterTwins.uuidArray(apiUser.getUser().getUserGroups().getIdSetSafe()),
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
    public TwinflowTransitionEntity updateTwinflowTransition(TwinflowTransitionEntity twinflowTransitionEntity, I18nEntity nameI18n, I18nEntity descriptionI18n, EntityCUD<TwinflowTransitionValidatorRuleEntity> validatorCUD, EntityCUD<TwinflowTransitionTriggerEntity> triggerCUD) throws ServiceException {
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

        dbTwinflowTransitionEntity = updateSafe(dbTwinflowTransitionEntity, changesHelper);
        if (changesHelper.hasChanges()) {
            evictCache(cacheManager, TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, dbTwinflowTransitionEntity.getTwinflow().getTwinClassId());
        }
        return dbTwinflowTransitionEntity;
    }

    public void cudValidators(TwinflowTransitionEntity dbTwinflowTransitionEntity, EntityCUD<TwinflowTransitionValidatorRuleEntity> validatorCUD) throws ServiceException {
        if (validatorCUD == null)
            return;
        if (CollectionUtils.isNotEmpty(validatorCUD.getCreateList())) {
            createValidators(dbTwinflowTransitionEntity, validatorCUD.getCreateList());
        }
        if (CollectionUtils.isNotEmpty(validatorCUD.getUpdateList())) {
            updateValidators(dbTwinflowTransitionEntity, validatorCUD.getUpdateList());
        }
        if (CollectionUtils.isNotEmpty(validatorCUD.getDeleteList())) {
            deleteValidators(dbTwinflowTransitionEntity, validatorCUD.getDeleteList());
        }
        evictCache(cacheManager, TwinflowTransitionValidatorRuleRepository.CACHE_TRANSITION_VALIDATOR_RULES_BY_TRANSITION_ID_ORDERED, dbTwinflowTransitionEntity.getId());
    }

    @Transactional
    public void deleteValidators(TwinflowTransitionEntity dbTwinflowTransitionEntity, List<TwinflowTransitionValidatorRuleEntity> validatorDeleteList) throws ServiceException {
        entitySmartService.deleteAllEntitiesAndLog(validatorDeleteList, twinflowTransitionValidatorRuleRepository);
    }

    public List<TwinflowTransitionValidatorRuleEntity> createValidators(TwinflowTransitionEntity dbTwinflowTransitionEntity, List<TwinflowTransitionValidatorRuleEntity> validators) throws ServiceException {
        for (TwinflowTransitionValidatorRuleEntity validator : validators)
            validator.setTwinflowTransitionId(dbTwinflowTransitionEntity.getId());
        return IterableUtils.toList(entitySmartService.saveAllAndLog(validators, twinflowTransitionValidatorRuleRepository));
    }

    //TODO support new logic with sets for CUD validator
    public void updateValidators(TwinflowTransitionEntity dbTwinflowTransitionEntity, List<TwinflowTransitionValidatorRuleEntity> validators) throws ServiceException {
//        ChangesHelper changesHelper = new ChangesHelper();
//        TwinflowTransitionValidatorEntity dbValidatorEntity;
//        List<TwinflowTransitionValidatorEntity> saveList = new ArrayList<>();
//        for (TwinflowTransitionValidatorEntity validator : validators) {
//            changesHelper.flush();
//            dbValidatorEntity = entitySmartService.findById(validator.getId(), twinflowTransitionValidatorRepository, EntitySmartService.FindMode.ifEmptyThrows);
//            if (changesHelper.isChanged(TwinflowTransitionValidatorEntity.Fields.order, dbValidatorEntity.getOrder(), validator.getOrder()))
//                dbValidatorEntity.setOrder(validator.getOrder());
//            if (changesHelper.isChanged(TwinflowTransitionValidatorEntity.Fields.invert, dbValidatorEntity.isInvert(), validator.isInvert()))
//                dbValidatorEntity.setInvert(validator.isInvert());
//            if (changesHelper.isChanged(TwinflowTransitionValidatorEntity.Fields.twinValidatorFeaturerId, dbValidatorEntity.getTwinValidatorFeaturerId(), validator.getTwinValidatorFeaturerId()))
//                dbValidatorEntity.setTwinValidatorFeaturerId(validator.getTwinValidatorFeaturerId());
//            if (changesHelper.isChanged(TwinflowTransitionValidatorEntity.Fields.twinValidatorParams, dbValidatorEntity.getTwinValidatorParams(), validator.getTwinValidatorParams()))
//                dbValidatorEntity.setTwinValidatorParams(validator.getTwinValidatorParams());
//            if (changesHelper.isChanged(TwinflowTransitionValidatorEntity.Fields.isActive, dbValidatorEntity.isActive(), validator.isActive()))
//                dbValidatorEntity.setActive(validator.isActive());
//            if (changesHelper.hasChanges())
//                saveList.add(dbValidatorEntity);
//        }
//        if (!CollectionUtils.isEmpty(saveList))
//            entitySmartService.saveAllAndLogChanges(saveList, twinflowTransitionValidatorRepository, changesHelper);
    }

    @Transactional
    public void cudTriggers(TwinflowTransitionEntity dbTwinflowTransitionEntity, EntityCUD<TwinflowTransitionTriggerEntity> triggerCUD) throws ServiceException {
        if (triggerCUD == null)
            return;
        if (CollectionUtils.isNotEmpty(triggerCUD.getCreateList())) {
            createTriggers(dbTwinflowTransitionEntity, triggerCUD.getCreateList());
        }
        if (CollectionUtils.isNotEmpty(triggerCUD.getUpdateList())) {
            updateTriggers(dbTwinflowTransitionEntity, triggerCUD.getUpdateList());
        }
        if (CollectionUtils.isNotEmpty(triggerCUD.getDeleteList())) {
            deleteTriggers(dbTwinflowTransitionEntity, triggerCUD.getDeleteList());
        }
        evictCache(cacheManager, TwinflowTransitionTriggerRepository.CACHE_TRANSITION_TRIGGERS_BY_TRANSITION_ID_ORDERED, dbTwinflowTransitionEntity.getId());
    }

    @Transactional
    public void deleteTriggers(TwinflowTransitionEntity dbTwinflowTransitionEntity, List<TwinflowTransitionTriggerEntity> triggerDeleteList) throws ServiceException {
        entitySmartService.deleteAllEntitiesAndLog(triggerDeleteList, twinflowTransitionTriggerRepository);
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
        if (!CollectionUtils.isEmpty(saveList))
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
        if (changesHelper.isChanged(TwinflowTransitionEntity.Fields.descriptionI18NId, dbTwinflowTransitionEntity.getDescriptionI18NId(), descriptionI18n.getId()))
            dbTwinflowTransitionEntity.setDescriptionI18NId(descriptionI18n.getId());
    }


    public void updateTransitionName(TwinflowTransitionEntity dbTwinflowTransitionEntity, I18nEntity nameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbTwinflowTransitionEntity.getNameI18NId() != null)
            nameI18n.setId(dbTwinflowTransitionEntity.getNameI18NId());
        i18nService.saveTranslations(I18nType.TWINFLOW_NAME, nameI18n);
        if (changesHelper.isChanged(TwinflowTransitionEntity.Fields.nameI18NId, dbTwinflowTransitionEntity.getNameI18NId(), nameI18n.getId()))
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
        if (null != statusId && !UuidUtils.isNullifyMarker(statusId) && !twinClassService.isStatusAllowedForTwinClass(dbTwinflowTransitionEntity.getTwinflow().getTwinClass(), statusId))
            throw new ServiceException(ErrorCodeTwins.TRANSITION_STATUS_INCORRECT, "status[" + statusId + "] is not allowed for twinClass[" + dbTwinflowTransitionEntity.getTwinflow().getTwinClassId() + "]");
        dbTwinflowTransitionEntity.setSrcTwinStatusId(UuidUtils.nullifyIfNecessary(statusId));
    }

    public void updateTransitionDstStatus(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID statusId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(TwinflowTransitionEntity.Fields.dstTwinStatusId, dbTwinflowTransitionEntity.getSrcTwinStatusId(), statusId))
            return;
        if (null == statusId)
            throw new ServiceException(ErrorCodeTwins.TRANSITION_STATUS_INCORRECT, "Dst status for transition can't be null");
        if (!twinClassService.isStatusAllowedForTwinClass(dbTwinflowTransitionEntity.getTwinflow().getTwinClass(), statusId))
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
        userGroupService.loadGroupsForCurrentUser();
        TwinflowTransitionEntity transition = twinflowTransitionRepository.findTransition(
                transitionId,
                apiUser.getDomainId(),
                TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                TypedParameterTwins.uuidNullable(twinEntity.getPermissionSchemaSpaceId()),
                apiUser.getUserId(),
                TypedParameterTwins.uuidArray(apiUser.getUser().getUserGroups().getIdSetSafe()),
                TypedParameterTwins.uuidNullable(twinEntity.getTwinClassId()),
                TwinService.isAssignee(twinEntity, apiUser),
                TwinService.isCreator(twinEntity, apiUser));
        if (transition == null)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionId + "] can not be found or denied  for " + twinEntity.logDetailed());
        if (!transition.getTwinflowId().equals(twinEntity.getTwinflow().getId()))
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT, "Transition[" + transitionId + "] can not be performed for " + twinEntity.logDetailed()
                    + ". Twinflow[" + twinEntity.getTwinflow().getId() + "] was detected for twin, but given transition is linked to twinflow[" + transition.getTwinflowId() + "]");
        if (transition.getSrcTwinStatusId() != null && !twinEntity.getTwinStatusId().equals(transition.getSrcTwinStatusId()))
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
        userGroupService.loadGroupsForCurrentUser();
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
                    TypedParameterTwins.uuidArray(apiUser.getUser().getUserGroups().getIdSetSafe()),
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
        userGroupService.loadGroupsForCurrentUser();
        TwinflowTransitionEntity transition = twinflowTransitionRepository.findTransitionByAlias(
                twinEntity.getTwinflow().getId(),
                twinEntity.getTwinStatusId(),
                transitionAlias,
                apiUser.getDomainId(),
                TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                TypedParameterTwins.uuidNullable(twinEntity.getPermissionSchemaSpaceId()),
                apiUser.getUserId(),
                TypedParameterTwins.uuidArray(apiUser.getUser().getUserGroups().getIdSetSafe()),
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

    public TransitionContextBatch createTransitionContext(Collection<TwinEntity> twinEntities, String transitionAlias) throws ServiceException {
        twinflowService.loadTwinflow(twinEntities);
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroupsForCurrentUser();
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
                    TypedParameterTwins.uuidArray(apiUser.getUser().getUserGroups().getIdSetSafe()),
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
        return new TransitionContextBatch(transitionContextMap.values());
    }

    public Kit<TwinflowTransitionValidatorRuleEntity, UUID> loadValidators(TwinflowTransitionEntity transition) {
        if (transition.getValidatorRulesKit() != null)
            return transition.getValidatorRulesKit();
        List<TwinflowTransitionValidatorRuleEntity> validators = twinflowTransitionValidatorRuleRepository.findByTwinflowTransitionIdOrderByOrder(transition.getId());
        transition.setValidatorRulesKit(new Kit<>(validators, TwinflowTransitionValidatorRuleEntity::getId));
        return transition.getValidatorRulesKit();
    }

    public void loadValidators(Collection<TwinflowTransitionEntity> transitions) {
        Map<UUID, TwinflowTransitionEntity> needLoad = new HashMap<>();
        for (TwinflowTransitionEntity transition : transitions)
            if (transition.getValidatorRulesKit() == null)
                needLoad.put(transition.getId(), transition);
        if (needLoad.isEmpty()) return;
        KitGrouped<TwinflowTransitionValidatorRuleEntity, UUID, UUID> validatorsKit = new KitGrouped<>(
                twinflowTransitionValidatorRuleRepository.findAllByTwinflowTransitionIdInOrderByOrder(needLoad.keySet()), TwinflowTransitionValidatorRuleEntity::getId, TwinflowTransitionValidatorRuleEntity::getTwinflowTransitionId);
        for (Map.Entry<UUID, TwinflowTransitionEntity> entry : needLoad.entrySet())
            entry.getValue().setValidatorRulesKit(new Kit<>(validatorsKit.getGrouped(entry.getKey()), TwinflowTransitionValidatorRuleEntity::getId));
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
        if (transitionContext.isValidated())
            return;
        List<TwinflowTransitionValidatorRuleEntity> transitionValidatorEntityList = twinflowTransitionValidatorRuleRepository.findByTwinflowTransitionIdOrderByOrder(transitionContext.getTransitionEntity().getId());
        for (TwinEntity twinEntity : transitionContext.getTargetTwinList().values())
            if (!runTransitionValidators(transitionContext.getTransitionEntity(), transitionValidatorEntityList, twinEntity))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_DENIED);
        transitionContext.setValidated(true);
    }

    public boolean runTransitionValidators(TwinflowTransitionEntity twinflowTransitionEntity, TwinEntity twinEntity) throws ServiceException {
        // findByTwinflowTransitionIdOrderByOrder method result must be cached to avoid extra query count (in case of loading for list of twins)
        // if cache will be disabled - validator must be loaded in one query
        List<TwinflowTransitionValidatorRuleEntity> transitionValidatorEntityList = twinflowTransitionValidatorRuleRepository.findByTwinflowTransitionIdOrderByOrder(twinflowTransitionEntity.getId());
        return runTransitionValidators(twinflowTransitionEntity, transitionValidatorEntityList, twinEntity);
    }

    //todo optimize for collection processing
    public boolean runTransitionValidators(TwinflowTransitionEntity twinflowTransitionEntity, List<TwinflowTransitionValidatorRuleEntity> transitionValidatorEntityList, TwinEntity twinEntity) throws ServiceException {
        boolean validationResultOfRule = true;
        for (TwinflowTransitionValidatorRuleEntity transitionValidatorRuleEntity : transitionValidatorEntityList) {
            validationResultOfRule = true;
            if (!transitionValidatorRuleEntity.isActive()) {
                log.info(transitionValidatorRuleEntity.easyLog(EasyLoggable.Level.NORMAL) + " will not be used, since it is inactive. ");
                continue;
            }
            List<TwinValidatorEntity> sortedTwinValidators = new ArrayList<>(transitionValidatorRuleEntity.getTwinValidators());
            sortedTwinValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));
            for (TwinValidatorEntity twinValidatorEntity : sortedTwinValidators) {
                if (!twinValidatorEntity.isActive()) {
                    log.info(twinValidatorEntity.easyLog(EasyLoggable.Level.NORMAL) + " from " + transitionValidatorRuleEntity.easyLog(EasyLoggable.Level.NORMAL) + " will not be used, since it is inactive. ");
                    continue;
                }

                TwinValidator transitionValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
                TwinValidator.ValidationResult validationResult = transitionValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinEntity, twinValidatorEntity.isInvert());
                validationResultOfRule = validationResult.isValid();
                if (!validationResultOfRule) {
                    log.info(twinValidatorEntity.easyLog(EasyLoggable.Level.NORMAL) + " from " + transitionValidatorRuleEntity.easyLog(EasyLoggable.Level.NORMAL) + " is not valid. " + validationResult.getMessage());
                    break;
                }
            }
        }
        return validationResultOfRule;
    }

    public DraftEntity draftTransition(TransitionContext transitionContext) throws ServiceException {
        return draftTransitions(new TransitionContextBatch(List.of(transitionContext)));
    }

    public DraftEntity draftTransitions(TransitionContextBatch transitionContextBatch) throws ServiceException {
        for (TransitionContext transitionContext : transitionContextBatch.getAll()) {
            validateTransition(transitionContext);
            fillAttachmentsTransition(transitionContext);
        }
        runFactories(transitionContextBatch);
        DraftCollector draftCollector = draftService.beginDraft();
        try {
            draftService.draftFactoryResult(draftCollector, transitionContextBatch.getFactoried().values());
            //simple transitions also must be drafted here
            TwinUpdate twinUpdate;
            for (TransitionContext transitionContext : transitionContextBatch.getSimple()) {
                for (TwinEntity twinEntity : transitionContext.getTargetTwinList().values()) {
                    twinUpdate = new TwinUpdate();
                    twinUpdate
                            .setDbTwinEntity(twinEntity)
                            .setTwinEntity(new TwinEntity()
                                    .setId(twinEntity.getId())
                                    .setTwinStatusId(transitionContext.getTransitionEntity().getDstTwinStatusId())
                                    .setTwinStatus(transitionContext.getTransitionEntity().getDstTwinStatus()));
                    draftService.draftTwinUpdate(draftCollector, twinUpdate);
                }
            }
            draftService.endDraft(draftCollector);
        } catch (ServiceException e) {
            draftCollector.getDraftEntity()
                    .setStatus(DraftStatus.CONSTRUCTION_EXCEPTION)
                    .setStatusDetails(e.log());
            draftService.endDraft(draftCollector);
            throw e;
        }
        return draftCollector.getDraftEntity();
    }

    public TransitionResult performTransition(TransitionContext transitionContext) throws ServiceException {
        return performTransitions(new TransitionContextBatch(List.of(transitionContext)));
    }

    public TransitionResult performTransitions(TransitionContextBatch transitionContextBatch) throws ServiceException {
        for (TransitionContext transitionContext : transitionContextBatch.getAll()) {
            validateTransition(transitionContext);
            fillAttachmentsTransition(transitionContext);
        }
        runFactories(transitionContextBatch);
        TransitionResult transitionResult = null;
        if (transitionContextBatch.isMustBeDrafted()) { // we will go to drafting
            transitionResult = storeMajorTransition(transitionContextBatch);
        } else {
            transitionResult = storeMinorTransitions(transitionContextBatch);
        }
        for (TransitionContext transitionContext : transitionContextBatch.getAll()) {
            runTriggers(transitionContext);
        }
        return transitionResult;
    }

    private TransitionResult storeMajorTransition(TransitionContextBatch transitionContextBatch) throws ServiceException {
        DraftEntity draftEntity = draftTransitions(transitionContextBatch);
        draftCommitService.commitNowOrInQueue(draftEntity);
        TransitionResultMajor transitionResultMajor = new TransitionResultMajor();
        transitionResultMajor.setCommitedDraftEntity(draftEntity);
        return transitionResultMajor;
    }

    public TransitionResult storeMinorTransitions(TransitionContextBatch transitionContextBatch) throws ServiceException {
        TransitionResultMinor transitionResultMinor = new TransitionResultMinor();
        for (TransitionContext transitionContext : transitionContextBatch.getSimple()) {
            twinService.changeStatus(transitionContext.getTargetTwinList().values(), transitionContext.getTransitionEntity().getDstTwinStatus());
            transitionResultMinor.setTransitionedTwinList(transitionContext.getTargetTwinList().values().stream().toList());
        }
        commitFactoriesResult(transitionContextBatch.getFactoried(), transitionResultMinor); // without "drafting" we can get only minor results
        return transitionResultMinor;
    }

    public void runFactories(TransitionContextBatch transitionContextBatch) throws ServiceException {
        for (Map.Entry<TransitionContext, FactoryResultUncommited> entry : transitionContextBatch.getFactoried().entrySet()) {
            if (entry.getValue() != null) //factory is already run
                continue;
            FactoryResultUncommited factoryResultUncommited = runTransitionFactory(entry.getKey());
            entry.setValue(factoryResultUncommited); //filling result
            if (twinFactoryService.mustBeDrafted(factoryResultUncommited))
                transitionContextBatch.setMustBeDrafted(true); //this is batch decision for all results
        }
    }

    private FactoryResultUncommited runTransitionFactory(TransitionContext transitionContext) throws ServiceException {
        UUID inbuiltTwinFactoryId = transitionContext.getTransitionEntity().getInbuiltTwinFactoryId();
        FactoryBranchId factoryBranchId = FactoryBranchId.root(inbuiltTwinFactoryId);
        FactoryContext factoryContext = new FactoryContext(FactoryLauncher.transition, factoryBranchId)
                .setInputTwinList(transitionContext.getTargetTwinList().values())
                .setFields(transitionContext.getFields())
                .setAttachmentCUD(transitionContext.getAttachmentCUD())
                .setBasics(transitionContext.getBasics());
        if (CollectionUtils.isNotEmpty(transitionContext.getNewTwinList())) { //new twins must be added to factory content for having possibility to run pipelines for them
            for (TwinCreate twinCreate : transitionContext.getNewTwinList()) {
                factoryContext.getFactoryItemList().add(new FactoryItem()
                        .setOutput(twinCreate)
                        .setFactoryContext(factoryContext));
//                            .setContextFactoryItemList(transitionContext.getTargetTwinList().values().stream().toList())); //fixme
            }
        }
        LoggerUtils.traceTreeStart();
        FactoryResultUncommited factoryResultUncommited;
        try {
            factoryResultUncommited = twinFactoryService.runFactoryAndCollectResult(inbuiltTwinFactoryId, factoryContext);
        } finally {
            LoggerUtils.traceTreeEnd();
        }
        transitionToDstStatus(transitionContext, factoryResultUncommited);
        return factoryResultUncommited;
    }

    private static void fillAttachmentsTransition(TransitionContext transitionContext) {
        if (transitionContext.isAttachmentsFilled())
            return;
        if (transitionContext.getAttachmentCUD() != null && CollectionUtils.isNotEmpty(transitionContext.getAttachmentCUD().getCreateList())) {
            transitionContext.getAttachmentCUD().getCreateList().forEach(a -> a
                    .setTwinflowTransitionId(transitionContext.getTransitionEntity().getId())
                    .setTwinflowTransition(transitionContext.getTransitionEntity()));
        }
        transitionContext.setAttachmentsFilled(true);
    }

    public void transitionToDstStatus(TransitionContext transitionContext, FactoryResultUncommited factoryResultUncommited) throws ServiceException {
        for (TwinUpdate twinUpdate : factoryResultUncommited.getUpdates()) {
            if (isTransitionedTwin(transitionContext, twinUpdate.getTwinEntity())) {// case when twin was taken from input, we have to force update status from transition
                if (twinUpdate.getTwinEntity().getTwinStatusId() == null || twinUpdate.getDbTwinEntity().getTwinStatusId().equals(twinUpdate.getTwinEntity().getTwinStatusId()))
                    twinUpdate.getTwinEntity()
                            .setTwinStatusId(transitionContext.getTransitionEntity().getDstTwinStatusId())
                            .setTwinStatus(transitionContext.getTransitionEntity().getDstTwinStatus());
            }
        }
        if (factoryResultUncommited.getUpdates() == null)
            System.out.println();
    }

    @Transactional
    public TransitionResult commitFactoryResult(TransitionContext transitionContext, FactoryResultUncommited factoryResultUncommited) throws ServiceException {
        FactoryResultCommited factoryResultCommited = twinFactoryService.commitResult(factoryResultUncommited);
        if (factoryResultCommited instanceof FactoryResultCommitedMinor factoryResultCommitedMinor) {
            TransitionResultMinor transitionResultMinor = new TransitionResultMinor();
            transitionResultMinor.addProcessedTwins(factoryResultCommitedMinor.getCreatedTwinList());
            for (TwinEntity twinUpdated : factoryResultCommitedMinor.getUpdatedTwinList()) {
                if (isTransitionedTwin(transitionContext, twinUpdated))
                    transitionResultMinor.addTransitionedTwin(twinUpdated);
                else
                    transitionResultMinor.addProcessedTwin(twinUpdated);
            }
            transitionResultMinor.setDeletedTwinIdList(factoryResultCommitedMinor.getDeletedTwinIdList());
            return transitionResultMinor;
        } else if (factoryResultCommited instanceof FactoryResultCommitedMajor factoryResultCommitedMajor) {
            return new TransitionResultMajor().setCommitedDraftEntity(factoryResultCommitedMajor.getCommitedDraftEntity());
        }
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }


    public void commitFactoriesResult(Map<TransitionContext, FactoryResultUncommited> factoryTransitions, TransitionResultMinor transitionResultMinor) throws ServiceException {
        for (var entry : factoryTransitions.entrySet()) {
            FactoryResultCommited factoryResultCommited = twinFactoryService.commitResult(entry.getValue());
            if (factoryResultCommited instanceof FactoryResultCommitedMinor factoryResultCommitedMinor) {
                transitionResultMinor.addProcessedTwins(factoryResultCommitedMinor.getCreatedTwinList());
                for (TwinEntity twinUpdated : factoryResultCommitedMinor.getUpdatedTwinList()) {
                    if (isTransitionedTwin(entry.getKey(), twinUpdated))
                        transitionResultMinor.addTransitionedTwin(twinUpdated);
                    else
                        transitionResultMinor.addProcessedTwin(twinUpdated);
                }
                transitionResultMinor.setDeletedTwinIdList(factoryResultCommitedMinor.getDeletedTwinIdList());
            } else { // we can not process FactoryResultCommitedMajor,
                throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
            }
        }

    }

    public static boolean isTransitionedTwin(TransitionContext transitionContext, TwinEntity twinEntity) {
        return transitionContext.getTargetTwinList() != null && transitionContext.getTargetTwinList().containsKey(twinEntity.getId());
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

    public PaginationResult<TwinflowTransitionAliasEntity> findTransitionAliases(TransitionAliasSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinflowTransitionAliasEntity> spec = createTransitionAliasSearchSpecification(search);
        Page<TwinflowTransitionAliasEntity> ret = twinflowTransitionAliasRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinflowTransitionAliasEntity> createTransitionAliasSearchSpecification(TransitionAliasSearch search) throws ServiceException {
        return Specification.allOf(
                checkFieldUuid(authService.getApiUser().getDomainId(), TwinflowTransitionAliasEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinflowTransitionAliasEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinflowTransitionAliasEntity.Fields.id),
                checkFieldLikeIn(search.getAliasLikeList(), false, true, TwinflowTransitionAliasEntity.Fields.alias),
                checkFieldLikeIn(search.getAliasNotLikeList(), true, true, TwinflowTransitionAliasEntity.Fields.alias)
        );
    }

    public void countUsagesInTwinflowTransition(TwinflowTransitionAliasEntity transitionAlias) {
        countUsagesInTwinflowTransition(Collections.singletonList(transitionAlias));
    }

    public void countUsagesInTwinflowTransition(Collection<TwinflowTransitionAliasEntity> transitionAliasList) {
        Kit<TwinflowTransitionAliasEntity, UUID> needLoad = new Kit<>(TwinflowTransitionAliasEntity::getId);
        for (TwinflowTransitionAliasEntity transitionAlias : transitionAliasList) {
            if (transitionAlias.getInTwinflowTransitionUsagesCount() == null)
                needLoad.add(transitionAlias);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> transitionAliasMap = mapUuidInt(twinflowTransitionRepository.countByTransitionAliasIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(transitionAlias -> transitionAlias.setInTwinflowTransitionUsagesCount(transitionAliasMap.getOrDefault(transitionAlias.getId(), 0)));
    }
}

