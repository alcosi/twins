package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.Kit;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowService extends EntitySecureFindServiceImpl<TwinflowEntity> {
    final TwinflowRepository twinflowRepository;
    final TwinflowSchemaRepository twinflowSchemaRepository;
    final TwinflowSchemaMapRepository twinflowSchemaMapRepository;
    final TwinflowTransitionRepository twinflowTransitionRepository;
    final TwinflowTransitionValidatorRepository twinflowTransitionValidatorRepository;
    final TwinStatusTransitionTriggerRepository twinStatusTransitionTriggerRepository;
    @Lazy
    final DomainService domainService;
    final TwinClassService twinClassService;
    final TwinStatusService twinStatusService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;
    @Lazy
    final TwinService twinService;

    @Override
    public CrudRepository<TwinflowEntity, UUID> entityRepository() {
        return twinflowRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinflowEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassId");
        if (entity.getInitialTwinStatusId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty initialTwinStatusId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinClass() == null)
                    entity.setTwinClass(twinClassService.findEntitySafe(entity.getTwinClassId()));
                if (entity.getInitialTwinStatus() == null)
                    entity.setInitialTwinStatus(twinStatusService.findEntitySafe(entity.getInitialTwinStatusId()));
                if (entity.getTwinClassId() != entity.getTwinClass().getId())
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect dstTwin object");
                if (entity.getInitialTwinStatusId() != entity.getInitialTwinStatus().getId())
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect srcTwin object");
            default:
                if (!twinClassService.isInstanceOf(entity.getTwinClassId(), entity.getInitialTwinStatus().getTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect initialTwinStatusId[" + entity.getInitialTwinStatusId() + "]");
        }
        return true;
    }

    public UUID checkTwinflowSchemaAllowed(UUID domainId, UUID businessAccountId, UUID twinFlowsSchemaId) throws ServiceException {
        Optional<TwinflowSchemaEntity> permissionSchemaEntity = twinflowSchemaRepository.findById(twinFlowsSchemaId);
        if (permissionSchemaEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown twinFlowsSchemaId[" + twinFlowsSchemaId + "]");
        if (permissionSchemaEntity.get().domainId() != domainId)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_ALLOWED, "twinFlowsSchemaId[" + twinFlowsSchemaId + "] is not allows in domain[" + domainId + "]");
        if (permissionSchemaEntity.get().businessAccountId() != null && permissionSchemaEntity.get().businessAccountId() != businessAccountId)
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

    private UUID getTwinflowSchemaSpaceId(TwinEntity twinEntity) {
        if (twinEntity.getTwinflowSchemaSpaceId() != null)
            return twinEntity.getTwinflowSchemaSpaceId();
        //looks like new twin creation
        twinService.loadHeadForTwin(twinEntity);
        if (twinEntity.getHeadTwin() != null)
            return  twinEntity.getHeadTwin().getTwinflowSchemaSpaceId();
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
            twinflowEntity = twinflowMap.get(twinEntity.getTwinClassId().toString() + (twinEntity.getTwinflowSchemaSpaceId() != null ? twinEntity.getTwinflowSchemaSpaceId() : ""));
            if (twinflowEntity == null)
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_CONFIGURED, twinEntity.logNormal() + " can not detect twinflow");
            twinEntity.setTwinflow(twinflowEntity);
        }
    }

    public void loadTwinflowsForTwinClasses(List<TwinClassEntity> twinClasses) {
        for (TwinClassEntity twinClass : twinClasses) {
            loadTwinflows(twinClass);
        }
    }

    public void loadTwinflows(TwinClassEntity twinClass) {
        if (twinClass.getTwinflowKit() == null) {
            twinClass.setTwinflowKit(new Kit<>(twinflowRepository.findByTwinClassId(twinClass.getId()), TwinflowEntity::getId));
        }
    }

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
            TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturer(), TransitionTrigger.class);
            transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), twinEntity, srcStatusEntity, dstStatusEntity);
        }
    }

    public void forceDeleteSchemas(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        List<UUID> schemasToDelete = twinflowRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(schemasToDelete, twinflowRepository);
    }
}

