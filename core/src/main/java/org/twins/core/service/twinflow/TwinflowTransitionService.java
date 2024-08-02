package org.twins.core.service.twinflow;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.LoggerUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.*;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.domain.factory.*;
import org.twins.core.domain.transition.*;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.auth.AuthService;
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


@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionService extends EntitySecureFindServiceImpl<TwinflowTransitionEntity> {
    final TwinflowTransitionRepository twinflowTransitionRepository;
    final TwinflowTransitionValidatorRepository twinflowTransitionValidatorRepository;
    final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;
    final TwinStatusTransitionTriggerRepository twinStatusTransitionTriggerRepository;
    final TwinflowTransitionAliasRepository twinflowTransitionAliasRepository;
    final TwinClassService twinClassService;
    final TwinFactoryService twinFactoryService;
    final TwinStatusService twinStatusService;
    @Lazy
    final TwinService twinService;
    final TwinflowService twinflowService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;
    @Lazy
    final DraftService draftService;
    final UserGroupService userGroupService;
    final PermissionService permissionService;
    final UserService userService;
    final I18nService i18nService;

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
                if (entity.getSrcTwinStatus() == null && entity.getSrcTwinStatusId() != null)
                    entity.setSrcTwinStatus(twinStatusService.findEntitySafe(entity.getSrcTwinStatusId()));
                if (entity.getDstTwinStatus() == null)
                    entity.setDstTwinStatus(twinStatusService.findEntitySafe(entity.getDstTwinStatusId()));
                if (entity.getTwinflow() == null)
                    entity.setTwinflow(twinflowService.findEntitySafe(entity.getTwinflowId()));
                if (entity.getPermission() == null && entity.getPermissionId() != null)
                    entity.setPermission(permissionService.findEntitySafe(entity.getPermissionId()));
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

    public TwinflowTransitionEntity createTwinflowTransition(TwinflowTransitionEntity twinflowTransitionEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        twinflowTransitionEntity
                .setNameI18NId(i18nService.createI18nAndTranslations(I18nType.TWINFLOW_TRANSITION_NAME, nameI18n).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.TWINFLOW_TRANSITION_DESCRIPTION, descriptionI18n).getId())
                .setCreatedByUserId(apiUser.getUserId())
                .setTwinflowTransitionAliasId(creatAliasIfNeeded(twinflowTransitionEntity.getTwinflowTransitionAlias()));
        validateEntityAndThrow(twinflowTransitionEntity, EntitySmartService.EntityValidateMode.beforeSave);
        return entitySmartService.save(twinflowTransitionEntity, twinflowTransitionRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }

    private UUID creatAliasIfNeeded(TwinflowTransitionAliasEntity transitionAlias) throws ServiceException {
        if (transitionAlias.getDomainId() == null)
            transitionAlias.setDomainId(authService.getApiUser().getDomainId());
        if (transitionAlias.getId() != null)
            return transitionAlias.getId();
        UUID currentTransitionAliasId = twinflowTransitionAliasRepository.findIdByDomainIdAndAlias(transitionAlias.getDomainId(), transitionAlias.getAlias());
        if (currentTransitionAliasId != null)
            transitionAlias.setId(currentTransitionAliasId);
        else
            saveTwinflowTransitionAlias(transitionAlias);
        return transitionAlias.getId();
    }

    private TwinflowTransitionAliasEntity saveTwinflowTransitionAlias(TwinflowTransitionAliasEntity transitionAlias) throws ServiceException {
        return entitySmartService.save(transitionAlias, twinflowTransitionAliasRepository, EntitySmartService.SaveMode.saveAndLogOnException);
    }

    @Transactional
    public TwinflowTransitionEntity updateTwinflowTransition(TwinflowTransitionEntity twinflowTransitionEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        TwinflowTransitionEntity dbTwinflowTransitionEntity = findEntitySafe(twinflowTransitionEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateTransitionName(dbTwinflowTransitionEntity, nameI18n, changesHelper);
        updateTransitionDescription(dbTwinflowTransitionEntity, descriptionI18n, changesHelper);
        updateTransitionInBuildFactory(dbTwinflowTransitionEntity, twinflowTransitionEntity.getInbuiltTwinFactoryId(), changesHelper);
        updateTransitionDraftingFactory(dbTwinflowTransitionEntity, twinflowTransitionEntity.getDraftingTwinFactoryId(), changesHelper);
        updateTransitionPermission(dbTwinflowTransitionEntity, twinflowTransitionEntity.getPermissionId(), changesHelper);
        updateTransitionSrcStatus(dbTwinflowTransitionEntity, twinflowTransitionEntity.getSrcTwinStatusId(), changesHelper);
        updateTransitionDstStatus(dbTwinflowTransitionEntity, twinflowTransitionEntity.getDstTwinStatusId(), changesHelper);
        dbTwinflowTransitionEntity = entitySmartService.saveAndLogChanges(dbTwinflowTransitionEntity, twinflowTransitionRepository, changesHelper);
        twinClassService.evictCache(dbTwinflowTransitionEntity.getTwinflow().getTwinClassId());
        return dbTwinflowTransitionEntity;
    }

    @Transactional
    public void updateTransitionDescription(TwinflowTransitionEntity dbTwinflowTransitionEntity, I18nEntity descriptionI18n, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbTwinflowTransitionEntity.getDescriptionI18NId() != null)
            descriptionI18n.setId(dbTwinflowTransitionEntity.getDescriptionI18NId());
        i18nService.saveTranslations(I18nType.TWINFLOW_DESCRIPTION, descriptionI18n);
        dbTwinflowTransitionEntity.setDescriptionI18NId(descriptionI18n.getId());
    }


    @Transactional
    public void updateTransitionName(TwinflowTransitionEntity dbTwinflowTransitionEntity, I18nEntity nameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbTwinflowTransitionEntity.getNameI18NId() != null)
            nameI18n.setId(dbTwinflowTransitionEntity.getNameI18NId());
        i18nService.saveTranslations(I18nType.TWINFLOW_NAME, nameI18n);
        dbTwinflowTransitionEntity.setNameI18NId(nameI18n.getId());
    }

    @Transactional
    public void updateTransitionInBuildFactory(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID factoryId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("inbuiltTwinFactoryId", dbTwinflowTransitionEntity.getInbuiltTwinFactoryId(), factoryId))
            return;
        dbTwinflowTransitionEntity.setInbuiltTwinFactoryId(factoryId);
    }

    @Transactional
    public void updateTransitionDraftingFactory(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID factoryId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("draftingTwinFactoryId", dbTwinflowTransitionEntity.getDraftingTwinFactoryId(), factoryId))
            return;
        dbTwinflowTransitionEntity.setDraftingTwinFactoryId(factoryId);
    }

    @Transactional
    public void updateTransitionPermission(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID permissionId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("permissionId", dbTwinflowTransitionEntity.getPermissionId(), permissionId))
            return;
        dbTwinflowTransitionEntity.setPermissionId(permissionId);
    }

    @Transactional
    public void updateTransitionSrcStatus(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID statusId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("srcStatusId", dbTwinflowTransitionEntity.getSrcTwinStatusId(), statusId))
            return;
        if(null != statusId && !twinClassService.isStatusAllowedForTwinClass(dbTwinflowTransitionEntity.getTwinflow().getTwinClass(), statusId))
            throw new ServiceException(ErrorCodeTwins.TRANSITION_STATUS_INCORRECT, "status[" + statusId + "] is not allowed for twinClass[" + dbTwinflowTransitionEntity.getTwinflow().getTwinClassId() + "]");
        dbTwinflowTransitionEntity.setSrcTwinStatusId(statusId);
    }

    @Transactional
    public void updateTransitionDstStatus(TwinflowTransitionEntity dbTwinflowTransitionEntity, UUID statusId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("dstStatusId", dbTwinflowTransitionEntity.getSrcTwinStatusId(), statusId))
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

    public TransitionContextBatch createTransitionContext(Collection<TwinEntity> twinEntities, String transitionAlias) throws ServiceException {
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
        return new TransitionContextBatch(transitionContextMap.values());
    }

    public void validateTransition(TransitionContext transitionContext) throws ServiceException {
        if (transitionContext.isValidated())
            return;
        List<TwinflowTransitionValidatorEntity> transitionValidatorEntityList = twinflowTransitionValidatorRepository.findByTwinflowTransitionIdOrderByOrder(transitionContext.getTransitionEntity().getId());
        for (TwinEntity twinEntity : transitionContext.getTargetTwinList().values())
            if (!runTransitionValidators(transitionContext.getTransitionEntity(), transitionValidatorEntityList, twinEntity))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_DENIED);
        transitionContext.setValidated(true);
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

            TwinValidator transitionValidator = featurerService.getFeaturer(transitionValidatorEntity.getTransitionValidatorFeaturer(), TwinValidator.class);
            TwinValidator.ValidationResult validationResult = transitionValidator.isValid(transitionValidatorEntity.getTwinValidatorParams(), twinEntity, transitionValidatorEntity.isInvert());
            if (!validationResult.isValid()) {
                log.info(twinflowTransitionEntity.easyLog(EasyLoggable.Level.NORMAL) + " is not valid. " + validationResult.getMessage());
                return false;
            }
        }
        return true;
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
        draftService.draftFactoryResult(draftCollector, transitionContextBatch.getFactoried().values());
        for (TransitionContext transitionContext : transitionContextBatch.getSimple()) {
            for (TwinEntity twinEntity : transitionContext.getTargetTwinList().values()) {
                draftService.draftUpdate(draftCollector, new TwinEntity()
                        .setId(twinEntity.getId())
                        .setTwinStatusId(transitionContext.getTransitionEntity().getDstTwinStatusId()));
            }
        }
        draftService.endDraft(draftCollector);
        return draftCollector.getDraftEntity();
    }

    @Transactional
    public TransitionResult performTransition(TransitionContext transitionContext) throws ServiceException {
        return performTransitions(new TransitionContextBatch(List.of(transitionContext)));
    }

    @Transactional
    public TransitionResult performTransitions(TransitionContextBatch transitionContextBatch) throws ServiceException {
        for (TransitionContext transitionContext : transitionContextBatch.getAll()) {
            validateTransition(transitionContext);
            fillAttachmentsTransition(transitionContext);
        }
        runFactories(transitionContextBatch);
        TransitionResult transitionResult = null;
        if (transitionContextBatch.isMustBeDrafted()) { // we will go to drafting
            DraftEntity draftEntity = draftTransitions(transitionContextBatch);
            draftService.commit(draftEntity.getId());
            TransitionResultMajor transitionResultMajor = new TransitionResultMajor();
            transitionResultMajor.setCommitedDraftEntity(draftEntity);
            transitionResult = transitionResultMajor;
        } else {
            TransitionResultMinor transitionResultMinor = new TransitionResultMinor();
            for (TransitionContext transitionContext : transitionContextBatch.getSimple()) {
                twinService.changeStatus(transitionContext.getTargetTwinList().values(), transitionContext.getTransitionEntity().getDstTwinStatus());
                transitionResultMinor.setTransitionedTwinList(transitionContext.getTargetTwinList().values().stream().toList());
            }
            commitFactoriesResult(transitionContextBatch.getFactoried(), transitionResultMinor); // without "drafting" we can get only minor results
        }
        for (TransitionContext transitionContext : transitionContextBatch.getAll()) {
            runTriggers(transitionContext);
        }
        return transitionResult;
    }

    public void runFactories(TransitionContextBatch transitionContextBatch) throws ServiceException {
        for (Map.Entry<TransitionContext, FactoryResultUncommited> entry : transitionContextBatch.getFactoried().entrySet()) {
            if (entry.getValue() != null) //factory is already run
                continue;
            FactoryResultUncommited factoryResultUncommited = runTransitionFactory(entry.getKey());
            entry.setValue(factoryResultUncommited); //filling result
            if (CollectionUtils.isNotEmpty(factoryResultUncommited.getDeletes()))
                transitionContextBatch.setMustBeDrafted(true);
        }
    }

    private FactoryResultUncommited runTransitionFactory(TransitionContext transitionContext) throws ServiceException {
        FactoryContext factoryContext = new FactoryContext()
                .setInputTwinList(transitionContext.getTargetTwinList().values())
                .setFields(transitionContext.getFields())
                .setAttachmentCUD(transitionContext.getAttachmentCUD());
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
            factoryResultUncommited = twinFactoryService.runFactory(transitionContext.getTransitionEntity().getInbuiltTwinFactoryId(), factoryContext);
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

    @Transactional
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

}

