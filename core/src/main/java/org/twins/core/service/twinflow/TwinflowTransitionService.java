package org.twins.core.service.twinflow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.LoggerUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.*;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinCreate;
import org.twins.core.domain.TwinOperation;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.transition.TransitionContext;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserGroupService;
import org.twins.core.service.user.UserService;

import java.util.*;


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
    final UserGroupService userGroupService;
    final PermissionService permissionService;
    final UserService userService;
    final I18nService i18nService;

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

    public TwinflowTransitionEntity createTwinflowTransition(TwinflowTransitionEntity twinflowTransitionEntity, String nameInDefaultLocale) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        twinflowTransitionEntity
                .setNameI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_STATUS_NAME, nameInDefaultLocale).getI18nId())
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

            TwinValidator transitionValidator = featurerService.getFeaturer(transitionValidatorEntity.getTransitionValidatorFeaturer(), TwinValidator.class);
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
        if (transitionContext.getTransitionEntity().getInbuiltTwinFactoryId() != null) {
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
            transitionedTwinList = org.cambium.common.util.CollectionUtils.safeAdd(transitionedTwinList, twinEntity);
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
            processedTwinList = org.cambium.common.util.CollectionUtils.safeAdd(processedTwinList, twinEntity);
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

