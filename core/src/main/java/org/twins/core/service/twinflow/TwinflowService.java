package org.twins.core.service.twinflow;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.CacheEvictCollector;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CacheUtils;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinflow.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.enums.status.StatusType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinflowService extends EntitySecureFindServiceImpl<TwinflowEntity> {
    private final TwinflowRepository twinflowRepository;
    private final TwinflowSchemaRepository twinflowSchemaRepository;
    private final TwinflowSchemaMapRepository twinflowSchemaMapRepository;
    private final TwinStatusTransitionTriggerRepository twinStatusTransitionTriggerRepository;
    private final TwinClassService twinClassService;
    private final TwinStatusService twinStatusService;
    private final I18nService i18nService;
    @Lazy
    private final FeaturerService featurerService;
    @Lazy
    private final AuthService authService;
    @Lazy
    private final TwinService twinService;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public CrudRepository<TwinflowEntity, UUID> entityRepository() {
        return twinflowRepository;
    }

    @Override
    public Function<TwinflowEntity, UUID> entityGetIdFunction() {
        return TwinflowEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return twinClassService.isEntityReadDenied(entity.getTwinClass(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinflowEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassId");
        if (entity.getInitialTwinStatusId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty initialTwinStatusId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getTwinClassId()))
                    entity.setTwinClass(twinClassService.findEntitySafe(entity.getTwinClassId()));
                if (entity.getInitialTwinStatus() == null || !entity.getInitialTwinStatus().getId().equals(entity.getInitialTwinStatusId()))
                    entity.setInitialTwinStatus(twinStatusService.findEntitySafe(entity.getInitialTwinStatusId()));
            default:
                if (!twinClassService.isInstanceOf(entity.getTwinClass(), entity.getInitialTwinStatus().getTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect initialTwinStatusId[" + entity.getInitialTwinStatusId() + "]");
        }
        return true;
    }

    public UUID checkTwinflowSchemaAllowed(UUID domainId, UUID businessAccountId, UUID twinFlowsSchemaId) throws ServiceException {
        Optional<TwinflowSchemaEntity> permissionSchemaEntity = twinflowSchemaRepository.findById(twinFlowsSchemaId);
        if (permissionSchemaEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown twinFlowsSchemaId[" + twinFlowsSchemaId + "]");
        if (!permissionSchemaEntity.get().getDomainId().equals(domainId))
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_ALLOWED, "twinFlowsSchemaId[" + twinFlowsSchemaId + "] is not allows in domain[" + domainId + "]");
        if (permissionSchemaEntity.get().getBusinessAccountId() != null && !permissionSchemaEntity.get().getBusinessAccountId().equals(businessAccountId))
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_ALLOWED, "twinFlowsSchemaId[" + twinFlowsSchemaId + "] is not allows in businessAccount[" + businessAccountId + "]");
        return twinFlowsSchemaId;
    }

    public TwinflowEntity loadTwinflow(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getTwinflow() != null)
            return twinEntity.getTwinflow();
        ApiUser apiUser = authService.getApiUser();
        TwinflowEntity twinflowEntity = twinflowRepository.twinflowDetect(
                apiUser.getDomainId(),
                TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()),
                TypedParameterTwins.uuidNullable(getTwinflowSchemaSpaceId(twinEntity)),
                TypedParameterTwins.uuidNullable(twinEntity.getTwinClassId()));
        if (twinflowEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_CONFIGURED, "Twinflow is not configured for twinClass[" + twinEntity.getTwinClassId() + "]");
        twinEntity.setTwinflow(twinflowEntity);
        return twinflowEntity;
    }

    private UUID getTwinflowSchemaSpaceId(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getTwinflowSchemaSpaceId() != null)
            return twinEntity.getTwinflowSchemaSpaceId();
        //looks like new twin creation
        twinService.loadHeadForTwin(twinEntity);
        if (twinEntity.getHeadTwin() != null)
            return twinEntity.getHeadTwin().getTwinflowSchemaSpaceId();
        return null;
    }

    public void loadTwinflow(Collection<TwinEntity> twinEntityList) throws ServiceException {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList) {
            if (twinEntity.getTwinflow() != null)
                continue;
            needLoad.put(twinEntity.getId(), twinEntity);
        }
        if (MapUtils.isEmpty(needLoad))
            return;
        ApiUser apiUser = authService.getApiUser();
        List<Object[]> twinflowList = twinflowRepository.twinflowsDetect(apiUser.getDomainId(), TypedParameterTwins.uuidNullable(apiUser.getBusinessAccountId()), needLoad.keySet());
        Map<String, TwinflowEntity> twinflowMap = new HashMap<>();
        for (Object[] dbRow : twinflowList) {
            twinflowMap.put((String) dbRow[0], (TwinflowEntity) dbRow[1]);
        }
        TwinflowEntity twinflowEntity = null;
        for (TwinEntity twinEntity : needLoad.values()) {
            //twinflow can be inherited from extended class, that is why twin.getTwinClassId is not always equal to twinflow.twinClassId here
            twinflowEntity = twinflowMap.get(twinEntity.getTwinClassId().toString() + (twinEntity.getTwinflowSchemaSpaceId() != null ? twinEntity.getTwinflowSchemaSpaceId() : ""));
            if (twinflowEntity == null)
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_CONFIGURED, twinEntity.logNormal() + " can not detect twinflow");
            twinEntity.setTwinflow(validateEntityAndThrow(twinflowEntity, EntitySmartService.EntityValidateMode.afterRead));
        }
    }

    public void loadTwinflows(TwinClassEntity twinClass) {
        loadTwinflows(Collections.singletonList(twinClass));
    }

    public void loadTwinflows(List<TwinClassEntity> twinClasses) {
        Kit<TwinClassEntity, UUID> needLoad = new Kit<>(TwinClassEntity::getId);
        Set<UUID> needLoadClassesIds = new HashSet<>();
        for (var twinClass : twinClasses) {
            if (twinClass.getTwinflowKit() != null)
                continue;
            twinClass.setTwinflowKit(new Kit<>(TwinflowEntity::getId));
            needLoad.add(twinClass);
            needLoadClassesIds.addAll(twinClass.getExtendedClassIdSet()); //current class id is already in this set
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        List<TwinflowEntity> twinflows = twinflowRepository.findByTwinClassIdIn(needLoadClassesIds);
        if (CollectionUtils.isEmpty(twinflows))
            return;
        KitGrouped<TwinflowEntity, UUID, UUID> loaded = new KitGrouped<>(twinflows, TwinflowEntity::getId, TwinflowEntity::getTwinClassId);
        for (var twinClass : needLoad.getCollection()) {
            for (var extendedTwinClassId : twinClass.getExtendedClassIdSet()) {
                twinClass.getTwinflowKit().addAll(loaded.getGrouped(extendedTwinClassId));
            }
        }
    }

    @Transactional
    public void runTwinStatusTransitionTriggers(TwinEntity twinEntity, TwinStatusEntity srcStatusEntity, TwinStatusEntity dstStatusEntity) throws ServiceException {
        List<TwinStatusTransitionTriggerEntity> triggerEntityList;
        UUID srcStatusId = srcStatusEntity != null ? srcStatusEntity.getId() : null;
        UUID dstStatusId = dstStatusEntity != null ? dstStatusEntity.getId() : null;
        if (srcStatusId != null && !srcStatusId.equals(dstStatusId)) { // outgoing triggers
            triggerEntityList = twinStatusTransitionTriggerRepository.findAllByTwinStatusIdAndTypeAndActiveOrderByOrder(srcStatusId, TwinStatusTransitionTriggerEntity.TransitionType.outgoing, true);
            runTriggers(twinEntity, triggerEntityList, srcStatusEntity, dstStatusEntity);
        }
        if (dstStatusId != null && !dstStatusId.equals(srcStatusId)) { // incoming triggers
            triggerEntityList = twinStatusTransitionTriggerRepository.findAllByTwinStatusIdAndTypeAndActiveOrderByOrder(dstStatusId, TwinStatusTransitionTriggerEntity.TransitionType.incoming, true);
            runTriggers(twinEntity, triggerEntityList, srcStatusEntity, dstStatusEntity);
        }
    }

    private void runTriggers(TwinEntity twinEntity, List<TwinStatusTransitionTriggerEntity> twinStatusTransitionTriggerEntityList, TwinStatusEntity srcStatusEntity, TwinStatusEntity dstStatusEntity) throws ServiceException {
        if (CollectionUtils.isEmpty(twinStatusTransitionTriggerEntityList))
            return;
        for (TwinStatusTransitionTriggerEntity triggerEntity : twinStatusTransitionTriggerEntityList) {
            log.info(triggerEntity.easyLog(EasyLoggable.Level.DETAILED) + " will be triggered");
            TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturerId(), TransitionTrigger.class);
            transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), twinEntity, srcStatusEntity, dstStatusEntity);
        }
    }

    public void forceDeleteSchemas(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        List<UUID> schemasToDelete = twinflowRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(schemasToDelete, twinflowRepository);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinflowEntity createTwinflow(TwinClassEntity twinClassEntity, TwinStatusEntity twinStatusEntity) throws ServiceException {
        if (!twinClassService.isStatusAllowedForTwinClass(twinClassEntity, twinStatusEntity.getId()))
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_INIT_STATUS_INCORRECT, twinStatusEntity.logShort() + " is not allowed for " + twinClassEntity.logShort());
        String twinflowName = "Default " + twinClassEntity.getKey() + " twinflow";
        TwinflowEntity twinflowEntity = new TwinflowEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setNameI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWINFLOW_NAME, twinflowName).getId())
                .setDescriptionI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWINFLOW_DESCRIPTION, twinflowName).getId())
                .setInitialTwinStatusId(twinStatusEntity.getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(SystemEntityService.USER_SYSTEM);
        return saveSafe(twinflowEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinflowEntity createTwinflow(TwinflowEntity twinflowEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        if (!twinClassService.isStatusAllowedForTwinClass(twinflowEntity.getTwinClassId(), twinflowEntity.getInitialTwinStatusId()))
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_INIT_STATUS_INCORRECT, "status[" + twinflowEntity.getInitialTwinStatusId() + "] is not allowed for twinClass[" + twinflowEntity.getTwinClassId() + "]");

        ApiUser apiUser = authService.getApiUser();
        twinflowEntity
                .setNameI18NId(i18nService.createI18nAndTranslations(I18nType.TWINFLOW_NAME, nameI18n).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.TWINFLOW_DESCRIPTION, descriptionI18n).getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(apiUser.getUserId());
        return saveSafe(twinflowEntity);
    }

    @Transactional
    public TwinflowSchemaMapEntity registerTwinflow(TwinflowEntity twinflowEntity, DomainEntity domainEntity, TwinClassEntity twinClassEntity) throws ServiceException {
        TwinflowSchemaMapEntity twinflowSchemaMapEntity = new TwinflowSchemaMapEntity()
                .setTwinflowSchemaId(domainEntity.getTwinflowSchemaId())
                .setTwinClassId(twinClassEntity.getId())
                .setTwinflowId(twinflowEntity.getId())
                .setTwinflow(twinflowEntity);
        return entitySmartService.save(twinflowSchemaMapEntity, twinflowSchemaMapRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinflowEntity updateTwinflow(TwinflowEntity twinflowEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        TwinflowEntity dbTwinflowEntity = findEntitySafe(twinflowEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateTwinflowName(dbTwinflowEntity, nameI18n, changesHelper);
        updateTwinflowDescription(dbTwinflowEntity, descriptionI18n, changesHelper);
        updateTwinflowInitStatus(dbTwinflowEntity, twinflowEntity.getInitialTwinStatusId(), changesHelper);
        dbTwinflowEntity = updateSafe(dbTwinflowEntity, changesHelper);
        if (changesHelper.hasChanges()) {
            CacheEvictCollector cacheEvictCollector = new CacheEvictCollector();
            cacheEvictCollector
                    .add(dbTwinflowEntity.getTwinClassId(), TwinClassRepository.CACHE_TWIN_CLASS_BY_ID)
                    .add(dbTwinflowEntity.getTwinClassId() ,TwinClassEntity.class.getSimpleName());
            CacheUtils.evictCache(cacheManager, cacheEvictCollector);
        }
        return dbTwinflowEntity;
    }

    public void updateTwinflowDescription(TwinflowEntity dbTwinflowEntity, I18nEntity descriptionI18n, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbTwinflowEntity.getDescriptionI18NId() != null)
            descriptionI18n.setId(dbTwinflowEntity.getDescriptionI18NId());
        i18nService.saveTranslations(I18nType.TWINFLOW_DESCRIPTION, descriptionI18n);
        if (changesHelper.isChanged(TwinflowEntity.Fields.descriptionI18NId, dbTwinflowEntity.getDescriptionI18NId(), descriptionI18n.getId()))
            dbTwinflowEntity.setDescriptionI18NId(descriptionI18n.getId());
    }


    public void updateTwinflowName(TwinflowEntity dbTwinflowEntity, I18nEntity nameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbTwinflowEntity.getNameI18NId() != null)
            nameI18n.setId(dbTwinflowEntity.getNameI18NId());
        i18nService.saveTranslations(I18nType.TWINFLOW_NAME, nameI18n);
        if (changesHelper.isChanged(TwinflowEntity.Fields.nameI18NId, dbTwinflowEntity.getNameI18NId(), nameI18n.getId()))
            dbTwinflowEntity.setNameI18NId(nameI18n.getId());
    }

    public void updateTwinflowInitStatus(TwinflowEntity dbTwinflowEntity, UUID initStatusId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(TwinflowEntity.Fields.initialTwinStatusId, dbTwinflowEntity.getInitialTwinStatusId(), initStatusId))
            return;
        if (!twinClassService.isStatusAllowedForTwinClass(dbTwinflowEntity.getTwinClass(), initStatusId))
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_INIT_STATUS_INCORRECT, "status[" + initStatusId + "] is not allowed for twinClass[" + dbTwinflowEntity.getTwinClassId() + "]");
        dbTwinflowEntity.setInitialTwinStatusId(initStatusId);
    }

    public TwinStatusEntity getInitSketchStatusSafe(TwinflowEntity twinflow) throws ServiceException {
        var sketchStatus = twinflow.getInitialSketchTwinStatusId(); //hope that getTwinClass is not null
        if (sketchStatus == null) {
            throw new ServiceException(ErrorCodeTwins.TWIN_STATUS_SKETCH_FORBIDDEN);
        }
        if (!twinflow.getInitialSketchTwinStatus().getType().equals(StatusType.SKETCH)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_STATUS_INCORRECT, "configured status[{}] is not a sketch status", sketchStatus);
        }
        return twinflow.getInitialSketchTwinStatus();
    }
}

